package com.livingworld.group;

import com.livingworld.config.ConfigManager;
import com.livingworld.memory.MemoryCategory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Bounded repository of shared memories maintained by a MobGroup.
 */
public class GroupMemoryBank {
    private final Map<UUID, SharedMemory> memories = new LinkedHashMap<>();

    public synchronized SharedMemory remember(SharedMemory memory, long currentTick) {
        if (memory == null) return null;

        // Check if existing similar memory can be reinforced
        SharedMemory existing = findSimilar(memory);
        if (existing != null) {
            existing.reinforce(0.2f * memory.getImportance(), 0.15f * memory.getConfidence(), currentTick);
            if (memory.getLocation() != null) {
                existing.setLocation(memory.getLocation());
            }
            return existing;
        }

        // Bound enforcement
        int max = ConfigManager.get().maxGroupMemories;
        if (memories.size() >= max) {
            evictLowest();
        }

        memories.put(memory.getId(), memory);
        return memory;
    }

    private SharedMemory findSimilar(SharedMemory candidate) {
        for (SharedMemory m : memories.values()) {
            if (m.getCategory() == candidate.getCategory()) {
                if (!candidate.getContext().isEmpty() && candidate.getContext().equals(m.getContext())) {
                    return m;
                }
                if (candidate.getLocation() != null && m.getLocation() != null) {
                    if (candidate.getLocation().isWithinDistance(m.getLocation(), 8.0)) {
                        return m;
                    }
                }
            }
        }
        return null;
    }

    private void evictLowest() {
        if (memories.isEmpty()) return;
        memories.values().stream()
                .min(Comparator.comparingDouble(SharedMemory::getRetentionWeight))
                .ifPresent(lowest -> memories.remove(lowest.getId()));
    }

    public synchronized void decay(long currentTick) {
        float baseRate = ConfigManager.get().baseMemoryDecayRate;
        List<UUID> toRemove = new ArrayList<>();
        for (SharedMemory m : memories.values()) {
            m.decay(currentTick, baseRate);
            if (m.isExpired()) {
                toRemove.add(m.getId());
            }
        }
        toRemove.forEach(memories::remove);
    }

    public synchronized List<SharedMemory> getMemories(MemoryCategory category) {
        return memories.values().stream()
                .filter(m -> m.getCategory() == category)
                .collect(Collectors.toList());
    }

    public synchronized Optional<SharedMemory> getStrongest(MemoryCategory category) {
        return memories.values().stream()
                .filter(m -> m.getCategory() == category)
                .max(Comparator.comparingDouble(SharedMemory::getRetentionWeight));
    }

    public synchronized Optional<SharedMemory> getClosest(MemoryCategory category, BlockPos pos) {
        if (pos == null) return Optional.empty();
        return memories.values().stream()
                .filter(m -> m.getCategory() == category && m.getLocation() != null)
                .min(Comparator.comparingDouble(m -> m.getLocation().getSquaredDistance(pos)));
    }

    public synchronized Collection<SharedMemory> getAll() {
        return new ArrayList<>(memories.values());
    }

    public synchronized int size() {
        return memories.size();
    }

    public synchronized void clear() {
        memories.clear();
    }

    public synchronized void writeToNbt(NbtCompound compound) {
        NbtList list = new NbtList();
        for (SharedMemory m : memories.values()) {
            list.add(m.toNbt());
        }
        compound.put("SharedMemories", list);
    }

    public synchronized void readFromNbt(NbtCompound compound) {
        clear();
        if (!compound.contains("SharedMemories", NbtElement.LIST_TYPE)) return;
        NbtList list = compound.getList("SharedMemories", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            SharedMemory sm = SharedMemory.fromNbt(list.getCompound(i));
            memories.put(sm.getId(), sm);
        }
    }
}
