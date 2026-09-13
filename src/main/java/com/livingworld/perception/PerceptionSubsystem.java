package com.livingworld.perception;

import com.livingworld.config.ConfigManager;
import com.livingworld.config.LivingWorldConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Executes sensory perception queries for a mob at bounded intervals.
 * Gathers entities, threats, players, food, and environmental hazards.
 */
public class PerceptionSubsystem {
    private final MobEntity mob;
    private PerceptionSnapshot lastSnapshot;
    private long lastScanTick = -1;

    public PerceptionSubsystem(MobEntity mob) {
        this.mob = mob;
        this.lastSnapshot = PerceptionSnapshot.empty(0);
    }

    /**
     * Executes perception scan or returns cached snapshot if called between scan intervals.
     */
    public PerceptionSnapshot scan(long currentTick) {
        LivingWorldConfig config = ConfigManager.get();
        if (lastSnapshot != null && (currentTick - lastScanTick) < config.perceptionScanInterval) {
            return lastSnapshot;
        }

        World world = mob.getWorld();
        if (world.isClient()) {
            return lastSnapshot;
        }

        double hRange = config.perceptionHorizontalRange;
        double vRange = config.perceptionVerticalRange;

        Box searchBox = mob.getBoundingBox().expand(hRange, vRange, hRange);
        List<Entity> nearby = world.getOtherEntities(mob, searchBox, Entity::isAlive);

        List<PerceivedEntity> allPerceived = new ArrayList<>(nearby.size());
        List<PerceivedEntity> players = new ArrayList<>();
        List<PerceivedEntity> threats = new ArrayList<>();
        List<BlockPos> foodLocations = new ArrayList<>();

        for (Entity entity : nearby) {
            double distance = mob.distanceTo(entity);
            boolean canSee = mob.canSee(entity);

            // If it is a dropped food item
            if (entity instanceof ItemEntity itemEntity) {
                if (itemEntity.getStack().contains(DataComponentTypes.FOOD)) {
                    foodLocations.add(itemEntity.getBlockPos());
                }
                continue;
            }

            PerceivedEntity perceived = new PerceivedEntity(mob, entity, distance, canSee);
            allPerceived.add(perceived);

            if (perceived.isPlayer()) {
                players.add(perceived);
            }
            if (perceived.getThreatScore() > 0.3f) {
                threats.add(perceived);
            }
        }

        // Bounded local hazard scan (small radius: 6x3x6 around mob)
        List<BlockPos> hazards = scanLocalHazards(world, mob.getBlockPos(), 6, 3);

        lastSnapshot = new PerceptionSnapshot(currentTick, allPerceived, players, threats, foodLocations, hazards);
        lastScanTick = currentTick;
        return lastSnapshot;
    }

    /**
     * Scans immediate neighborhood for dangerous blocks (fire, lava, magma, etc.).
     */
    private List<BlockPos> scanLocalHazards(World world, BlockPos center, int hRadius, int vRadius) {
        List<BlockPos> hazards = new ArrayList<>();
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for (int x = -hRadius; x <= hRadius; x += 2) {
            for (int y = -vRadius; y <= vRadius; y++) {
                for (int z = -hRadius; z <= hRadius; z += 2) {
                    mutable.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    BlockState state = world.getBlockState(mutable);
                    if (state.isOf(Blocks.LAVA) || state.isOf(Blocks.FIRE) ||
                        state.isOf(Blocks.SOUL_FIRE) || state.isOf(Blocks.CAMPFIRE) ||
                        state.isOf(Blocks.MAGMA_BLOCK) || state.isOf(Blocks.SWEET_BERRY_BUSH)) {
                        hazards.add(mutable.toImmutable());
                        if (hazards.size() >= 8) { // Bounded limit
                            return hazards;
                        }
                    }
                }
            }
        }
        return hazards;
    }

    public PerceptionSnapshot getLastSnapshot() {
        return lastSnapshot;
    }
}
