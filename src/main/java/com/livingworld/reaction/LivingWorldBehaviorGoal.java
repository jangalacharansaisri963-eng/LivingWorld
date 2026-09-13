package com.livingworld.reaction;

import com.livingworld.behavior.BehaviorState;
import com.livingworld.behavior.EvaluationResult;
import com.livingworld.intelligence.MobIntelligence;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;

/**
 * Fabric Goal injected into vanilla MobEntity's goalSelector.
 * Applies Living World behavioral steering while respecting vanilla navigation.
 */
public class LivingWorldBehaviorGoal extends Goal {
    private final MobEntity mob;
    private final MobIntelligence intelligence;
    private int executionTimer = 0;

    public LivingWorldBehaviorGoal(MobEntity mob, MobIntelligence intelligence) {
        this.mob = mob;
        this.intelligence = intelligence;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (intelligence == null) return false;
        EvaluationResult eval = intelligence.getCurrentEvaluation();
        return eval != null && eval.getState() != BehaviorState.CALM;
    }

    @Override
    public boolean shouldContinue() {
        if (intelligence == null) return false;
        EvaluationResult eval = intelligence.getCurrentEvaluation();
        if (eval == null || eval.getState() == BehaviorState.CALM) return false;
        return executionTimer < 100 && !mob.getNavigation().isIdle();
    }

    @Override
    public void start() {
        this.executionTimer = 0;
        executeReactionStep();
    }

    @Override
    public void tick() {
        this.executionTimer++;
        if (this.executionTimer % 15 == 0) {
            executeReactionStep();
        }
    }

    @Override
    public void stop() {
        this.executionTimer = 0;
    }

    private void executeReactionStep() {
        EvaluationResult eval = intelligence.getCurrentEvaluation();
        if (eval == null) return;

        BehaviorState state = eval.getState();
        BlockPos targetPos = eval.getTargetLocation();

        switch (state) {
            case FLEE -> {
                if (targetPos != null) {
                    // Flee in the opposite direction of the threat
                    Vec3d threatVec = Vec3d.ofCenter(targetPos);
                    if (mob instanceof PathAwareEntity pathAware) {
                        Vec3d fleeTarget = NoPenaltyTargeting.findFrom(pathAware, 16, 7, threatVec);
                        if (fleeTarget != null) {
                            mob.getNavigation().startMovingTo(fleeTarget.x, fleeTarget.y, fleeTarget.z, 1.3);
                        }
                    } else {
                        Vec3d awayVec = mob.getPos().subtract(threatVec).normalize();
                        Vec3d fleeTarget = mob.getPos().add(awayVec.multiply(12));
                        mob.getNavigation().startMovingTo(fleeTarget.x, fleeTarget.y, fleeTarget.z, 1.3);
                    }
                }
            }
            case SEEK_SHELTER -> {
                if (targetPos != null) {
                    mob.getNavigation().startMovingTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.15);
                } else {
                    // Look for immediate shelter dynamically
                    intelligence.getEnvironmentSubsystem().findNearbyShelter(14).ifPresent(pos -> {
                        mob.getNavigation().startMovingTo(pos.getX(), pos.getY(), pos.getZ(), 1.15);
                    });
                }
            }
            case INVESTIGATE -> {
                if (targetPos != null) {
                    mob.getNavigation().startMovingTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.0);
                    mob.getLookControl().lookAt(targetPos.getX(), targetPos.getY(), targetPos.getZ());
                }
            }
            case ALERT -> {
                // Stop pathing, orient eyes towards the alert location
                mob.getNavigation().stop();
                if (targetPos != null) {
                    mob.getLookControl().lookAt(targetPos.getX(), targetPos.getY(), targetPos.getZ());
                }
            }
            case CURIOUS -> {
                if (targetPos != null) {
                    // Approach to a polite distance (4-6 blocks)
                    double dist = mob.squaredDistanceTo(Vec3d.ofCenter(targetPos));
                    if (dist > 16.0) {
                        mob.getNavigation().startMovingTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 0.9);
                    } else {
                        mob.getNavigation().stop();
                    }
                    mob.getLookControl().lookAt(targetPos.getX(), targetPos.getY(), targetPos.getZ());
                }
            }
            case DEFEND -> {
                if (eval.getTargetEntityUuid() != null) {
                    // If target entity exists, engage look control or target selector
                    if (targetPos != null) {
                        mob.getLookControl().lookAt(targetPos.getX(), targetPos.getY(), targetPos.getZ());
                        mob.getNavigation().startMovingTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.25);
                    }
                }
            }
            case SEARCH_FOOD -> {
                if (targetPos != null) {
                    mob.getNavigation().startMovingTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.05);
                    mob.getLookControl().lookAt(targetPos.getX(), targetPos.getY(), targetPos.getZ());
                }
            }
            case FOLLOW -> {
                if (targetPos != null) {
                    double distSq = mob.squaredDistanceTo(Vec3d.ofCenter(targetPos));
                    if (distSq > 16.0) { // Beyond 4 blocks
                        mob.getNavigation().startMovingTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.15);
                    } else if (distSq < 6.25) { // Closer than 2.5 blocks
                        mob.getNavigation().stop();
                    }
                    mob.getLookControl().lookAt(targetPos.getX(), targetPos.getY(), targetPos.getZ());
                }
            }
            case AVOID -> {
                if (targetPos != null) {
                    Vec3d hazardVec = Vec3d.ofCenter(targetPos);
                    if (mob instanceof PathAwareEntity pathAware) {
                        Vec3d avoidTarget = NoPenaltyTargeting.findFrom(pathAware, 12, 6, hazardVec);
                        if (avoidTarget != null) {
                            mob.getNavigation().startMovingTo(avoidTarget.x, avoidTarget.y, avoidTarget.z, 1.05);
                        }
                    } else {
                        Vec3d awayVec = mob.getPos().subtract(hazardVec).normalize();
                        Vec3d avoidTarget = mob.getPos().add(awayVec.multiply(8));
                        mob.getNavigation().startMovingTo(avoidTarget.x, avoidTarget.y, avoidTarget.z, 1.05);
                    }
                }
            }
            case RETURN -> {
                if (targetPos != null) {
                    mob.getNavigation().startMovingTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.1);
                    mob.getLookControl().lookAt(targetPos.getX(), targetPos.getY(), targetPos.getZ());
                }
            }
            case EXPLORE -> {
                if (mob instanceof PathAwareEntity pathAware) {
                    Vec3d roamTarget = NoPenaltyTargeting.find(pathAware, 10, 5);
                    if (roamTarget != null) {
                        mob.getNavigation().startMovingTo(roamTarget.x, roamTarget.y, roamTarget.z, 0.95);
                    }
                }
            }
            case CALM -> {
                // Handled by vanilla wandering and goals
            }
        }
    }
}
