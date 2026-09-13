package com.livingworld.group;

import net.minecraft.util.math.BlockPos;

import java.util.UUID;

/**
 * Ephemeral communication packet exchanged between mob group members.
 */
public record GroupSignal(
        GroupSignalType type,
        UUID senderUuid,
        BlockPos location,
        UUID targetEntityUuid,
        float importance,
        long timestamp,
        String context
) {
    public boolean isExpired(long currentTick, long maxTtlTicks) {
        return (currentTick - timestamp) > maxTtlTicks;
    }

    public float getEffectiveUrgency() {
        return type.getBaseUrgency() * importance;
    }
}
