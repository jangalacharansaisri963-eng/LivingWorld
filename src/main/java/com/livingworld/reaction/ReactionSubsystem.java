package com.livingworld.reaction;

import com.livingworld.behavior.BehaviorState;
import com.livingworld.behavior.EvaluationResult;
import net.minecraft.entity.mob.MobEntity;

/**
 * Manages the execution and transition of behavioral reactions for an intelligent mob.
 */
public class ReactionSubsystem {
    private final MobEntity mob;
    private BehaviorState currentState = BehaviorState.CALM;
    private long stateEnteredTick = 0;

    public ReactionSubsystem(MobEntity mob) {
        this.mob = mob;
    }

    /**
     * Updates active reaction state if the newly evaluated decision represents a change or priority override.
     */
    public void applyReaction(EvaluationResult decision, long currentTick) {
        if (decision == null) return;

        BehaviorState newState = decision.getState();
        if (newState != currentState) {
            this.currentState = newState;
            this.stateEnteredTick = currentTick;
        }
    }

    public BehaviorState getCurrentState() {
        return currentState;
    }

    public long getStateEnteredTick() {
        return stateEnteredTick;
    }

    public long getDurationInCurrentState(long currentTick) {
        return Math.max(0, currentTick - stateEnteredTick);
    }
}
