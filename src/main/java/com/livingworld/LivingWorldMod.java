package com.livingworld;

import com.livingworld.config.ConfigManager;
import com.livingworld.debug.LivingWorldCommand;
import com.livingworld.group.GroupManager;
import com.livingworld.knowledge.WorldKnowledgeState;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;

/**
 * Main Fabric Mod Initializer for Living World V2.
 * Initializes configuration, commands, lifecycle events, world persistent state, and group manager.
 */
public class LivingWorldMod implements ModInitializer {

    @Override
    public void onInitialize() {
        LivingWorldConstants.LOGGER.info("==========================================");
        LivingWorldConstants.LOGGER.info("  Initializing {} v{} for Minecraft 1.21.1",
                LivingWorldConstants.MOD_NAME, LivingWorldConstants.VERSION);
        LivingWorldConstants.LOGGER.info("==========================================");

        // 1. Initialize Configuration
        ConfigManager.load();

        // 2. Register Server Commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LivingWorldCommand.register(dispatcher);
        });

        // 3. Register Server Lifecycle Listeners
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            LivingWorldConstants.LOGGER.info("[LivingWorld V2] Dedicated/Integrated server active. Intelligence pipeline & group coordination enabled.");
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            ConfigManager.save();
            GroupManager.getInstance().clear();
            LivingWorldConstants.LOGGER.info("[LivingWorld V2] Server stopping. Saved configuration and flushed groups.");
        });

        // 4. Register World Tick for Group Coordination
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (ConfigManager.get().enableGroupIntelligence) {
                GroupManager.getInstance().tick(world, world.getTime());
            }
        });

        // 5. Register World Load Listeners for Persistent State
        ServerWorldEvents.LOAD.register((server, world) -> {
            WorldKnowledgeState state = WorldKnowledgeState.getServerState(world);
            LivingWorldConstants.LOGGER.info("[LivingWorld V2] Loaded WorldKnowledgeState for dimension: {} (Total memories tracked: {})",
                    world.getRegistryKey().getValue(), state.getTotalMemoriesFormed());
        });

        LivingWorldConstants.LOGGER.info("[LivingWorld V2] Mod initialization complete.");
    }
}
