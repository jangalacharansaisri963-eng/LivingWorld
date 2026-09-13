package com.livingworld.memory;

/**
 * Categories of memories tracked by intelligent mobs in Living World V1.
 */
public enum MemoryCategory {
    DANGER(1.5f, 0.001f),           // High importance, slower decay
    PLAYER(1.2f, 0.0015f),          // Moderate-high importance
    FOOD(1.0f, 0.003f),             // Standard importance, moderate decay
    SHELTER(1.3f, 0.001f),          // High importance, slow decay
    LOCATION(0.9f, 0.002f),         // Spatial point of interest
    ENVIRONMENTAL_EVENT(1.1f, 0.002f), // Weather shifts, thunder strikes, explosions
    ACTION_OUTCOME(1.0f, 0.0025f),  // Successful escapes, failed forage attempts
    GROUP_EXPERIENCE(1.2f, 0.0015f), // Shared group memories, coordinated movements
    GENERAL_EXPERIENCE(0.8f, 0.004f); // Broad sensory impressions, faster decay

    private final float baseImportanceWeight;
    private final float baseDecayRate;

    MemoryCategory(float baseImportanceWeight, float baseDecayRate) {
        this.baseImportanceWeight = baseImportanceWeight;
        this.baseDecayRate = baseDecayRate;
    }

    public float getBaseImportanceWeight() {
        return baseImportanceWeight;
    }

    public float getBaseDecayRate() {
        return baseDecayRate;
    }
}
