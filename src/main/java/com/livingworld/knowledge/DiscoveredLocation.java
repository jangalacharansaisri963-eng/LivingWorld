package com.livingworld.knowledge;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

/**
 * Represents a recognized point of interest in the mob's local knowledge.
 */
public class DiscoveredLocation {
    public enum Type {
        SHELTER,
        DANGER_ZONE,
        FEEDING_GROUND,
        WATER_SOURCE,
        LANDMARK,
        SAFE_REFUGE,
        FREQUENT_ROUTE,
        GROUP_GATHERING
    }

    private final Type type;
    private final BlockPos pos;
    private final long discoveredTick;
    private int visits;
    private float utilityScore; // 0.0 to 1.0

    public DiscoveredLocation(Type type, BlockPos pos, long discoveredTick, float utilityScore) {
        this.type = type;
        this.pos = pos;
        this.discoveredTick = discoveredTick;
        this.visits = 1;
        this.utilityScore = utilityScore;
    }

    public void recordVisit() {
        this.visits++;
        this.utilityScore = Math.min(1.0f, this.utilityScore + 0.05f);
    }

    public Type getType() { return type; }
    public BlockPos getPos() { return pos; }
    public long getDiscoveredTick() { return discoveredTick; }
    public int getVisits() { return visits; }
    public float getUtilityScore() { return utilityScore; }

    public NbtCompound toNbt() {
        NbtCompound tag = new NbtCompound();
        tag.putString("Type", type.name());
        tag.putInt("X", pos.getX());
        tag.putInt("Y", pos.getY());
        tag.putInt("Z", pos.getZ());
        tag.putLong("Tick", discoveredTick);
        tag.putInt("Visits", visits);
        tag.putFloat("Utility", utilityScore);
        return tag;
    }

    public static DiscoveredLocation fromNbt(NbtCompound tag) {
        Type type;
        try {
            type = Type.valueOf(tag.getString("Type"));
        } catch (IllegalArgumentException e) {
            type = Type.LANDMARK;
        }
        BlockPos pos = new BlockPos(tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z"));
        long tick = tag.getLong("Tick");
        float utility = tag.getFloat("Utility");
        DiscoveredLocation loc = new DiscoveredLocation(type, pos, tick, utility);
        loc.visits = Math.max(1, tag.getInt("Visits"));
        return loc;
    }
}
