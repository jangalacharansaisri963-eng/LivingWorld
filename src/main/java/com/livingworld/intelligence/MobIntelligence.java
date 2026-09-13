package com.livingworld.intelligence;

import com.livingworld.LivingWorldConstants;
import com.livingworld.adaptation.AdaptationSubsystem;
import com.livingworld.adaptation.MobPersonality;
import com.livingworld.behavior.BehaviorEvaluator;
import com.livingworld.behavior.BehaviorState;
import com.livingworld.behavior.EvaluationResult;
import com.livingworld.config.ConfigManager;
import com.livingworld.config.LivingWorldConfig;
import com.livingworld.core.IntelligencePipeline;
import com.livingworld.environment.EnvironmentSnapshot;
import com.livingworld.environment.EnvironmentSubsystem;
import com.livingworld.group.*;
import com.livingworld.knowledge.LocalWorldKnowledge;
import com.livingworld.learning.LearningSubsystem;
import com.livingworld.memory.Memory;
import com.livingworld.memory.MemoryBank;
import com.livingworld.perception.PerceptionSnapshot;
import com.livingworld.perception.PerceptionSubsystem;
import com.livingworld.reaction.ReactionSubsystem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;

import java.util.UUID;

/**
 * Core intelligence controller attached to each Living World intelligent mob.
 * Coordinates perception, memory, evaluation, reaction, learning, adaptation, group intelligence, and persistence.
 */
public class MobIntelligence implements IntelligencePipeline {
    private final MobEntity mob;
    private final MemoryBank memoryBank;
    private final MobPersonality personality;
    private final PerceptionSubsystem perception;
    private final EnvironmentSubsystem environment;
    private final BehaviorEvaluator evaluator;
    private final ReactionSubsystem reaction;
    private final LearningSubsystem learning;
    private final AdaptationSubsystem adaptation;
    private final LocalWorldKnowledge localKnowledge;

    private EvaluationResult currentEvaluation;
    private final int tickOffset;
    private UUID savedGroupId = null;

    public MobIntelligence(MobEntity mob) {
        this.mob = mob;
        this.memoryBank = new MemoryBank();
        this.personality = new MobPersonality();
        this.perception = new PerceptionSubsystem(mob);
        this.environment = new EnvironmentSubsystem(mob);
        this.evaluator = new BehaviorEvaluator(mob, memoryBank, personality);
        this.reaction = new ReactionSubsystem(mob);
        this.learning = new LearningSubsystem(mob, memoryBank, personality);
        this.adaptation = new AdaptationSubsystem(personality, memoryBank);
        this.localKnowledge = new LocalWorldKnowledge();

        // Stagger tick offset based on entity ID hash to spread workload smoothly across ticks
        this.tickOffset = Math.abs(mob.getId() % 20);
        this.currentEvaluation = EvaluationResult.calm("Initialized");
    }

    /**
     * Primary tick hook called from MobEntity.tick().
     */
    public void tick() {
        if (mob.getWorld().isClient() || !mob.isAlive()) {
            return;
        }

        long currentTick = mob.getWorld().getTime();
        LivingWorldConfig config = ConfigManager.get();

        // Check if this tick corresponds to this mob's staggered intelligence cycle
        if ((currentTick + tickOffset) % config.intelligenceTickInterval == 0) {
            executePipeline(currentTick);
        }

        // Periodic memory decay check
        if (currentTick % config.memoryDecayInterval == 0) {
            memoryBank.decay(currentTick);
        }
    }

