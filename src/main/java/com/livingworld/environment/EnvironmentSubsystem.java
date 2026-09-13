package com.livingworld.environment;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import java.util.Optional;

/**
 * Manages environmental observation for an intelligent mob.
 * Queries light, weather, biome, and shelter conditions at non-expensive intervals.
 */
public class EnvironmentSubsystem {
    private final MobEntity mob;
    private EnvironmentSnapshot lastSnapshot;
    private long lastCheckTick = -1;

    public EnvironmentSubsystem(MobEntity mob) {
        this.mob = mob;
    }

    /**
     * Inspects ambient environment or returns recent cached snapshot.
     */
    public EnvironmentSnapshot checkEnvironment(long currentTick) {
        // Cache environment for 20 ticks (1 sec)
        if (lastSnapshot != null && (currentTick - lastCheckTick) < 20) {
            return lastSnapshot;
        }

        World world = mob.getWorld();
        BlockPos pos = mob.getBlockPos();

        long timeOfDay = world.getTimeOfDay() % 24000L;
        boolean isDay = world.isDay();
        boolean isNight = !isDay;
        boolean isRaining = world.isRaining();
        boolean isThundering = world.isThundering();

        int totalLight = world.getLightLevel(pos);
        int skyLight = world.getLightLevel(LightType.SKY, pos);
        int blockLight = world.getLightLevel(LightType.BLOCK, pos);

        String biomeKey = world.getBiome(pos).getKey()
                .map(k -> k.getValue().toString())
                .orElse("minecraft:unknown");

        float shelterScore = ShelterDetector.evaluateShelterQuality(world, pos);
        boolean inWater = mob.isTouchingWater();

        lastSnapshot = new EnvironmentSnapshot(
                timeOfDay, isDay, isNight, isRaining, isThundering,
                totalLight, skyLight, blockLight, biomeKey, shelterScore, inWater
        );
        lastCheckTick = currentTick;
        return lastSnapshot;
    }

    /**
     * Looks for the nearest suitable shelter position within radius.
     */
    public Optional<BlockPos> findNearbyShelter(int radius) {
        return ShelterDetector.findNearbyShelter(mob.getWorld(), mob.getBlockPos(), radius);
    }

    public EnvironmentSnapshot getLastSnapshot() {
        return lastSnapshot;
    }
}
