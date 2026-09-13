package com.livingworld.knowledge;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

/**
 * World-level persistence layer for Living World using Minecraft 1.21.1 PersistentState API.
 * Uses RegistryWrapper.WrapperLookup pattern required by 1.21.1.
 */
public class WorldKnowledgeState extends PersistentState {
    private static final String IDENTIFIER = "livingworld_knowledge";

    public static final Type<WorldKnowledgeState> TYPE = new Type<>(
            WorldKnowledgeState::new,
            WorldKnowledgeState::createFromNbt,
            null
    );

    private long worldEventsRecorded = 0;
    private long totalMemoriesFormed = 0;

    public WorldKnowledgeState() {
        super();
    }

    public static WorldKnowledgeState getServerState(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE, IDENTIFIER);
    }

    public void incrementMemoriesFormed() {
        this.totalMemoriesFormed++;
        this.markDirty();
    }

    public void incrementWorldEvents() {
        this.worldEventsRecorded++;
        this.markDirty();
    }

    public long getTotalMemoriesFormed() { return totalMemoriesFormed; }
    public long getWorldEventsRecorded() { return worldEventsRecorded; }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putLong("WorldEventsRecorded", worldEventsRecorded);
        nbt.putLong("TotalMemoriesFormed", totalMemoriesFormed);
        return nbt;
    }

    public static WorldKnowledgeState createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        WorldKnowledgeState state = new WorldKnowledgeState();
        state.worldEventsRecorded = tag.getLong("WorldEventsRecorded");
        state.totalMemoriesFormed = tag.getLong("TotalMemoriesFormed");
        return state;
    }
}
