package com.livingworld.behavior;

import net.minecraft.util.math.BlockPos;

import java.util.UUID;

/**
 * Result of rule-based behavior evaluation.
 * Contains the target state, priority score, target coordinates, and explainable rationale.
 */
public class EvaluationResult {
    private final BehaviorState state;
    private final float score; // 0.0 to 1.0+
    private final BlockPos targetLocation;
    private final UUID targetEntityUuid;
    private final String reason;
    private final boolean groupInfluenced;

    public EvaluationResult(BehaviorState state, float score, BlockPos targetLocation,
                            UUID targetEntityUuid, String reason) {
        this(state, score, targetLocation, targetEntityUuid, reason, false);
    }

    public EvaluationResult(BehaviorState state, float score, BlockPos targetLocation,
                            UUID targetEntityUuid, String reason, boolean groupInfluenced) {
        this.state = state;
        this.score = score;
        this.targetLocation = targetLocation;
        this.targetEntityUuid = targetEntityUuid;
        this.reason = reason != null ? reason : "";
        this.groupInfluenced = groupInfluenced;
    }

    public static EvaluationResult calm(String reason) {
        return new EvaluationResult(BehaviorState.CALM, 0.1f, null, null, reason, false);
    }

    public BehaviorState getState() { return state; }
    public float getScore() { return score; }
    public BlockPos getTargetLocation() { return targetLocation; }
    public UUID getTargetEntityUuid() { return targetEntityUuid; }
    public String getReason() { return reason; }
    public boolean isGroupInfluenced() { return groupInfluenced; }

    @Override
    public String toString() {
        return String.format("Evaluation[%s, score=%.2f, group=%b, target=%s, reason='%s']",
                state, score, groupInfluenced, targetLocation != null ? targetLocation.toShortString() : "none", reason);
    }
}
