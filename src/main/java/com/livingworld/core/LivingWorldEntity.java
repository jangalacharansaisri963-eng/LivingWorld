package com.livingworld.core;

import com.livingworld.intelligence.MobIntelligence;

/**
 * Interface injected into MobEntity via Fabric Mixin (Duck Typing).
 * Allows retrieving the attached MobIntelligence component.
 */
public interface LivingWorldEntity {
    MobIntelligence livingworld$getIntelligence();
    void livingworld$setIntelligence(MobIntelligence intelligence);
}
