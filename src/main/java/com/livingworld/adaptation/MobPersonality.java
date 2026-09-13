package com.livingworld.adaptation;

import net.minecraft.nbt.NbtCompound;

import java.util.Random;

/**
 * Individual behavioral traits and tendencies for an intelligent mob.
 * Dynamically shifts over time through adaptation and experience.
 */
public class MobPersonality {
    private float fearfulness;     // Tendency to flee vs stand ground
    private float curiosity;       // Tendency to investigate stimuli and new entities
    private float trustfulness;    // Tendency to tolerate or approach players
    private float aggressiveness;  // Tendency to defend or attack when provoked
    private float shelterAffinity; // Urgency to seek shelter during adverse weather
    private float sociability;     // Tendency to seek and coordinate in groups
    private float cautiousness;    // Preference for familiar safe paths and cautious posture

    public MobPersonality() {
        Random random = new Random();
        this.fearfulness = 0.4f + (random.nextFloat() * 0.3f);     // 0.40 - 0.70
        this.curiosity = 0.3f + (random.nextFloat() * 0.4f);       // 0.30 - 0.70
        this.trustfulness = 0.2f + (random.nextFloat() * 0.3f);    // 0.20 - 0.50
        this.aggressiveness = 0.2f + (random.nextFloat() * 0.3f);  // 0.20 - 0.50
        this.shelterAffinity = 0.4f + (random.nextFloat() * 0.3f); // 0.40 - 0.70
        this.sociability = 0.4f + (random.nextFloat() * 0.4f);     // 0.40 - 0.80
        this.cautiousness = 0.35f + (random.nextFloat() * 0.35f);  // 0.35 - 0.70
    }

    public MobPersonality(float fearfulness, float curiosity, float trustfulness,
                          float aggressiveness, float shelterAffinity) {
        this(fearfulness, curiosity, trustfulness, aggressiveness, shelterAffinity, 0.5f, 0.5f);
    }

    public MobPersonality(float fearfulness, float curiosity, float trustfulness,
                          float aggressiveness, float shelterAffinity,
                          float sociability, float cautiousness) {
        this.fearfulness = clamp(fearfulness);
        this.curiosity = clamp(curiosity);
        this.trustfulness = clamp(trustfulness);
        this.aggressiveness = clamp(aggressiveness);
        this.shelterAffinity = clamp(shelterAffinity);
        this.sociability = clamp(sociability);
        this.cautiousness = clamp(cautiousness);
    }

    public void shiftFearfulness(float delta) {
        this.fearfulness = clamp(this.fearfulness + delta);
    }

    public void shiftCuriosity(float delta) {
        this.curiosity = clamp(this.curiosity + delta);
    }

    public void shiftTrustfulness(float delta) {
        this.trustfulness = clamp(this.trustfulness + delta);
    }

    public void shiftAggressiveness(float delta) {
        this.aggressiveness = clamp(this.aggressiveness + delta);
    }

    public void shiftShelterAffinity(float delta) {
        this.shelterAffinity = clamp(this.shelterAffinity + delta);
    }

    public void shiftSociability(float delta) {
        this.sociability = clamp(this.sociability + delta);
    }

    public void shiftCautiousness(float delta) {
        this.cautiousness = clamp(this.cautiousness + delta);
    }

    private static float clamp(float val) {
        return Math.max(0.05f, Math.min(0.95f, val));
    }

    public float getFearfulness() { return fearfulness; }
    public float getCuriosity() { return curiosity; }
    public float getTrustfulness() { return trustfulness; }
    public float getAggressiveness() { return aggressiveness; }
    public float getShelterAffinity() { return shelterAffinity; }
    public float getSociability() { return sociability; }
    public float getCautiousness() { return cautiousness; }

    public NbtCompound toNbt() {
        NbtCompound tag = new NbtCompound();
        tag.putFloat("Fear", fearfulness);
        tag.putFloat("Curiosity", curiosity);
        tag.putFloat("Trust", trustfulness);
        tag.putFloat("Aggression", aggressiveness);
        tag.putFloat("ShelterAffinity", shelterAffinity);
        tag.putFloat("Sociability", sociability);
        tag.putFloat("Cautiousness", cautiousness);
        return tag;
    }

    public static MobPersonality fromNbt(NbtCompound tag) {
        float fear = tag.getFloat("Fear");
        float curiosity = tag.getFloat("Curiosity");
        float trust = tag.getFloat("Trust");
        float aggression = tag.getFloat("Aggression");
        float shelter = tag.getFloat("ShelterAffinity");
        float soc = tag.contains("Sociability") ? tag.getFloat("Sociability") : 0.5f;
        float caut = tag.contains("Cautiousness") ? tag.getFloat("Cautiousness") : 0.5f;
        return new MobPersonality(fear, curiosity, trust, aggression, shelter, soc, caut);
    }

    @Override
    public String toString() {
        return String.format("Personality[fear=%.2f, cur=%.2f, trust=%.2f, agg=%.2f, shelter=%.2f, soc=%.2f, caut=%.2f]",
                fearfulness, curiosity, trustfulness, aggressiveness, shelterAffinity, sociability, cautiousness);
    }
}
