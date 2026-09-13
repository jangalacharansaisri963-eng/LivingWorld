package com.livingworld;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Constants and metadata for Living World V2.
 */
public final class LivingWorldConstants {
    public static final String MOD_ID = "livingworld";
    public static final String MOD_NAME = "Living World V2";
    public static final String VERSION = "2.0.0";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final String NBT_ROOT_KEY = "LivingWorldData";
    public static final String NBT_VERSION_KEY = "Version";
    public static final String NBT_MEMORIES_KEY = "Memories";
    public static final String NBT_PERSONALITY_KEY = "Personality";
    public static final String NBT_KNOWLEDGE_KEY = "Knowledge";
    public static final String NBT_BEHAVIOR_KEY = "Behavior";
    public static final String NBT_GROUP_KEY = "Group";

    private LivingWorldConstants() {}
}
