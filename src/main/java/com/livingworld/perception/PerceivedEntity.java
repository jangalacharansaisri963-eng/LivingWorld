package com.livingworld.perception;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

/**
 * Structured information about an entity observed by the perception system.
 */
public class PerceivedEntity {
    private final UUID uuid;
    private final EntityType<?> type;
    private final Vec3d position;
    private final double distance;
    private final boolean hasLineOfSight;
    private final boolean isPlayer;
    private final boolean isHostile;
    private final boolean isAlive;
    private final float threatScore;

    public PerceivedEntity(LivingEntity observer, Entity observed, double distance, boolean hasLineOfSight) {
        this.uuid = observed.getUuid();
        this.type = observed.getType();
        this.position = observed.getPos();
        this.distance = distance;
        this.hasLineOfSight = hasLineOfSight;
        this.isPlayer = observed instanceof PlayerEntity;
        this.isHostile = observed instanceof HostileEntity;
        this.isAlive = observed.isAlive();
        this.threatScore = computeThreatScore(observer, observed, distance, isPlayer, isHostile);
    }

    private static float computeThreatScore(LivingEntity observer, Entity observed, double distance,
                                            boolean isPlayer, boolean isHostile) {
        if (!observed.isAlive()) return 0.0f;

        float score = 0.0f;
        if (isHostile) {
            score += 0.8f;
        }
        if (isPlayer) {
            PlayerEntity player = (PlayerEntity) observed;
            // Holding weapons or sprint-approaching increases perceived threat
            if (player.getMainHandStack().getItem().toString().contains("sword") ||
                player.getMainHandStack().getItem().toString().contains("axe") ||
                player.getMainHandStack().getItem().toString().contains("bow")) {
                score += 0.6f;
            } else {
                score += 0.2f; // Neutral player presence
            }
        }

        // Distance attenuation: closer threats are significantly more urgent
        double proximityFactor = Math.max(0.1, 1.0 - (distance / 32.0));
        return (float) (score * proximityFactor);
    }

    public UUID getUuid() { return uuid; }
    public EntityType<?> getType() { return type; }
    public Vec3d getPosition() { return position; }
    public double distance() { return distance; }
    public boolean hasLineOfSight() { return hasLineOfSight; }
    public boolean isPlayer() { return isPlayer; }
    public boolean isHostile() { return isHostile; }
    public boolean isAlive() { return isAlive; }
    public float getThreatScore() { return threatScore; }

    @Override
    public String toString() {
        return String.format("PerceivedEntity[%s, dist=%.1f, threat=%.2f, player=%b, hostile=%b]",
                type.getUntranslatedName(), distance, threatScore, isPlayer, isHostile);
    }
}