    @Override
    public void executePipeline(long currentTick) {
        // Step 1: Perception
        PerceptionSnapshot perceptionSnapshot = perceive();

        // Step 2: Update Memories with active perceptions
        updateMemories(perceptionSnapshot, currentTick);

        // Step 3: Environmental Awareness
        EnvironmentSnapshot environmentSnapshot = environment.checkEnvironment(currentTick);

        // Step 4: Group Intelligence Coordination
        MobGroup group = null;
        if (ConfigManager.get().enableGroupIntelligence) {
            group = GroupManager.getInstance().findOrCreateGroup(mob, this, currentTick);
            if (group != null) {
                communicateWithGroup(group, perceptionSnapshot, environmentSnapshot, currentTick);
            }
        }

        // Step 5: Rule-based Behavior Evaluation (with group context)
        EvaluationResult decision = evaluate(perceptionSnapshot, environmentSnapshot, group, currentTick);
        this.currentEvaluation = decision;

        // Step 6: Decision & Reaction
        react(decision);

        // Step 7: Learning
        learn(decision, currentTick);

        // Step 8: Adaptation
        adapt();

        // Optional Debug Logging
        if (ConfigManager.get().debugMode && ConfigManager.get().logEvaluations && decision.getState() != BehaviorState.CALM) {
            LivingWorldConstants.LOGGER.debug("[LivingWorld V2] Mob {} (id={}): State={}, Group={}, Target={}, Reason='{}'",
                    mob.getType().getUntranslatedName(), mob.getId(), decision.getState(),
                    group != null ? group.getRole(mob.getUuid()) : "Solo",
                    decision.getTargetLocation(), decision.getReason());
        }
    }

    /**
     * Exchanges signals and synchronizes shared knowledge with nearby group companions.
     */
    private void communicateWithGroup(MobGroup group, PerceptionSnapshot perception,
                                     EnvironmentSnapshot environment, long currentTick) {
        // 1. Broadcast immediate danger if perceived
        if (perception.hasImmediateThreat()) {
            perception.getPrimaryThreat().ifPresent(threat -> {
                GroupSignal alert = new GroupSignal(
                        GroupSignalType.DANGER_ALERT, mob.getUuid(),
                        net.minecraft.util.math.BlockPos.ofFloored(threat.getPosition()),
                        threat.getUuid(), 1.0f, currentTick, threat.getType().getUntranslatedName()
                );
                group.broadcastSignal(alert, currentTick);
            });
        }

        // 2. Broadcast food discovery
        if (perception.hasFood()) {
            var foodPos = perception.getFoodLocations().get(0);
            GroupSignal foodSignal = new GroupSignal(
                    GroupSignalType.FOOD_SPOTTED, mob.getUuid(), foodPos,
                    null, 0.8f, currentTick, "Abundant food spotted"
            );
            group.broadcastSignal(foodSignal, currentTick);
        }

        // 3. Broadcast shelter discovery if high quality
        if (environment.getCurrentShelterScore() > 0.8f && environment.isHarshWeather()) {
            GroupSignal shelterSignal = new GroupSignal(
                    GroupSignalType.SHELTER_FOUND, mob.getUuid(), mob.getBlockPos(),
                    null, 0.9f, currentTick, "Solid overhead shelter located"
            );
            group.broadcastSignal(shelterSignal, currentTick);
        }

        // 4. Assimilate top shared memories into individual memory bank with high retention
        for (SharedMemory sm : group.getSharedMemories().getAll()) {
            if (sm.getConfidence() > 0.65f && sm.getLocation() != null) {
                Memory individualMem = new Memory(
                        sm.getCategory(), sm.getLocation(), currentTick,
                        sm.getImportance() * 0.8f, sm.getConfidence() * 0.8f,
                        "Shared via Group: " + sm.getContext()
                );
                individualMem.setShared(true);
                memoryBank.remember(individualMem, currentTick);
            }
        }
    }

    @Override
    public PerceptionSnapshot perceive() {
        return perception.scan(mob.getWorld().getTime());
    }

    @Override
    public void updateMemories(PerceptionSnapshot perceptionSnapshot, long currentTick) {
        // Active threats and food are prioritized in memory
        if (perceptionSnapshot.hasImmediateThreat()) {
            perceptionSnapshot.getPrimaryThreat().ifPresent(threat -> {
                learning.onDamageReceived(mob.getDamageSources().generic(), 2.0f, null, currentTick);
            });
        }
    }

    @Override
    public EvaluationResult evaluate(PerceptionSnapshot perceptionSnapshot, EnvironmentSnapshot environmentSnapshot) {
        return evaluator.evaluate(perceptionSnapshot, environmentSnapshot);
    }

    @Override
    public EvaluationResult evaluate(PerceptionSnapshot perceptionSnapshot, EnvironmentSnapshot environmentSnapshot,
                                    MobGroup group, long currentTick) {
        return evaluator.evaluate(perceptionSnapshot, environmentSnapshot, group, currentTick);
    }

