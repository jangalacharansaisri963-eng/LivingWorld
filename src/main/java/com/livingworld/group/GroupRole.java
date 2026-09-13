package com.livingworld.group;

/**
 * Functional role of an individual mob within its group or herd.
 * Influences positioning, task priority, and behavior steering.
 */
public enum GroupRole {
    LEADER("Leads group movement, sets collective direction, and coordinates responses"),
    DEFENDER("Frontline protector; positions between detected threats and followers"),
    SCOUT("Roves perimeter; detects distant food sources, shelters, and hazards"),
    FOLLOWER("Maintains herd cohesion, follows leader direction, and responds to alerts");

    private final String description;

    GroupRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
