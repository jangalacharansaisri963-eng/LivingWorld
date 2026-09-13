package com.livingworld.perception;

import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Immutable structured snapshot produced by the perception system.
 */
public class PerceptionSnapshot {
    private final long timestamp;
    private final List<PerceivedEntity> allEntities;
    private final List<PerceivedEntity> players;
    private final List<PerceivedEntity> threats;
    private final List<BlockPos> foodLocations;
    private final List<BlockPos> nearbyHazards;
    private final float maxThreatLevel;
    private final PerceivedEntity primaryThreat;

    public PerceptionSnapshot(long timestamp,
                              List<PerceivedEntity> allEntities,
                              List<PerceivedEntity> players,
                              List<PerceivedEntity> threats,
                              List<BlockPos> foodLocations,
                              List<BlockPos> nearbyHazards) {
        this.timestamp = timestamp;
        this.allEntities = Collections.unmodifiableList(allEntities);
        this.players = Collections.unmodifiableList(players);
        this.threats = Collections.unmodifiableList(threats);
        this.foodLocations = Collections.unmodifiableList(foodLocations);
        this.nearbyHazards = Collections.unmodifiableList(nearbyHazards);

        PerceivedEntity maxThreat = null;
        float maxScore = 0.0f;
        for (PerceivedEntity threat : threats) {
            if (threat.getThreatScore() > maxScore) {
                maxScore = threat.getThreatScore();
                maxThreat = threat;
            }
        }
        this.maxThreatLevel = maxScore;
        this.primaryThreat = maxThreat;
    }

    public static PerceptionSnapshot empty(long timestamp) {
        return new PerceptionSnapshot(timestamp, Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    public long getTimestamp() { return timestamp; }
    public List<PerceivedEntity> getAllEntities() { return allEntities; }
    public List<PerceivedEntity> getPlayers() { return players; }
    public List<PerceivedEntity> getThreats() { return threats; }
    public List<BlockPos> getFoodLocations() { return foodLocations; }
    public List<BlockPos> getNearbyHazards() { return nearbyHazards; }
    public float getMaxThreatLevel() { return maxThreatLevel; }
    public Optional<PerceivedEntity> getPrimaryThreat() { return Optional.ofNullable(primaryThreat); }

    public boolean hasImmediateThreat() {
        return maxThreatLevel >= 0.5f;
    }

    public boolean hasPlayers() {
        return !players.isEmpty();
    }

    public boolean hasFood() {
        return !foodLocations.isEmpty();
    }

    public boolean hasHazards() {
        return !nearbyHazards.isEmpty();
    }
}
