package dev.chickenthedev.stratagems;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

// Simple JSON config in config/stratagems.json. Lets players tune cooldowns,
// call-in delays, and whether stratagem explosions break blocks.
public class StratagemConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE = new File("config/stratagems.json");

    public static StratagemConfig INSTANCE = new StratagemConfig();

    public Map<String, Integer> cooldownSeconds = new HashMap<>();
    public Map<String, Integer> callInSeconds = new HashMap<>();
    public boolean blockDamage = true;

    public int cooldownTicksFor(String id, int defaultTicks) {
        Integer seconds = cooldownSeconds.get(id);
        return seconds != null ? seconds * 20 : defaultTicks;
    }

    public int callInTicksFor(String id, int defaultTicks) {
        Integer seconds = callInSeconds.get(id);
        return seconds != null ? seconds * 20 : defaultTicks;
    }

    public static void load() {
        try {
            if (FILE.exists()) {
                try (FileReader reader = new FileReader(FILE)) {
                    INSTANCE = GSON.fromJson(reader, StratagemConfig.class);
                    if (INSTANCE == null) INSTANCE = new StratagemConfig();
                }
                Constants.LOG.info("Loaded stratagem config from {}", FILE.getPath());
            } else {
                save();
                Constants.LOG.info("Wrote default stratagem config to {}", FILE.getPath());
            }
        } catch (Exception e) {
            Constants.LOG.warn("Failed to load stratagem config, using defaults", e);
            INSTANCE = new StratagemConfig();
        }
    }

    public static void save() {
        try {
            FILE.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(FILE)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (Exception e) {
            Constants.LOG.warn("Failed to save stratagem config", e);
        }
    }
}
