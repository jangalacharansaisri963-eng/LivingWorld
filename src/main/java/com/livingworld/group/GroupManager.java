package com.livingworld.group;

import com.livingworld.config.ConfigManager;
import com.livingworld.config.LivingWorldConfig;
import com.livingworld.core.LivingWorldEntity;
import com.livingworld.intelligence.MobIntelligence;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry and lifecycle manager for all active MobGroups in the world.
 * Enforces staggered evaluation, clean resource pruning, and dynamic herd formation.
 */
public class GroupManager {
    private static final GroupManager INSTANCE = new GroupManager();

    private final Map<UUID, MobGroup> activeGroups = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> mobToGroupMap = new ConcurrentHashMap<>();
    private long lastManagerTick = 0;

    private GroupManager() {}

    public static GroupManager getInstance() {
        return INSTANCE;
    }

    public synchronized MobGroup getGroupForMob(UUID mobUuid) {
        if (mobUuid == null) return null;
        UUID gId = mobToGroupMap.get(mobUuid);
        if (gId == null) return null;
        MobGroup group = activeGroups.get(gId);
        if (group != null && group.contains(mobUuid)) {
            return group;
        }
        mobToGroupMap.remove(mobUuid);
        return null;
    }

    /**
     * Finds an existing nearby group with compatible members or forms a new group.
     */
    public synchronized MobGroup findOrCreateGroup(MobEntity mob, MobIntelligence intelligence, long currentTick) {
        if (mob == null || !mob.isAlive()) return null;

        LivingWorldConfig config = ConfigManager.get();
        if (!config.enableGroupIntelligence) return null;

        UUID mobUuid = mob.getUuid();
        MobGroup existing = getGroupForMob(mobUuid);
        if (existing != null) {
            return existing;
        }

        // Search for nearby mobs of identical entity type to form or join herd
        Box searchBox = mob.getBoundingBox().expand(config.groupSearchRadius);
        List<MobEntity> nearby = mob.getWorld().getEntitiesByClass(
                (Class<MobEntity>) mob.getClass(),
                searchBox,
                e -> e.isAlive() && !e.getUuid().equals(mobUuid)
        );

        for (MobEntity candidate : nearby) {
            UUID candUuid = candidate.getUuid();
            MobGroup candGroup = getGroupForMob(candUuid);

            // Join candidate's existing group if capacity allows
            if (candGroup != null && candGroup.getMemberCount() < config.maxGroupSize) {
                if (candGroup.addMember(mob, GroupRole.FOLLOWER)) {
                    mobToGroupMap.put(mobUuid, candGroup.getGroupId());
                    return candGroup;
                }
            } else if (candGroup == null && candidate instanceof LivingWorldEntity) {
                // Form a new group together!
                MobGroup newGroup = new MobGroup(UUID.randomUUID());
                newGroup.addMember(candidate, GroupRole.LEADER);
                newGroup.addMember(mob, GroupRole.FOLLOWER);

                activeGroups.put(newGroup.getGroupId(), newGroup);
                mobToGroupMap.put(candUuid, newGroup.getGroupId());
                mobToGroupMap.put(mobUuid, newGroup.getGroupId());
                return newGroup;
            }
        }

        return null;
    }

    public synchronized void leaveGroup(UUID mobUuid) {
        UUID gId = mobToGroupMap.remove(mobUuid);
        if (gId != null) {
            MobGroup group = activeGroups.get(gId);
            if (group != null) {
                group.removeMember(mobUuid);
                if (group.getMemberCount() == 0) {
                    activeGroups.remove(gId);
                }
            }
        }
    }

    /**
     * Staggered world-level tick to evaluate active groups and clean stale references.
     */
    public synchronized void tick(ServerWorld world, long currentTick) {
        LivingWorldConfig config = ConfigManager.get();
        if (currentTick - lastManagerTick < config.groupTickInterval) {
            return;
        }
        lastManagerTick = currentTick;

        List<UUID> emptyGroupIds = new ArrayList<>();

        for (MobGroup group : activeGroups.values()) {
            group.tick(world, currentTick);
            if (group.getMemberCount() == 0) {
                emptyGroupIds.add(group.getGroupId());
            } else {
                // Synchronize reverse map
                for (UUID mUuid : group.getMemberUuids()) {
                    mobToGroupMap.put(mUuid, group.getGroupId());
                }
            }
        }

        for (UUID gId : emptyGroupIds) {
            activeGroups.remove(gId);
        }

        // Clean stale entries in mobToGroupMap
        mobToGroupMap.entrySet().removeIf(e -> {
            MobGroup g = activeGroups.get(e.getValue());
            return g == null || !g.contains(e.getKey());
        });
    }

    public synchronized Collection<MobGroup> getActiveGroups() {
        return Collections.unmodifiableCollection(activeGroups.values());
    }

    public synchronized int getActiveGroupCount() {
        return activeGroups.size();
    }

    public synchronized int getTotalGroupedMobs() {
        return mobToGroupMap.size();
    }

    public synchronized void clear() {
        activeGroups.clear();
        mobToGroupMap.clear();
    }
}
