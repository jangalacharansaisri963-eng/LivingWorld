package com.livingworld.group;

import com.livingworld.config.ConfigManager;
import com.livingworld.config.LivingWorldConfig;
import com.livingworld.core.LivingWorldEntity;
import com.livingworld.intelligence.MobIntelligence;
import com.livingworld.memory.MemoryCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a cohesive pack, herd, or flock of intelligent mobs.
 * Coordinates collective decisions, shared memories, roles, and communication signals.
 */
public class MobGroup {
    private final UUID groupId;
    private UUID leaderUuid;
    private final Map<UUID, GroupRole> members = new LinkedHashMap<>();
    private final GroupMemoryBank sharedMemories = new GroupMemoryBank();
    private final List<GroupSignal> activeSignals = new ArrayList<>();
    private GroupDecision currentDecision;

    private long lastLeadershipTick = 0;
    private long lastDecisionTick = 0;
    private BlockPos groupCentroid = null;

    public MobGroup(UUID groupId) {
        this.groupId = groupId != null ? groupId : UUID.randomUUID();
        this.currentDecision = GroupDecision.remainTogether("Forming initial group cohesion");
    }

    public UUID getGroupId() {
        return groupId;
    }

    public synchronized UUID getLeaderUuid() {
        return leaderUuid;
    }

    public synchronized void setLeaderUuid(UUID leaderUuid) {
        this.leaderUuid = leaderUuid;
    }

    public synchronized boolean contains(UUID mobUuid) {
        return members.containsKey(mobUuid);
    }

    public synchronized int getMemberCount() {
        return members.size();
    }

    public synchronized Set<UUID> getMemberUuids() {
        return new HashSet<>(members.keySet());
    }

    public synchronized GroupRole getRole(UUID mobUuid) {
        return members.getOrDefault(mobUuid, GroupRole.FOLLOWER);
    }

    public synchronized void setRole(UUID mobUuid, GroupRole role) {
        if (members.containsKey(mobUuid)) {
            members.put(mobUuid, role);
        }
    }

    public synchronized boolean addMember(MobEntity mob, GroupRole requestedRole) {
        LivingWorldConfig config = ConfigManager.get();
        if (members.size() >= config.maxGroupSize) {
            return false;
        }

        UUID mobUuid = mob.getUuid();
        GroupRole role = requestedRole != null ? requestedRole : GroupRole.FOLLOWER;

        // If first member or no leader, assign leader
        if (members.isEmpty() || leaderUuid == null) {
            leaderUuid = mobUuid;
            role = GroupRole.LEADER;
        }

        members.put(mobUuid, role);
        return true;
    }

    public synchronized boolean removeMember(UUID mobUuid) {
        GroupRole removedRole = members.remove(mobUuid);
        if (removedRole != null) {
            if (Objects.equals(leaderUuid, mobUuid)) {
                leaderUuid = null; // Re-election on next tick
            }
            return true;
        }
        return false;
    }

    /**
     * Broadcasts a communication signal to all group members.
     * Automatically captures persistent shared memories for significant alerts.
     */
    public synchronized void broadcastSignal(GroupSignal signal, long currentTick) {
        if (signal == null) return;

        // Add to active signal buffer (bounded to 12 recent signals)
        activeSignals.add(signal);
        while (activeSignals.size() > 12) {
            activeSignals.remove(0);
        }

        // Corroborate into shared memory if critical
        if (signal.location() != null) {
            switch (signal.type()) {
                case DANGER_ALERT, RETREAT_ORDER -> {
                    SharedMemory danger = new SharedMemory(
                            MemoryCategory.DANGER, signal.location(), signal.senderUuid(),
                            currentTick, signal.importance(), 0.9f, signal.context()
                    );
                    sharedMemories.remember(danger, currentTick);
                }
                case FOOD_SPOTTED -> {
                    SharedMemory food = new SharedMemory(
                            MemoryCategory.FOOD, signal.location(), signal.senderUuid(),
                            currentTick, signal.importance(), 0.85f, signal.context()
                    );
                    sharedMemories.remember(food, currentTick);
                }
                case SHELTER_FOUND -> {
                    SharedMemory shelter = new SharedMemory(
                            MemoryCategory.SHELTER, signal.location(), signal.senderUuid(),
                            currentTick, signal.importance(), 0.9f, signal.context()
                    );
                    sharedMemories.remember(shelter, currentTick);
                }
                default -> {}
            }
        }
    }

    public synchronized List<GroupSignal> getRecentSignals(long currentTick, long maxAgeTicks) {
        List<GroupSignal> recent = new ArrayList<>();
        for (GroupSignal s : activeSignals) {
            if (!s.isExpired(currentTick, maxAgeTicks)) {
                recent.add(s);
            }
        }
        return recent;
    }

