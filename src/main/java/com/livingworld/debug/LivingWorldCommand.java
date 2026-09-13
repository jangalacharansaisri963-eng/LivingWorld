package com.livingworld.debug;

import com.livingworld.config.ConfigManager;
import com.livingworld.core.LivingWorldEntity;
import com.livingworld.intelligence.MobIntelligence;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;

import java.util.Comparator;
import java.util.List;

/**
 * Fabric server commands for Living World inspection, debug toggles, and status queries.
 */
public class LivingWorldCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("livingworld")
                .requires(source -> source.hasPermissionLevel(2))
                // /livingworld inspect
                .then(CommandManager.literal("inspect")
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                            // Find nearest mob within 16 blocks
                            Box box = player.getBoundingBox().expand(16.0);
                            List<MobEntity> mobs = player.getWorld().getEntitiesByClass(MobEntity.class, box, Entity::isAlive);
                            if (mobs.isEmpty()) {
                                ctx.getSource().sendFeedback(() -> Text.literal("No nearby mobs found within 16 blocks.").formatted(Formatting.RED), false);
                                return 0;
                            }

                            MobEntity nearest = mobs.stream()
                                    .min(Comparator.comparingDouble(player::squaredDistanceTo))
                                    .orElse(null);

                            if (nearest instanceof LivingWorldEntity lwMob) {
                                DebugManager.inspectMob(player, nearest, lwMob.livingworld$getIntelligence());
                            } else {
                                ctx.getSource().sendFeedback(() -> Text.literal("Nearest mob has no intelligence attached.").formatted(Formatting.YELLOW), false);
                            }
                            return 1;
                        })
                )
                // /livingworld debug <true|false>
                .then(CommandManager.literal("debug")
                        .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(ctx -> {
                                    boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
                                    ConfigManager.get().debugMode = enabled;
                                    ConfigManager.get().logEvaluations = enabled;
                                    ConfigManager.save();
                                    ctx.getSource().sendFeedback(() -> Text.literal("Living World debug mode set to: " + enabled).formatted(Formatting.GREEN), true);
                                    return 1;
                                })
                        )
                )
                // /livingworld clearmemories
                .then(CommandManager.literal("clearmemories")
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                            Box box = player.getBoundingBox().expand(16.0);
                            List<MobEntity> mobs = player.getWorld().getEntitiesByClass(MobEntity.class, box, Entity::isAlive);
                            int cleared = 0;
                            for (MobEntity mob : mobs) {
                                if (mob instanceof LivingWorldEntity lwMob) {
                                    MobIntelligence intel = lwMob.livingworld$getIntelligence();
                                    if (intel != null) {
                                        intel.getMemoryBank().clear();
                                        cleared++;
                                    }
                                }
                            }
                            final int count = cleared;
                            ctx.getSource().sendFeedback(() -> Text.literal("Cleared memories for " + count + " nearby mob(s).").formatted(Formatting.GREEN), false);
                            return cleared;
                        })
                )
                // /livingworld groups
                .then(CommandManager.literal("groups")
                        .executes(ctx -> {
                            var gm = com.livingworld.group.GroupManager.getInstance();
                            ctx.getSource().sendFeedback(() -> Text.literal(String.format("Active Groups: %d | Total Grouped Mobs: %d",
                                    gm.getActiveGroupCount(), gm.getTotalGroupedMobs())).formatted(Formatting.GOLD), false);
                            for (var g : gm.getActiveGroups()) {
                                ctx.getSource().sendFeedback(() -> Text.literal(String.format("  • Group [%s]: %d members, Leader=%s, Consensus=%s",
                                        g.getGroupId().toString().substring(0, 8),
                                        g.getMemberCount(),
                                        g.getLeaderUuid() != null ? g.getLeaderUuid().toString().substring(0, 8) : "None",
                                        g.getCurrentDecision().getType())).formatted(Formatting.AQUA), false);
                            }
                            return gm.getActiveGroupCount();
                        })
                )
        );
    }
}
