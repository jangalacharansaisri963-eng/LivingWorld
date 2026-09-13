package com.livingworld.behavior;

import com.livingworld.adaptation.MobPersonality;
import com.livingworld.environment.EnvironmentSnapshot;
import com.livingworld.group.GroupDecision;
import com.livingworld.group.GroupRole;
import com.livingworld.group.MobGroup;
import com.livingworld.memory.Memory;
import com.livingworld.memory.MemoryBank;
import com.livingworld.memory.MemoryCategory;
import com.livingworld.perception.PerceivedEntity;
import com.livingworld.perception.PerceptionSnapshot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Optional;

/**
 * Rule-based evaluation engine for Living World V2.
 * Evaluates perceptions, active memories, environmental factors, personality traits,
 * and group consensus to select from 12 contextual behavioral states.
 */
public class BehaviorEvaluator {
    private final MobEntity mob;
    private final MemoryBank memoryBank;
    private final MobPersonality personality;

    private BehaviorState lastState = BehaviorState.CALM;
    private long stateEnteredTick = 0;

    public BehaviorEvaluator(MobEntity mob, MemoryBank memoryBank, MobPersonality personality) {
        this.mob = mob;
        this.memoryBank = memoryBank;
        this.personality = personality;
    }

    public EvaluationResult evaluate(PerceptionSnapshot perception, EnvironmentSnapshot environment) {
        return evaluate(perception, environment, null, 0);
    }

