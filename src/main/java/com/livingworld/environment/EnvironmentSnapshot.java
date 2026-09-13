package com.livingworld.environment;

/**
 * Structured snapshot of the ambient environmental conditions around a mob.
 */
public class EnvironmentSnapshot {
    private final long timeOfDay;
    private final boolean isDay;
    private final boolean isNight;
    private final boolean isRaining;
    private final boolean isThundering;
    private final int totalLight;
    private final int skyLight;
    private final int blockLight;
    private final String biomeKey;
    private final float currentShelterScore;
    private final boolean inWater;

    public EnvironmentSnapshot(long timeOfDay, boolean isDay, boolean isNight,
                               boolean isRaining, boolean isThundering,
                               int totalLight, int skyLight, int blockLight,
                               String biomeKey, float currentShelterScore, boolean inWater) {
        this.timeOfDay = timeOfDay;
        this.isDay = isDay;
        this.isNight = isNight;
        this.isRaining = isRaining;
        this.isThundering = isThundering;
        this.totalLight = totalLight;
        this.skyLight = skyLight;
        this.blockLight = blockLight;
        this.biomeKey = biomeKey;
        this.currentShelterScore = currentShelterScore;
        this.inWater = inWater;
    }

    public long getTimeOfDay() { return timeOfDay; }
    public boolean isDay() { return isDay; }
    public boolean isNight() { return isNight; }
    public boolean isRaining() { return isRaining; }
    public boolean isThundering() { return isThundering; }
    public int getTotalLight() { return totalLight; }
    public int getSkyLight() { return skyLight; }
    public int getBlockLight() { return blockLight; }
    public String getBiomeKey() { return biomeKey; }
    public float getCurrentShelterScore() { return currentShelterScore; }
    public boolean isInWater() { return inWater; }

    public boolean isHarshWeather() {
        return isThundering || (isRaining && currentShelterScore < 0.4f);
    }

    public boolean isPitchDark() {
        return totalLight <= 3;
    }
}
