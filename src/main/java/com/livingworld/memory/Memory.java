package com.livingworld.memory;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;
import java.util.UUID;

/**
 * Encapsulates a contextual, bounded memory in Living World V1.
 * Supports decay, reinforcement, confidence scaling, and NBT serialization.
 */
public class Memory {
    private final UUID id;
    private final MemoryCategory category;
    private BlockPos location;
    private final long creationTick;
    private long lastReinforcedTick;
    private float importance; // 0.0 to 1.0 (or higher with reinforcement)
    private float confidence; // 0.0 to 1.0
    private int reinforcementCount;
    private String sourceContext;
    private NbtCompound extraData;

    public Memory(UUID id, MemoryCategory category, BlockPos location, long currentTick,
                  float importance, float confidence, String sourceContext) {
        this.id = id != null ? id : UUID.randomUUID();
        this.category = Objects.requireNonNull(category, "category cannot be null");
        this.location = location;
        this.creationTick = currentTick;
        this.lastReinforcedTick = currentTick;
        this.importance = Math.max(0.0f, Math.min(2.0f, importance));
        this.confidence = Math.max(0.0f, Math.min(1.0f, confidence));
        this.reinforcementCount = 1;
        this.sourceContext = sourceContext != null ? sourceContext : "";
        this.extraData = new NbtCompound();
    }

    public Memory(MemoryCategory category, BlockPos location, long currentTick,
                  float importance, float confidence, String sourceContext) {
        this(UUID.randomUUID(), category, location, currentTick, importance, confidence, sourceContext);
    }

    /**
     * Reinforces this memory through repeated exposure or corroborating events.
     */
    public void reinforce(float importanceBoost, float confidenceBoost, long currentTick) {
        this.reinforcementCount++;
        this.lastReinforcedTick = currentTick;
        this.importance = Math.min(2.0f, this.importance + importanceBoost);
        this.confidence = Math.min(1.0f, this.confidence + confidenceBoost);
    }

    /**
     * Applies decay over elapsed time.
     * High importance and frequent reinforcement mitigate decay.
     */
    public void decay(long currentTick, float globalDecayMultiplier) {
        long elapsed = Math.max(0L, currentTick - this.lastReinforcedTick);
        if (elapsed <= 0) return;

        float effectiveDecay = category.getBaseDecayRate() * globalDecayMultiplier;
        // Reinforcement buffer: each reinforcement reduces decay by 15% (up to 75% max protection)
        float protectionFactor = 1.0f - Math.min(0.75f, reinforcementCount * 0.15f);
        float decayAmount = effectiveDecay * protectionFactor * (elapsed / 20.0f);

        this.confidence = Math.max(0.0f, this.confidence - decayAmount);
        this.importance = Math.max(0.0f, this.importance - (decayAmount * 0.5f));
    }

    /**
     * Computes the retention weight used for eviction and decision priority.
     */
    public float getRetentionWeight() {
        return (importance * category.getBaseImportanceWeight()) * (0.3f + 0.7f * confidence);
    }

    public boolean isExpired() {
        return confidence <= 0.05f || importance <= 0.05f;
    }

    public long getAge(long currentTick) {
        return Math.max(0, currentTick - creationTick);
    }

    public boolean isShared() {
        return extraData.getBoolean("Shared");
    }

    public void setShared(boolean shared) {
        extraData.putBoolean("Shared", shared);
    }

    public boolean isActionSuccess() {
        return extraData.getBoolean("Success");
    }

    public void setActionSuccess(boolean success) {
        extraData.putBoolean("Success", success);
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public MemoryCategory getCategory() { return category; }
    public BlockPos getLocation() { return location; }
    public void setLocation(BlockPos location) { this.location = location; }
    public long getCreationTick() { return creationTick; }
    public long getLastReinforcedTick() { return lastReinforcedTick; }
    public float getImportance() { return importance; }
    public float getConfidence() { return confidence; }
    public int getReinforcementCount() { return reinforcementCount; }
    public String getSourceContext() { return sourceContext; }
    public void setSourceContext(String sourceContext) { this.sourceContext = sourceContext; }
    public NbtCompound getExtraData() { return extraData; }
    public void setExtraData(NbtCompound extraData) { this.extraData = extraData != null ? extraData : new NbtCompound(); }

    /**
     * Serializes memory to NBT compound.
     */
    public NbtCompound toNbt() {
        NbtCompound tag = new NbtCompound();
        tag.putUuid("Id", id);
        tag.putString("Category", category.name());
        if (location != null) {
            tag.putInt("LocX", location.getX());
            tag.putInt("LocY", location.getY());
            tag.putInt("LocZ", location.getZ());
        }
        tag.putLong("CreationTick", creationTick);
        tag.putLong("LastReinforcedTick", lastReinforcedTick);
        tag.putFloat("Importance", importance);
        tag.putFloat("Confidence", confidence);
        tag.putInt("ReinforcementCount", reinforcementCount);
        tag.putString("SourceContext", sourceContext);
        if (!extraData.isEmpty()) {
            tag.put("Extra", extraData);
        }
        return tag;
    }

    /**
     * Deserializes memory from NBT compound.
     */
    public static Memory fromNbt(NbtCompound tag) {
        UUID id = tag.containsUuid("Id") ? tag.getUuid("Id") : UUID.randomUUID();
        String catName = tag.getString("Category");
        MemoryCategory category;
        try {
            category = MemoryCategory.valueOf(catName);
        } catch (IllegalArgumentException e) {
            category = MemoryCategory.GENERAL_EXPERIENCE;
        }

        BlockPos pos = null;
        if (tag.contains("LocX") && tag.contains("LocY") && tag.contains("LocZ")) {
            pos = new BlockPos(tag.getInt("LocX"), tag.getInt("LocY"), tag.getInt("LocZ"));
        }

        long creationTick = tag.getLong("CreationTick");
        float importance = tag.getFloat("Importance");
        float confidence = tag.getFloat("Confidence");
        String context = tag.getString("SourceContext");

        Memory memory = new Memory(id, category, pos, creationTick, importance, confidence, context);
        memory.lastReinforcedTick = tag.getLong("LastReinforcedTick");
        memory.reinforcementCount = Math.max(1, tag.getInt("ReinforcementCount"));
        if (tag.contains("Extra")) {
            memory.extraData = tag.getCompound("Extra");
        }
        return memory;
    }

    @Override
    public String toString() {
        return String.format("Memory[%s, cat=%s, imp=%.2f, conf=%.2f, reinf=%d, pos=%s]",
                id.toString().substring(0, 8), category, importance, confidence, reinforcementCount, location);
    }
}
