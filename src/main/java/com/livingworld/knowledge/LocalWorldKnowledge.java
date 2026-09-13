package com.livingworld.knowledge;

import com.livingworld.config.ConfigManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Bounded local spatial knowledge for a mob.
 * Remembers discovered shelters, danger areas, feeding zones, and landmarks.
 */
public class LocalWorldKnowledge {
    private final List<DiscoveredLocation> knownLocations = new ArrayList<>();

    public synchronized void recordLocation(DiscoveredLocation.Type type, BlockPos pos, long currentTick, float utility) {
        if (pos == null) return;

        // Check if an existing location is within 10 blocks
        for (DiscoveredLocation loc : knownLocations) {
            if (loc.getType() == type && loc.getPos().isWithinDistance(pos, 10.0)) {
                loc.recordVisit();
                return;
            }
        }

        // Bounded capacity check
        int max = ConfigManager.get().maxKnownLocations;
        if (knownLocations.size() >= max) {
            // Evict lowest utility location
            knownLocations.stream()
                    .min(Comparator.comparingDouble(DiscoveredLocation::getUtilityScore))
                    .ifPresent(knownLocations::remove);
        }

        knownLocations.add(new DiscoveredLocation(type, pos, currentTick, utility));
    }

    public synchronized List<DiscoveredLocation> getLocations(DiscoveredLocation.Type type) {
        return knownLocations.stream()
                .filter(l -> l.getType() == type)
                .collect(Collectors.toList());
    }

    public synchronized Optional<DiscoveredLocation> getClosest(DiscoveredLocation.Type type, BlockPos center) {
        if (center == null) return Optional.empty();
        return knownLocations.stream()
                .filter(l -> l.getType() == type)
                .min(Comparator.comparingDouble(l -> l.getPos().getSquaredDistance(center)));
    }

    public synchronized List<DiscoveredLocation> getAll() {
        return new ArrayList<>(knownLocations);
    }

    public synchronized void writeToNbt(NbtCompound compound) {
        NbtList list = new NbtList();
        for (DiscoveredLocation loc : knownLocations) {
            list.add(loc.toNbt());
        }
        compound.put("KnownLocations", list);
    }

    public synchronized void readFromNbt(NbtCompound compound) {
        knownLocations.clear();
        if (!compound.contains("KnownLocations", NbtElement.LIST_TYPE)) return;

        NbtList list = compound.getList("KnownLocations", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            knownLocations.add(DiscoveredLocation.fromNbt(list.getCompound(i)));
        }
    }
}