    @Override
    public void react(EvaluationResult decision) {
        reaction.applyReaction(decision, mob.getWorld().getTime());
    }

    @Override
    public void learn(EvaluationResult decision, long currentTick) {
        learning.evaluateOutcome(decision, currentTick);
    }

    @Override
    public void adapt() {
        adaptation.adapt(mob.getWorld().getTime());
    }

    /**
     * Hook called when the mob receives damage.
     */
    public void onDamage(DamageSource source, float amount, Entity attacker) {
        long currentTick = mob.getWorld().getTime();
        learning.onDamageReceived(source, amount, attacker, currentTick);

        // Notify group of attack
        MobGroup group = GroupManager.getInstance().getGroupForMob(mob.getUuid());
        if (group != null) {
            GroupSignal alert = new GroupSignal(
                    GroupSignalType.DANGER_ALERT, mob.getUuid(), mob.getBlockPos(),
                    attacker != null ? attacker.getUuid() : null, 1.5f, currentTick,
                    "Member under attack!"
            );
            group.broadcastSignal(alert, currentTick);
        }
    }

    public MobGroup getGroup() {
        return GroupManager.getInstance().getGroupForMob(mob.getUuid());
    }

    public GroupRole getGroupRole() {
        MobGroup group = getGroup();
        return group != null ? group.getRole(mob.getUuid()) : GroupRole.FOLLOWER;
    }

    // Persistence to/from NBT
    public void writeToNbt(NbtCompound tag) {
        tag.putInt("Version", 2);
        NbtCompound memTag = new NbtCompound();
        memoryBank.writeToNbt(memTag);
        tag.put("Memories", memTag);

        tag.put("Personality", personality.toNbt());

        NbtCompound knowTag = new NbtCompound();
        localKnowledge.writeToNbt(knowTag);
        tag.put("Knowledge", knowTag);

        if (currentEvaluation != null) {
            tag.putString("State", currentEvaluation.getState().name());
        }

        MobGroup currentGroup = getGroup();
        if (currentGroup != null) {
            tag.putUuid("GroupId", currentGroup.getGroupId());
        }
    }

    public void readFromNbt(NbtCompound tag) {
        if (tag.contains("Memories")) {
            memoryBank.readFromNbt(tag.getCompound("Memories"));
        }
        if (tag.contains("Personality")) {
            MobPersonality loaded = MobPersonality.fromNbt(tag.getCompound("Personality"));
            personality.shiftFearfulness(loaded.getFearfulness() - personality.getFearfulness());
            personality.shiftCuriosity(loaded.getCuriosity() - personality.getCuriosity());
            personality.shiftTrustfulness(loaded.getTrustfulness() - personality.getTrustfulness());
            personality.shiftAggressiveness(loaded.getAggressiveness() - personality.getAggressiveness());
            personality.shiftShelterAffinity(loaded.getShelterAffinity() - personality.getShelterAffinity());
            personality.shiftSociability(loaded.getSociability() - personality.getSociability());
            personality.shiftCautiousness(loaded.getCautiousness() - personality.getCautiousness());
        }
        if (tag.contains("Knowledge")) {
            localKnowledge.readFromNbt(tag.getCompound("Knowledge"));
        }
        if (tag.containsUuid("GroupId")) {
            this.savedGroupId = tag.getUuid("GroupId");
        }
    }

    // Getters for subsystems
    public MobEntity getMob() { return mob; }
    public MemoryBank getMemoryBank() { return memoryBank; }
    public MobPersonality getPersonality() { return personality; }
    public PerceptionSubsystem getPerceptionSubsystem() { return perception; }
    public EnvironmentSubsystem getEnvironmentSubsystem() { return environment; }
    public ReactionSubsystem getReactionSubsystem() { return reaction; }
    public LearningSubsystem getLearningSubsystem() { return learning; }
    public AdaptationSubsystem getAdaptationSubsystem() { return adaptation; }
    public LocalWorldKnowledge getLocalKnowledge() { return localKnowledge; }
    public EvaluationResult getCurrentEvaluation() { return currentEvaluation; }
}
