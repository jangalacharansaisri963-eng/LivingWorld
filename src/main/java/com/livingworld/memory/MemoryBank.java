package com.livingworld.memory;

import com.livingworld.config.ConfigManager;
import com.livingworld.config.LivingWorldConfig;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Bounded, priority-aware repository of memories for an intelligent mob.
 * Manages category limits, global retention, reinforcement merging, and decay.
 */
public class MemoryBank {
    private final Map<UUID, Memory> memories = new LinkedHashMap<>();
    private final Map<MemoryCategory, List<Memory>> categoryIndex = new EnumMap<>(MemoryCategory.class);

    public MemoryBank() {
        for (MemoryCategory cat : MemoryCategory.values()) {
            categoryIndex.put(cat, new ArrayList<>());
        }
    }

    /**
     * Adds or reinforces a memory in the bank with bounded capacity checks.
     */
    public synchronized Memory remember(Memory memory, long currentTick) {
        if (memory == null) return null;

        LivingWorldConfig config = ConfigManager.get();

        // Check if an existing similar memory can be reinforced instead of creating a duplicate
        Memory existingSimilar = findSimilarMemory(memory);
        if (existingSimilar != null) {
            existingSimilar.reinforce(
                    config.memoryReinforcementBoost * memory.getImportance(),
                    0.2f * memory.getConfidence(),
                    currentTick
            );
            if (memory.getLocation() != null) {
                existingSimilar.setLocation(memory.getLocation());
            }
            return existingSimilar;
        }

        // Check per-category bounds
        List<Memory> inCat = categoryIndex.get(memory.getCategory());
        if (inCat.size() >= config.maxMemoriesPerCategory) {
            evictLowestRetention(inCat);
        }

        // Check total bounds
        if (memories.size() >= config.maxTotalMemories) {
            evictLowestRetention(new ArrayList<>(memories.values()));
        }

        // Store
        memories.put(memory.getId(), memory);
        inCat.add(memory);
        return memory;
    }

    /**
     * Finds a similar existing memory based on location proximity or identical source context.
     */
    private Memory findSimilarMemory(Memory candidate) {
        List<Memory> inCat = categoryIndex.get(candidate.getCategory());
        if (inCat == null || inCat.isEmpty()) return null;

        for (Memory m : inCat) {
            // Context match (e.g. player UUID or specific entity)
            if (!candidate.getSourceContext().isEmpty()
                    && candidate.getSourceContext().equals(m.getSourceContext())) {
                return m;
            }
            // Spatial match (within 6 blocks)
            if (candidate.getLocation() != null && m.getLocation() != null) {
                if (candidate.getLocation().isWithinDistance(m.getLocation(), 6.0)) {
                    return m;
                }
            }
        }
        return null;
    }

    /**
     * Evicts the memory with the lowest retention weight from the candidate list.
     */
    private void evictLowestRetention(List<Memory> candidates) {
        if (candidates.isEmpty()) return;

        Memory lowest = null;
        float minWeight = Float.MAX_VALUE;

        for (Memory m : candidates) {
            float weight = m.getRetentionWeight();
            if (weight < minWeight) {
                minWeight = weight;
                lowest = m;
            }
        }

        if (lowest != null) {
            remove(lowest.getId());
        }
    }

    /**
     * Removes a memory by its UUID.
     */
    public synchronized boolean remove(UUID id) {
        Memory m = memories.remove(id);
        if (m != null) {
            List<Memory> inCat = categoryIndex.get(m.getCategory());
            if (inCat != null) {
                inCat.remove(m);
            }
            return true;
        }
        return false;
    }

    /**
     * Periodically called to decay memories and evict expired ones.
     */
    public synchronized void decay(long currentTick) {
        LivingWorldConfig config = ConfigManager.get();
        List<UUID> toRemove = new ArrayList<>();

        for (Memory m : memories.values()) {
            m.decay(currentTick, config.baseMemoryDecayRate);
            if (m.isExpired()) {
                toRemove.add(m.getId());
            }
        }

        for (UUID id : toRemove) {
            remove(id);
        }
    }

    /**
     * Retrieves all active memories belonging to a category.
     */
    public synchronized List<Memory> getMemories(MemoryCategory category) {
        List<Memory> list = categoryIndex.get(category);
        return list != null ? new ArrayList<>(list) : Collections.emptyList();
    }

    /**
     * Retrieves the most important memory in a specific category.
     */
    public synchronized Optional<Memory> getStrongestMemory(MemoryCategory category) {
        List<Memory> inCat = categoryIndex.get(category);
        if (inCat == null || inCat.isEmpty()) return Optional.empty();

        return inCat.stream().max(Comparator.comparingDouble(Memory::getRetentionWeight));
    }

    /**
     * Retrieves the closest memory in space for a given category.
     */
    public synchronized Optional<Memory> getClosestMemory(MemoryCategory category, BlockPos pos) {
        if (pos == null) return Optional.empty();
        List<Memory> inCat = categoryIndex.get(category);
        if (inCat == null || inCat.isEmpty()) return Optional.empty();

        return inCat.stream()
                .filter(m -> m.getLocation() != null)
                .min(Comparator.comparingDouble(m -> m.getLocation().getSquaredDistance(pos)));
    }

    public synchronized Collection<Memory> getAllMemories() {
        return new ArrayList<>(memories.values());
    }

    /**
     * Extracts high-confidence, critical memories suitable for sharing with group companions.
     */
    public synchronized List<Memory> getMemoriesForSharing(long currentTick) {
        return memories.values().stream()
                .filter(m -> m.getConfidence() >= 0.6f && m.getImportance() >= 0.6f)
                .filter(m -> m.getCategory() == MemoryCategory.DANGER
                        || m.getCategory() == MemoryCategory.FOOD
                        || m.getCategory() == MemoryCategory.SHELTER
                        || m.getCategory() == MemoryCategory.LOCATION)
                .sorted(Comparator.comparingDouble(Memory::getRetentionWeight).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    public synchronized int size() {
        return memories.size();
    }

    public synchronized void clear() {
        memories.clear();
        for (List<Memory> list : categoryIndex.values()) {
            list.clear();
        }
    }

    /**
     * Serializes all memories to an NbtCompound list.
     */
    public synchronized void writeToNbt(NbtCompound compound) {
        NbtList list = new NbtList();
        for (Memory m : memories.values()) {
            list.add(m.toNbt());
        }
        compound.put("Memories", list);
    }

    /**
     * Restores memories from an NbtCompound.
     */
    public synchronized void readFromNbt(NbtCompound compound) {
        clear();
        if (!compound.contains("Memories", NbtElement.LIST_TYPE)) return;

        NbtList list = compound.getList("Memories", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound tag = list.getCompound(i);
            Memory m = Memory.fromNbt(tag);
            memories.put(m.getId(), m);
            List<Memory> inCat = categoryIndex.get(m.getCategory());
            if (inCat != null) {
                inCat.add(m);
            }
        }
    }
}
