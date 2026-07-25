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

public final class KitRegistry {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = Paths.get("config", "phoenix_essentials", "kits.json");

    private static final Map<String, KitDefinition> KITS = new LinkedHashMap<>();

    private KitRegistry() {}

    public static KitDefinition get(String name) {
        return KITS.get(name.toLowerCase());
    }

    public static void set(KitDefinition kit) {
        KITS.put(kit.name.toLowerCase(), kit);
        save();
    }

    public static boolean remove(String name) {
        boolean removed = KITS.remove(name.toLowerCase()) != null;
        if (removed) save();
        return removed;
    }

    public static Map<String, KitDefinition> getAll() {
        return KITS;
    }

    public static void load() {
        KITS.clear();
        if (!Files.exists(FILE)) return;
        try {
            String json = Files.readString(FILE);
            Type type = new TypeToken<Map<String, KitDefinition>>() {}.getType();
            Map<String, KitDefinition> parsed = GSON.fromJson(json, type);
            if (parsed != null) KITS.putAll(parsed);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(KITS));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