    /**
     * Periodic group coordination tick.
     * Evaluates leadership, updates roles, aggregates threats, updates centroid, and computes group decision.
     */
    public synchronized void tick(ServerWorld world, long currentTick) {
        if (members.isEmpty()) return;

        LivingWorldConfig config = ConfigManager.get();

        // 1. Decay shared memories & clean expired signals
        sharedMemories.decay(currentTick);
        activeSignals.removeIf(s -> s.isExpired(currentTick, 100));

        // 2. Validate member alive status and compute spatial centroid
        double sumX = 0, sumY = 0, sumZ = 0;
        int activeCount = 0;
        List<MobEntity> livingMobs = new ArrayList<>();
        List<UUID> deadUuids = new ArrayList<>();

        for (UUID uuid : members.keySet()) {
            Entity entity = world.getEntity(uuid);
            if (entity instanceof MobEntity mob && mob.isAlive()) {
                livingMobs.add(mob);
                sumX += mob.getX();
                sumY += mob.getY();
                sumZ += mob.getZ();
                activeCount++;
            } else {
                deadUuids.add(uuid);
            }
        }

        deadUuids.forEach(this::removeMember);

        if (activeCount == 0) {
            return;
        }

        this.groupCentroid = new BlockPos((int) (sumX / activeCount), (int) (sumY / activeCount), (int) (sumZ / activeCount));

        // 3. Separation check: prune members wandering beyond separation distance
        List<UUID> separated = new ArrayList<>();
        for (MobEntity mob : livingMobs) {
            if (groupCentroid != null && !mob.getBlockPos().isWithinDistance(groupCentroid, config.groupSeparationDistance)) {
                separated.add(mob.getUuid());
            }
        }
        for (UUID sepUuid : separated) {
            removeMember(sepUuid);
            livingMobs.removeIf(m -> m.getUuid().equals(sepUuid));
        }

        if (livingMobs.isEmpty()) return;

        // 4. Leadership evaluation
        if (leaderUuid == null || currentTick - lastLeadershipTick > config.groupLeadershipEvaluationInterval) {
            evaluateLeadership(livingMobs);
            lastLeadershipTick = currentTick;
        }

        // 5. Group Decision Consensus
        evaluateGroupDecision(livingMobs, currentTick);
    }

    private void evaluateLeadership(List<MobEntity> livingMobs) {
        MobEntity bestCandidate = null;
        float bestScore = -1f;

        for (MobEntity mob : livingMobs) {
            float healthScore = mob.getHealth() / Math.max(1.0f, mob.getMaxHealth());
            float personalityScore = 0.5f;

            if (mob instanceof LivingWorldEntity lwe) {
                MobIntelligence intel = lwe.livingworld$getIntelligence();
                if (intel != null) {
                    personalityScore = intel.getPersonality().getSociability() * 0.4f
                            + (1.0f - intel.getPersonality().getFearfulness()) * 0.3f
                            + intel.getPersonality().getTrustfulness() * 0.3f;
                }
            }

            float totalScore = (healthScore * 0.5f) + (personalityScore * 0.5f);
            if (totalScore > bestScore) {
                bestScore = totalScore;
                bestCandidate = mob;
            }
        }

        if (bestCandidate != null) {
            this.leaderUuid = bestCandidate.getUuid();
            // Assign roles
            for (MobEntity mob : livingMobs) {
                UUID uuid = mob.getUuid();
                if (uuid.equals(leaderUuid)) {
                    members.put(uuid, GroupRole.LEADER);
                } else if (mob instanceof LivingWorldEntity lwe && lwe.livingworld$getIntelligence() != null) {
                    float agg = lwe.livingworld$getIntelligence().getPersonality().getAggressiveness();
                    float cur = lwe.livingworld$getIntelligence().getPersonality().getCuriosity();
                    if (agg > 0.65f) {
                        members.put(uuid, GroupRole.DEFENDER);
                    } else if (cur > 0.60f) {
                        members.put(uuid, GroupRole.SCOUT);
                    } else {
                        members.put(uuid, GroupRole.FOLLOWER);
                    }
                } else {
                    members.put(uuid, GroupRole.FOLLOWER);
                }
            }
        }
    }

