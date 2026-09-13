package com.livingworld.config;

/**
 * Configuration options for Living World V1.
 * Controls tick intervals, limits, ranges, decay, and debug settings.
 */
public class LivingWorldConfig {
    // Execution Intervals (in game ticks)
    public int intelligenceTickInterval = 10;
    public int perceptionScanInterval = 20;
    public int memoryDecayInterval = 100;

    // Memory Boundaries
    public int maxMemoriesPerCategory = 16;
    public int maxTotalMemories = 64;
    public float baseMemoryDecayRate = 0.002f;
    public float memoryReinforcementBoost = 0.25f;

    // Sensory & Perception Ranges (in blocks)
    public double perceptionHorizontalRange = 24.0;
    public double perceptionVerticalRange = 10.0;
    public double threatDetectionRange = 18.0;
    public double shelterSearchRadius = 16.0;

    // Behavioral & Learning Factors
    public float learningStrength = 1.0f;
    public float adaptationRate = 0.05f;

    // Knowledge Limits
    public int maxKnownLocations = 20;

    // V2 Group Intelligence Settings
    public boolean enableGroupIntelligence = true;
    public double groupSearchRadius = 16.0;
    public double groupSeparationDistance = 28.0;
    public int maxGroupSize = 8;
    public int groupTickInterval = 20;
    public double groupCommunicationRange = 32.0;
    public int maxGroupMemories = 32;
    public float groupCohesionWeight = 0.6f;
    public int groupLeadershipEvaluationInterval = 100;

    // Debugging
    public boolean debugMode = false;
    public boolean logEvaluations = false;
}
