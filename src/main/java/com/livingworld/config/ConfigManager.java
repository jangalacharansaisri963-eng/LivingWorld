package com.livingworld.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.livingworld.LivingWorldConstants;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Handles loading, saving, and accessing Living World configuration.
 */
public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
    private static final File CONFIG_FILE = CONFIG_DIR.resolve("livingworld.json").toFile();

    private static LivingWorldConfig instance = new LivingWorldConfig();

    public static LivingWorldConfig get() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                LivingWorldConfig loaded = GSON.fromJson(reader, LivingWorldConfig.class);
                if (loaded != null) {
                    instance = loaded;
                    LivingWorldConstants.LOGGER.info("[LivingWorld] Configuration loaded successfully.");
                    return;
                }
            } catch (IOException e) {
                LivingWorldConstants.LOGGER.error("[LivingWorld] Failed to read configuration file: {}", e.getMessage());
            }
        }
        // Save defaults if file does not exist or failed
        instance = new LivingWorldConfig();
        save();
    }

    public static void save() {
        try {
            if (!CONFIG_FILE.getParentFile().exists()) {
                CONFIG_FILE.getParentFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(instance, writer);
                LivingWorldConstants.LOGGER.info("[LivingWorld] Default configuration saved.");
            }
        } catch (IOException e) {
            LivingWorldConstants.LOGGER.error("[LivingWorld] Failed to write configuration file: {}", e.getMessage());
        }
    }
}
