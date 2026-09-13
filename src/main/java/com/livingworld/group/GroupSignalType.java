package com.livingworld.group;

/**
 * Categorical signal types dispatched within a mob group for communication.
 */
public enum GroupSignalType {
    DANGER_ALERT(1.5f, "Alerts nearby group companions to immediate hostile threat"),
    FOOD_SPOTTED(1.0f, "Informs companions of edible food items or grazing grounds"),
    SHELTER_FOUND(1.2f, "Signals discovered protective overhead shelter"),
    RETREAT_ORDER(1.6f, "Directs members to break formation and flee hazard immediately"),
    DEFEND_CALL(1.4f, "Calls capable group members to rally and defend territory/comrades"),
    FOLLOW_LEADER(0.8f, "Leader signaling group to maintain formation and march"),
    REGROUP(1.0f, "Signals separated group members to gather back at rally point");

    private final float baseUrgency;
    private final String description;

    GroupSignalType(float baseUrgency, String description) {
        this.baseUrgency = baseUrgency;
        this.description = description;
    }

    public float getBaseUrgency() {
        return baseUrgency;
    }

    public String getDescription() {
        return description;
    }
}
