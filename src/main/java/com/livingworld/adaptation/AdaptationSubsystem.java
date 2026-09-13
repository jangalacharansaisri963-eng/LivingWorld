package com.livingworld.adaptation;

import com.livingworld.config.ConfigManager;
import com.livingworld.config.LivingWorldConfig;
import com.livingworld.memory.Memory;
import com.livingworld.memory.MemoryBank;
import com.livingworld.memory.MemoryCategory;

/**
 * Subsystem responsible for behavioral adaptation over time.
 * Shifts mob personality traits based on memory composition and lived experiences.
 */
public class AdaptationSubsystem {
    private final MobPersonality personality;
    private final MemoryBank memoryBank;
    private long lastAdaptationTick = 0;

    public AdaptationSubsystem(MobPersonality personality, MemoryBank memoryBank) {
        this.personality = personality;
        this.memoryBank = memoryBank;
    }

    /**
     * Periodically called to evaluate overall life experience and adjust behavioral traits.
     */
    public void adapt(long currentTick) {
        LivingWorldConfig config = ConfigManager.get();
        // Run adaptation check every 400 ticks (20 seconds)
        if (currentTick - lastAdaptationTick < 400) {
            return;
        }
        lastAdaptationTick = currentTick;

        float rate = config.adaptationRate;

        // Factor 1: Traumatic danger accumulation increases fearfulness and cautiousness
        int dangerCount = memoryBank.getMemories(MemoryCategory.DANGER).size();
        if (dangerCount >= 3) {
            personality.shiftFearfulness(rate);
            personality.shiftCautiousness(rate * 0.8f);
            personality.shiftTrustfulness(-rate * 0.5f);
        } else if (dangerCount == 0) {
            // Calm conditions gradually lower extreme fear
            personality.shiftFearfulness(-rate * 0.25f);
        }

        // Factor 2: Shelter experience reinforces shelter affinity
        int shelterCount = memoryBank.getMemories(MemoryCategory.SHELTER).size();
        if (shelterCount > 0) {
            personality.shiftShelterAffinity(rate * 0.5f);
        }

        // Factor 3: Player interactions
        for (Memory playerMem : memoryBank.getMemories(MemoryCategory.PLAYER)) {
            if (playerMem.getConfidence() > 0.6f) {
                // Familiar player presence increases trust and reduces fear
                personality.shiftTrustfulness(rate * 0.5f);
            }
        }

        // Factor 4: Group experience reinforces sociability
        int groupMemCount = memoryBank.getMemories(MemoryCategory.GROUP_EXPERIENCE).size();
        if (groupMemCount > 0) {
            personality.shiftSociability(rate * 0.6f);
        }
    }

    public MobPersonality getPersonality() {
        return personality;
    }
}
