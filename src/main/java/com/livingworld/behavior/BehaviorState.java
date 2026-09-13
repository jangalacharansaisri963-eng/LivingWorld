package com.livingworld.behavior;

/**
 * Core behavioral states supported in Living World V2.
 * Supports individual autonomous states as well as group coordinated behaviors.
 */
public enum BehaviorState {
    CALM(0, "Normal relaxed activity, passive grazing or resting"),
    ALERT(1, "Noticed disturbance or potential danger, heightened awareness"),
    CURIOUS(2, "Noticed novel or interesting entity/item, observing"),
    INVESTIGATE(3, "Moving towards sound, dropped food, or unfamiliar presence"),
    FLEE(4, "Actively fleeing from danger, predator, or hostile threat"),
    DEFEND(5, "Engaging defensively against persistent attacker or protecting territory"),
    SEEK_SHELTER(6, "Seeking roof cover or safe shelter from storms, thunder, or harsh darkness"),
    SEARCH_FOOD(7, "Actively searching for grazing grounds, crops, or edible items"),
    FOLLOW(8, "Following group leader, herd companions, or familiar entity"),
    AVOID(9, "Steering clear of remembered hazard, trap, or hostile territory"),
    RETURN(10, "Returning to home landmark, colony territory, or safe refuge"),
    EXPLORE(11, "Curiously scouting unfamiliar terrain or looking for resources");

    private final int priorityTier;
    private final String description;

    BehaviorState(int priorityTier, String description) {
        this.priorityTier = priorityTier;
        this.description = description;
    }

    public int getPriorityTier() {
        return priorityTier;
    }

    public String getDescription() {
        return description;
    }
}
