package com.livingworld.learning;

import com.livingworld.adaptation.MobPersonality;
import com.livingworld.behavior.BehaviorState;
import com.livingworld.behavior.EvaluationResult;
import com.livingworld.config.ConfigManager;
import com.livingworld.config.LivingWorldConfig;
import com.livingworld.memory.Memory;
import com.livingworld.memory.MemoryBank;
import com.livingworld.memory.MemoryCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

/**
 * Deterministic rule-based learning subsystem for Living World V1.
 * Updates memories, associates locations with danger or safety, and tracks player interactions.
 */
public class LearningSubsystem {
    private final MobEntity mob;
    private final MemoryBank memoryBank;
    private final MobPersonality personality;

    public LearningSubsystem(MobEntity mob, MemoryBank memoryBank, MobPersonality personality) {
        this.mob = mob;
        this.memoryBank = memoryBank;
        this.personality = personality;
    }

    /**
     * Learns from damage taken.
     * Records a danger memory at the location and associates threat with the attacker.
     */
    public void onDamageReceived(DamageSource source, float amount, Entity attacker, long currentTick) {
        LivingWorldConfig config = ConfigManager.get();
        BlockPos damagePos = mob.getBlockPos();

        float importance = Math.min(1.5f, 0.5f + (amount / 10.0f)) * config.learningStrength;
        String sourceContext = attacker != null ? attacker.getType().getUntranslatedName() : source.getName();

        Memory dangerMemory = new Memory(
                MemoryCategory.DANGER, damagePos, currentTick, importance, 0.9f, sourceContext
        );
        memoryBank.remember(dangerMemory, currentTick);

        // If attacked by player, record hostile player encounter
        if (attacker instanceof PlayerEntity player) {
            Memory playerMem = new Memory(
                    MemoryCategory.PLAYER, player.getBlockPos(), currentTick,
                    0.8f, 0.95f, player.getUuid().toString()
            );
            playerMem.getExtraData().putBoolean("Hostile", true);
            memoryBank.remember(playerMem, currentTick);

            // Shift personality towards fear and away from trust
            personality.shiftFearfulness(0.08f);
            personality.shiftTrustfulness(-0.15f);
        }
    }

    /**
     * Learns from a successful retreat.
     * Marks the retreat location as a safe haven in memory.
     */
    public void onFleeSuccessful(BlockPos safePos, long currentTick) {
        if (safePos == null) return;
        Memory refugeMem = new Memory(
                MemoryCategory.LOCATION, safePos, currentTick,
                0.7f, 0.8f, "Safe Refuge"
        );
        memoryBank.remember(refugeMem, currentTick);
    }

    /**
     * Learns from finding and utilizing shelter.
     * Reinforces the shelter's location and utility.
     */
    public void onShelterDiscovered(BlockPos shelterPos, float quality, long currentTick) {
        if (shelterPos == null) return;
        Memory shelterMem = new Memory(
                MemoryCategory.SHELTER, shelterPos, currentTick,
                0.6f + (quality * 0.4f), 0.85f, "Discovered Shelter"
        );
        memoryBank.remember(shelterMem, currentTick);
        personality.shiftShelterAffinity(0.02f);
    }

    /**
     * Learns from discovering food or grazing grounds.
     */
    public void onFoodDiscovered(BlockPos foodPos, long currentTick) {
        if (foodPos == null) return;
        Memory foodMem = new Memory(
                MemoryCategory.FOOD, foodPos, currentTick,
                0.65f, 0.9f, "Food Source"
        );
        memoryBank.remember(foodMem, currentTick);
    }

    /**
     * Learns from peaceful encounters with players (e.g. proximity without harm).
     */
    public void onPeacefulPlayerEncounter(PlayerEntity player, long currentTick) {
        Memory playerMem = new Memory(
                MemoryCategory.PLAYER, player.getBlockPos(), currentTick,
                0.5f, 0.7f, player.getUuid().toString()
        );
        playerMem.getExtraData().putBoolean("Peaceful", true);
        memoryBank.remember(playerMem, currentTick);

        // Gradual increase in trust
        personality.shiftTrustfulness(0.03f);
    }

    /**
     * Evaluates recent decision outcomes during pipeline execution.
     */
    public void evaluateOutcome(EvaluationResult decision, long currentTick) {
        if (decision == null) return;

        if (decision.getState() == BehaviorState.SEEK_SHELTER && decision.getTargetLocation() != null) {
            onShelterDiscovered(decision.getTargetLocation(), 0.7f, currentTick);
        } else if (decision.getState() == BehaviorState.INVESTIGATE && decision.getTargetLocation() != null) {
            if (decision.getReason().contains("food")) {
                onFoodDiscovered(decision.getTargetLocation(), currentTick);
            }
        }
    }
}
