package com.livingworld.group;

import com.livingworld.memory.MemoryCategory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;
import java.util.UUID;

/**
 * Knowledge entity shared among members of a MobGroup.
 * Aggregates discoveries from individual members with bounded decay and reinforcement.
 */
public class SharedMemory {
    private final UUID id;
    private final MemoryCategory category;
    private BlockPos location;
    private final UUID sourceMobUuid;
    private final long creationTick;
    private long lastReinforcedTick;
    private float importance;
    private float confidence;
    private int reinforcementCount;
    private String context;

    public SharedMemory(UUID id, MemoryCategory category, BlockPos location, UUID sourceMobUuid,
                        long creationTick, float importance, float confidence, String context) {
        this.id = id != null ? id : UUID.randomUUID();
        this.category = Objects.requireNonNull(category, "category cannot be null");
        this.location = location;
        this.sourceMobUuid = sourceMobUuid;
        this.creationTick = creationTick;
        this.lastReinforcedTick = creationTick;
        this.importance = Math.max(0.0f, Math.min(2.0f, importance));
        this.confidence = Math.max(0.0f, Math.min(1.0f, confidence));
        this.reinforcementCount = 1;
        this.context = context != null ? context : "";
    }

    public SharedMemory(MemoryCategory category, BlockPos location, UUID sourceMobUuid,
                        long creationTick, float importance, float confidence, String context) {
        this(UUID.randomUUID(), category, location, sourceMobUuid, creationTick, importance, confidence, context);
    }

    public void reinforce(float importanceBoost, float confidenceBoost, long currentTick) {
        this.reinforcementCount++;
        this.lastReinforcedTick = currentTick;
        this.importance = Math.min(2.0f, this.importance + importanceBoost);
        this.confidence = Math.min(1.0f, this.confidence + confidenceBoost);
    }

    public void decay(long currentTick, float baseDecayRate) {
        long elapsed = Math.max(0L, currentTick - this.lastReinforcedTick);
        if (elapsed <= 0) return;

        float effectiveDecay = category.getBaseDecayRate() * baseDecayRate;
        float protection = 1.0f - Math.min(0.70f, reinforcementCount * 0.12f);
        float decayAmount = effectiveDecay * protection * (elapsed / 20.0f);

        this.confidence = Math.max(0.0f, this.confidence - decayAmount);
        this.importance = Math.max(0.0f, this.importance - (decayAmount * 0.4f));
    }

    public float getRetentionWeight() {
        return (importance * category.getBaseImportanceWeight()) * (0.4f + 0.6f * confidence);
    }

    public boolean isExpired() {
        return confidence <= 0.05f || importance <= 0.05f;
    }

    public UUID getId() { return id; }
    public MemoryCategory getCategory() { return category; }
    public BlockPos getLocation() { return location; }
    public void setLocation(BlockPos location) { this.location = location; }
    public UUID getSourceMobUuid() { return sourceMobUuid; }
    public long getCreationTick() { return creationTick; }
    public long getLastReinforcedTick() { return lastReinforcedTick; }
    public float getImportance() { return importance; }
    public float getConfidence() { return confidence; }
    public int getReinforcementCount() { return reinforcementCount; }
    public String getContext() { return context; }

    public NbtCompound toNbt() {
        NbtCompound tag = new NbtCompound();
        tag.putUuid("Id", id);
        tag.putString("Category", category.name());
        if (location != null) {
            tag.putInt("LocX", location.getX());
            tag.putInt("LocY", location.getY());
            tag.putInt("LocZ", location.getZ());
        }
        if (sourceMobUuid != null) {
            tag.putUuid("SourceMob", sourceMobUuid);
        }
        tag.putLong("CreationTick", creationTick);
        tag.putLong("LastReinforcedTick", lastReinforcedTick);
        tag.putFloat("Importance", importance);
        tag.putFloat("Confidence", confidence);
        tag.putInt("ReinforcementCount", reinforcementCount);
        tag.putString("Context", context);
        return tag;
    }

    public static SharedMemory fromNbt(NbtCompound tag) {
        UUID id = tag.containsUuid("Id") ? tag.getUuid("Id") : UUID.randomUUID();
        String catStr = tag.getString("Category");
        MemoryCategory cat;
        try {
            cat = MemoryCategory.valueOf(catStr);
        } catch (IllegalArgumentException e) {
            cat = MemoryCategory.GENERAL_EXPERIENCE;
        }

        BlockPos pos = null;
        if (tag.contains("LocX") && tag.contains("LocY") && tag.contains("LocZ")) {
            pos = new BlockPos(tag.getInt("LocX"), tag.getInt("LocY"), tag.getInt("LocZ"));
        }

        UUID sourceMob = tag.containsUuid("SourceMob") ? tag.getUuid("SourceMob") : null;
        long creation = tag.getLong("CreationTick");
        float imp = tag.getFloat("Importance");
        float conf = tag.getFloat("Confidence");
        String ctx = tag.getString("Context");

        SharedMemory sm = new SharedMemory(id, cat, pos, sourceMob, creation, imp, conf, ctx);
        sm.lastReinforcedTick = tag.getLong("LastReinforcedTick");
        sm.reinforcementCount = Math.max(1, tag.getInt("ReinforcementCount"));
        return sm;
    }

    @Override
    public String toString() {
        return String.format("SharedMemory[%s, cat=%s, imp=%.2f, conf=%.2f, pos=%s]",
                id.toString().substring(0, 8), category, importance, confidence, location);
    }
}