    private void evaluateGroupDecision(List<MobEntity> livingMobs, long currentTick) {
        // Check 1: Active Danger Signals or Threats detected by members
        Optional<GroupSignal> dangerSignal = activeSignals.stream()
                .filter(s -> s.type() == GroupSignalType.DANGER_ALERT || s.type() == GroupSignalType.RETREAT_ORDER)
                .max(Comparator.comparingDouble(GroupSignal::getEffectiveUrgency));

        if (dangerSignal.isPresent()) {
            GroupSignal sig = dangerSignal.get();
            int defenderCount = (int) members.values().stream().filter(r -> r == GroupRole.DEFENDER).count();
            if (defenderCount > 0 && sig.importance() < 1.4f) {
                this.currentDecision = new GroupDecision(
                        GroupDecisionType.DEFEND, 0.90f, sig.location(), sig.targetEntityUuid(),
                        "Group defenders rallying against threat; followers maintaining defensive posture"
                );
            } else {
                this.currentDecision = new GroupDecision(
                        GroupDecisionType.FLEE, 0.95f, sig.location(), sig.targetEntityUuid(),
                        "Coordinated group flight from high threat near " + (sig.location() != null ? sig.location().toShortString() : "immediate area")
                );
            }
            return;
        }

        // Check 2: Remembered Shared Shelter during harsh weather
        boolean anyInHarsh = false;
        for (MobEntity mob : livingMobs) {
            if (mob instanceof LivingWorldEntity lwe && lwe.livingworld$getIntelligence() != null) {
                var env = lwe.livingworld$getIntelligence().getEnvironmentSubsystem().getLastSnapshot();
                if (env != null && env.isHarshWeather()) {
                    anyInHarsh = true;
                    break;
                }
            }
        }

        if (anyInHarsh) {
            Optional<SharedMemory> shelter = sharedMemories.getStrongest(MemoryCategory.SHELTER);
            if (shelter.isPresent() && shelter.get().getLocation() != null) {
                this.currentDecision = new GroupDecision(
                        GroupDecisionType.SEEK_SHELTER, 0.85f, shelter.get().getLocation(), null,
                        "Collective navigation to shared shelter haven during adverse weather"
                );
                return;
            }
        }

        // Check 3: Shared Food discovery
        Optional<SharedMemory> foodMem = sharedMemories.getStrongest(MemoryCategory.FOOD);
        if (foodMem.isPresent() && foodMem.get().getLocation() != null) {
            this.currentDecision = new GroupDecision(
                    GroupDecisionType.SEARCH_FOOD, 0.65f, foodMem.get().getLocation(), null,
                    "Group grazing movement towards discovered food source at " + foodMem.get().getLocation().toShortString()
            );
            return;
        }

        // Check 4: Follow Leader or Remain Together
        MobEntity leader = null;
        if (leaderUuid != null) {
            for (MobEntity mob : livingMobs) {
                if (mob.getUuid().equals(leaderUuid)) {
                    leader = mob;
                    break;
                }
            }
        }

        if (leader != null) {
            this.currentDecision = new GroupDecision(
                    GroupDecisionType.FOLLOW_LEADER, 0.40f, leader.getBlockPos(), leader.getUuid(),
                    "Maintaining herd cohesion behind leader " + leader.getType().getUntranslatedName()
            );
        } else {
            this.currentDecision = GroupDecision.remainTogether("Herd peaceful and grazing together");
        }
    }

    public GroupMemoryBank getSharedMemories() {
        return sharedMemories;
    }

    public GroupDecision getCurrentDecision() {
        return currentDecision;
    }

    public BlockPos getGroupCentroid() {
        return groupCentroid;
    }

    public NbtCompound toNbt() {
        NbtCompound tag = new NbtCompound();
        tag.putUuid("GroupId", groupId);
        if (leaderUuid != null) {
            tag.putUuid("LeaderId", leaderUuid);
        }

        NbtList memberList = new NbtList();
        for (Map.Entry<UUID, GroupRole> entry : members.entrySet()) {
            NbtCompound mTag = new NbtCompound();
            mTag.putUuid("Uuid", entry.getKey());
            mTag.putString("Role", entry.getValue().name());
            memberList.add(mTag);
        }
        tag.put("Members", memberList);

        NbtCompound memTag = new NbtCompound();
        sharedMemories.writeToNbt(memTag);
        tag.put("SharedMemories", memTag);

        return tag;
    }

    public static MobGroup fromNbt(NbtCompound tag) {
        UUID gId = tag.containsUuid("GroupId") ? tag.getUuid("GroupId") : UUID.randomUUID();
        MobGroup group = new MobGroup(gId);
        if (tag.containsUuid("LeaderId")) {
            group.leaderUuid = tag.getUuid("LeaderId");
        }

        if (tag.contains("Members", NbtElement.LIST_TYPE)) {
            NbtList list = tag.getList("Members", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < list.size(); i++) {
                NbtCompound mTag = list.getCompound(i);
                UUID u = mTag.getUuid("Uuid");
                String rName = mTag.getString("Role");
                GroupRole role;
                try {
                    role = GroupRole.valueOf(rName);
                } catch (IllegalArgumentException e) {
                    role = GroupRole.FOLLOWER;
                }
                group.members.put(u, role);
            }
        }

        if (tag.contains("SharedMemories")) {
            group.sharedMemories.readFromNbt(tag.getCompound("SharedMemories"));
        }

        return group;
    }
}
