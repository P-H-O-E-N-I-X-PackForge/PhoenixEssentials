package net.phoenixvine.essentials.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class OfflinePlayerIndex {

    public static final class Entry {

        public long lastSeenMs;
        public String lastKnownName = "";
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = Paths.get("config", "phoenix_essentials", "player_index.json");

    private static final Map<UUID, Entry> INDEX = new LinkedHashMap<>();
    private static boolean loaded = false;

    private OfflinePlayerIndex() {}

    public static Entry get(UUID uuid) {
        ensureLoaded();
        return INDEX.get(uuid);
    }

    public static void recordSeen(UUID uuid, String name) {
        ensureLoaded();
        Entry e = new Entry();
        e.lastSeenMs = System.currentTimeMillis();
        e.lastKnownName = name;
        INDEX.put(uuid, e);
        save();
    }

    private static void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        load();
    }

    private static void load() {
        INDEX.clear();
        if (!Files.exists(FILE)) return;
        try {
            String json = Files.readString(FILE);
            Type type = new TypeToken<Map<UUID, Entry>>() {}.getType();
            Map<UUID, Entry> parsed = GSON.fromJson(json, type);
            if (parsed != null) INDEX.putAll(parsed);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(INDEX));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