    /**
     * Evaluates current situation including group context and returns the highest priority behavioral decision.
     */
    public EvaluationResult evaluate(PerceptionSnapshot perception, EnvironmentSnapshot environment,
                                    MobGroup group, long currentTick) {
        float healthPct = mob.getHealth() / Math.max(1.0f, mob.getMaxHealth());
        EvaluationResult candidate;

        // RULE 1: Direct Active Threat / Hostile Engagement (Self-preservation overrides all)
        if (mob.getAttacker() != null && mob.getAttacker().isAlive()) {
            if (personality.getAggressiveness() > 0.6f && healthPct > 0.4f) {
                candidate = new EvaluationResult(
                        BehaviorState.DEFEND, 0.98f, mob.getAttacker().getBlockPos(),
                        mob.getAttacker().getUuid(), "Retaliating against active attacker (aggressiveness trait high)"
                );
            } else {
                candidate = new EvaluationResult(
                        BehaviorState.FLEE, 0.98f, mob.getAttacker().getBlockPos(),
                        mob.getAttacker().getUuid(), "Fleeing active attacker to preserve survival"
                );
            }
            return applyHysteresis(candidate, currentTick);
        }

        // RULE 2: Perceived Immediate Threat (Predator, monster, or armed player)
        if (perception.hasImmediateThreat()) {
            PerceivedEntity threat = perception.getPrimaryThreat().orElse(null);
            if (threat != null) {
                if (personality.getAggressiveness() > 0.7f && healthPct > 0.5f && threat.distance() < 8.0) {
                    candidate = new EvaluationResult(
                            BehaviorState.DEFEND, 0.92f, BlockPos.ofFloored(threat.getPosition()),
                            threat.getUuid(), "Defending against nearby threat " + threat.getType().getUntranslatedName()
                    );
                } else {
                    candidate = new EvaluationResult(
                            BehaviorState.FLEE, 0.90f, BlockPos.ofFloored(threat.getPosition()),
                            threat.getUuid(), "Fleeing from perceived high threat " + threat.getType().getUntranslatedName()
                    );
                }
                return applyHysteresis(candidate, currentTick);
            }
        }

        // RULE 3: Group Consensus Integration (Herd response)
        if (group != null && group.getCurrentDecision() != null) {
            GroupDecision groupDec = group.getCurrentDecision();
            GroupRole role = group.getRole(mob.getUuid());

            switch (groupDec.getType()) {
                case FLEE -> {
                    candidate = new EvaluationResult(
                            BehaviorState.FLEE, 0.88f, groupDec.getTargetLocation(), groupDec.getTargetEntityUuid(),
                            "Responding to group flight command: " + groupDec.getReason(), true
                    );
                    return applyHysteresis(candidate, currentTick);
                }
                case DEFEND -> {
                    if (role == GroupRole.DEFENDER || personality.getAggressiveness() > 0.6f) {
                        candidate = new EvaluationResult(
                                BehaviorState.DEFEND, 0.86f, groupDec.getTargetLocation(), groupDec.getTargetEntityUuid(),
                                "Rallying as group defender to engage threat", true
                        );
                        return applyHysteresis(candidate, currentTick);
                    } else {
                        // Followers avoid danger area
                        candidate = new EvaluationResult(
                                BehaviorState.AVOID, 0.78f, groupDec.getTargetLocation(), null,
                                "Follower staying clear of group engagement area", true
                        );
                        return applyHysteresis(candidate, currentTick);
                    }
                }
                case SEEK_SHELTER -> {
                    if (environment.isHarshWeather() || environment.isNight()) {
                        candidate = new EvaluationResult(
                                BehaviorState.SEEK_SHELTER, 0.84f, groupDec.getTargetLocation(), null,
                                "Moving with group to shared shelter", true
                        );
                        return applyHysteresis(candidate, currentTick);
                    }
                }
                case SEARCH_FOOD -> {
                    if (groupDec.getTargetLocation() != null) {
                        candidate = new EvaluationResult(
                                BehaviorState.SEARCH_FOOD, 0.65f, groupDec.getTargetLocation(), null,
                                "Moving with herd towards shared food source", true
                        );
                        return applyHysteresis(candidate, currentTick);
                    }
                }
                case FOLLOW_LEADER -> {
                    if (role != GroupRole.LEADER && groupDec.getTargetEntityUuid() != null) {
                        candidate = new EvaluationResult(
                                BehaviorState.FOLLOW, 0.55f, groupDec.getTargetLocation(), groupDec.getTargetEntityUuid(),
                                "Following herd leader in coordinated march", true
                        );
                        return applyHysteresis(candidate, currentTick);
                    }
                }
                default -> {}
            }
        }

        // RULE 4: Dangerous Memory Proximity / Avoidance
        Optional<Memory> dangerMemory = memoryBank.getStrongestMemory(MemoryCategory.DANGER);
        if (dangerMemory.isPresent()) {
            Memory danger = dangerMemory.get();
            if (danger.getLocation() != null && danger.getLocation().isWithinDistance(mob.getBlockPos(), 16.0)) {
                if (personality.getFearfulness() > 0.5f || personality.getCautiousness() > 0.5f) {
                    candidate = new EvaluationResult(
                            BehaviorState.AVOID, 0.75f, danger.getLocation(), null,
                            "Steering clear of remembered danger zone (" + danger.getSourceContext() + ")"
                    );
                    return applyHysteresis(candidate, currentTick);
                }
            }
        }

        // RULE 5: Harsh Weather & Shelter Seeking
        if (environment.isHarshWeather() || (environment.isNight() && personality.getShelterAffinity() > 0.6f)) {
            if (environment.getCurrentShelterScore() < 0.45f) {
                Optional<Memory> shelterMem = memoryBank.getClosestMemory(MemoryCategory.SHELTER, mob.getBlockPos());
                if (shelterMem.isPresent() && shelterMem.get().getLocation() != null) {
                    candidate = new EvaluationResult(
                            BehaviorState.SEEK_SHELTER, 0.76f, shelterMem.get().getLocation(), null,
                            "Heading to remembered shelter during inclement weather"
                    );
                } else {
                    candidate = new EvaluationResult(
                            BehaviorState.SEEK_SHELTER, 0.70f, null, null,
                            "Searching for overhead cover due to harsh weather"
                    );
                }
                return applyHysteresis(candidate, currentTick);
            }
        }

        // RULE 6: Food Discovery & Seeking
        if (perception.hasFood()) {
            BlockPos foodPos = perception.getFoodLocations().get(0);
            candidate = new EvaluationResult(
                    BehaviorState.SEARCH_FOOD, 0.62f, foodPos, null,
                    "Spotted edible food resource on ground"
            );
            return applyHysteresis(candidate, currentTick);
        }

        // RULE 7: Player Encounter & Curiosity
        if (perception.hasPlayers()) {
            PerceivedEntity nearestPlayer = perception.getPlayers().get(0);
            Optional<Memory> playerMemory = memoryBank.getMemories(MemoryCategory.PLAYER).stream()
                    .filter(m -> m.getSourceContext().equals(nearestPlayer.getUuid().toString()))
                    .findFirst();

            if (playerMemory.isPresent()) {
                Memory mem = playerMemory.get();
                if (mem.getConfidence() > 0.6f && personality.getTrustfulness() > 0.4f) {
                    candidate = new EvaluationResult(
                            BehaviorState.CURIOUS, 0.52f, BlockPos.ofFloored(nearestPlayer.getPosition()),
                            nearestPlayer.getUuid(), "Approaching recognized familiar player"
                    );
                    return applyHysteresis(candidate, currentTick);
                }
            } else if (personality.getCuriosity() > 0.5f && nearestPlayer.distance() < 12.0) {
                candidate = new EvaluationResult(
                        BehaviorState.INVESTIGATE, 0.48f, BlockPos.ofFloored(nearestPlayer.getPosition()),
                        nearestPlayer.getUuid(), "Investigating unfamiliar player presence"
                );
                return applyHysteresis(candidate, currentTick);
            } else {
                candidate = new EvaluationResult(
                        BehaviorState.ALERT, 0.44f, BlockPos.ofFloored(nearestPlayer.getPosition()),
                        nearestPlayer.getUuid(), "Monitoring unfamiliar player at safe distance"
                );
                return applyHysteresis(candidate, currentTick);
            }
        }

        // RULE 8: Exploration vs Calm
        if (personality.getCuriosity() > 0.65f && Math.random() < 0.25) {
            candidate = new EvaluationResult(
                    BehaviorState.EXPLORE, 0.35f, mob.getBlockPos(), null,
                    "Curiously scouting perimeter and novel terrain"
            );
            return applyHysteresis(candidate, currentTick);
        }

        // RULE 9: Default Calm
        candidate = EvaluationResult.calm("Peaceful surroundings, no acute stimuli detected");
        return applyHysteresis(candidate, currentTick);
    }

    /**
     * Prevents rapid state oscillation using hysteresis thresholds and minimum dwell time.
     */
    private EvaluationResult applyHysteresis(EvaluationResult candidate, long currentTick) {
        if (candidate.getState() == lastState) {
            return candidate;
        }

        // Urgent states (FLEE, DEFEND) always transition immediately
        if (candidate.getState() == BehaviorState.FLEE || candidate.getState() == BehaviorState.DEFEND) {
            this.lastState = candidate.getState();
            this.stateEnteredTick = currentTick;
            return candidate;
        }

        // Prevent oscillation: dwell time of at least 40 ticks (2 seconds)
        long dwellTicks = currentTick - stateEnteredTick;
        if (dwellTicks < 40 && candidate.getScore() < 0.70f) {
            // Retain previous state with original purpose
            return new EvaluationResult(
                    lastState, candidate.getScore(), candidate.getTargetLocation(),
                    candidate.getTargetEntityUuid(), candidate.getReason() + " (State stabilized)"
            );
        }

        this.lastState = candidate.getState();
        this.stateEnteredTick = currentTick;
        return candidate;
    }
}

