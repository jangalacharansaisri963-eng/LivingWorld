package com.livingworld.group;

/**
 * High-level behavioral consensus decisions agreed upon by a mob group.
 */
public enum GroupDecisionType {
    REMAIN_TOGETHER("Group remains in close cohesion, grazing or resting calmly"),
    INVESTIGATE("Group or scout investigates an interesting sound or sighting"),
    FLEE("Coordinated group escape away from active danger"),
    DEFEND("Defenders rally to confront and drive off an aggressor"),
    SEEK_SHELTER("Group travels together towards known overhead shelter"),
    SEARCH_FOOD("Group moves collectively to locate or harvest food sources"),
    SAFE_HAVEN_RETREAT("Group conducts an orderly retreat to a remembered safe landmark"),
    FOLLOW_LEADER("Group lines up behind and accompanies the elected leader"),
    REGROUP("Scattered members navigate towards a central rally coordinate");

    private final String description;

    GroupDecisionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
