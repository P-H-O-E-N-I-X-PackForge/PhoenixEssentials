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
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) return hcGet(name.toLowerCase());
        return KITS.get(name.toLowerCase());
    }

    public static void set(KitDefinition kit) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) {
            String key = kit.name.toLowerCase();
            net.phoenixvine.essentials.hc.hcCompileMerged1.dr_kit_put(key, kit.name, kit.cooldownSeconds);
            for (KitItemEntry e : kit.items) {
                net.phoenixvine.essentials.hc.hcCompileMerged1.dr_kit_put_item(key, e.item, e.count, e.nbt == null ? "" : e.nbt);
            }
            net.phoenixvine.essentials.hc.hcCompileMerged1.dr_kit_save();
            return;
        }
        KITS.put(kit.name.toLowerCase(), kit);
        save();
    }

    public static boolean remove(String name) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) return net.phoenixvine.essentials.hc.hcCompileMerged1.dr_kit_remove(name.toLowerCase()) != 0;
        boolean removed = KITS.remove(name.toLowerCase()) != null;
        if (removed) save();
        return removed;
    }

    public static Map<String, KitDefinition> getAll() {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) {
            // Snapshot (insertion order) of the HC store; callers only read it.
            Map<String, KitDefinition> snapshot = new LinkedHashMap<>();
            String names = net.phoenixvine.essentials.hc.hcCompileMerged1.dr_kit_names();
            if (!names.isEmpty()) {
                for (String key : names.split("\n")) snapshot.put(key, hcGet(key));
            }
            return snapshot;
        }
        return KITS;
    }

    private static KitDefinition hcGet(String key) {
        if (net.phoenixvine.essentials.hc.hcCompileMerged1.dr_kit_exists(key) == 0) return null;
        KitDefinition kit = new KitDefinition(net.phoenixvine.essentials.hc.hcCompileMerged1.dr_kit_name(key));
        kit.cooldownSeconds = net.phoenixvine.essentials.hc.hcCompileMerged1.dr_kit_cooldown(key);
        int n = net.phoenixvine.essentials.hc.hcCompileMerged1.dr_kit_item_count(key);
        for (int i = 0; i < n; i++) {
            KitItemEntry e = new KitItemEntry(net.phoenixvine.essentials.hc.hcCompileMerged1.dr_kit_item_id(key, i), net.phoenixvine.essentials.hc.hcCompileMerged1.dr_kit_item_amount(key, i));
            e.nbt = net.phoenixvine.essentials.hc.hcCompileMerged1.dr_kit_item_nbt(key, i);
            kit.items.add(e);
        }
        return kit;
    }

    public static void load() {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) {
            net.phoenixvine.essentials.hc.hcCompileMerged1.dr_kit_load();
            return;
        }
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
