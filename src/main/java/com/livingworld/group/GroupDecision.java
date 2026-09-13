package com.livingworld.group;

import net.minecraft.util.math.BlockPos;

import java.util.UUID;

/**
 * Result of group-level behavioral evaluation.
 * Informs individual members' behavior while preserving their individual survival imperatives.
 */
public class GroupDecision {
    private final GroupDecisionType type;
    private final float score; // 0.0 to 1.0+
    private final BlockPos targetLocation;
    private final UUID targetEntityUuid;
    private final String reason;

    public GroupDecision(GroupDecisionType type, float score, BlockPos targetLocation,
                         UUID targetEntityUuid, String reason) {
        this.type = type;
        this.score = score;
        this.targetLocation = targetLocation;
        this.targetEntityUuid = targetEntityUuid;
        this.reason = reason != null ? reason : "";
    }

    public static GroupDecision remainTogether(String reason) {
        return new GroupDecision(GroupDecisionType.REMAIN_TOGETHER, 0.2f, null, null, reason);
    }

    public GroupDecisionType getType() {
        return type;
    }

    public float getScore() {
        return score;
    }

    public BlockPos getTargetLocation() {
        return targetLocation;
    }

    public UUID getTargetEntityUuid() {
        return targetEntityUuid;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return String.format("GroupDecision[%s, score=%.2f, target=%s, reason='%s']",
                type, score, targetLocation != null ? targetLocation.toShortString() : "none", reason);
    }
}
