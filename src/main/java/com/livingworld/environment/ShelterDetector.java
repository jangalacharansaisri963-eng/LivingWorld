package com.livingworld.environment;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import java.util.Optional;

/**
 * Evaluates positions for overhead cover, enclosure, and safety from harsh weather and sun.
 */
public class ShelterDetector {

    /**
     * Evaluates whether a given block position qualifies as a shelter.
     * Score from 0.0 (exposed open field) to 1.0 (enclosed, roofed haven).
     */
    public static float evaluateShelterQuality(World world, BlockPos pos) {
        if (!world.getBlockState(pos.down()).isSolidBlock(world, pos.down())) {
            return 0.0f;
        }

        // Check overhead cover
        boolean hasOverheadRoof = false;
        BlockPos.Mutable checkPos = new BlockPos.Mutable(pos.getX(), pos.getY() + 1, pos.getZ());
        for (int dy = 1; dy <= 6; dy++) {
            checkPos.setY(pos.getY() + dy);
            BlockState state = world.getBlockState(checkPos);
            if (!state.isAir() && state.isOpaque()) {
                hasOverheadRoof = true;
                break;
            }
        }

        if (!hasOverheadRoof && world.isSkyVisible(pos)) {
            return 0.1f; // Fully exposed to rain and sun
        }

        float score = 0.5f; // Has roof

        // Check horizontal enclosure (walls)
        int wallCount = 0;
        BlockPos[] cardinals = {
                pos.north(), pos.south(), pos.east(), pos.west()
        };
        for (BlockPos wall : cardinals) {
            if (world.getBlockState(wall).isOpaque()) {
                wallCount++;
            }
        }
        score += (wallCount * 0.1f); // Up to +0.4 for four walls

        // Light safety (avoid total pitch black where monsters spawn)
        int blockLight = world.getLightLevel(LightType.BLOCK, pos);
        if (blockLight > 4) {
            score += 0.1f;
        }

        return Math.min(1.0f, score);
    }

    /**
     * Searches for a nearby shelter position within a bounded search radius.
     */
    public static Optional<BlockPos> findNearbyShelter(World world, BlockPos center, int radius) {
        BlockPos bestPos = null;
        float bestScore = 0.45f; // Threshold for acceptable shelter

        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int x = -radius; x <= radius; x += 3) {
            for (int z = -radius; z <= radius; z += 3) {
                for (int y = -2; y <= 3; y++) {
                    mutable.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    if (world.isAir(mutable) && world.isAir(mutable.up())) {
                        float score = evaluateShelterQuality(world, mutable);
                        if (score > bestScore) {
                            bestScore = score;
                            bestPos = mutable.toImmutable();
                            if (score >= 0.85f) {
                                return Optional.of(bestPos); // Early exit on high-quality shelter
                            }
                        }
                    }
                }
            }
        }

        return Optional.ofNullable(bestPos);
    }
}
