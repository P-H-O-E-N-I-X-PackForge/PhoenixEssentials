package net.phoenixvine.essentials.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class WarpRegistry {

    private static final class WarpEntry {

        String dimension;
        double x, y, z;
        float yaw, pitch;
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = Paths.get("config", "phoenix_essentials", "warps.json");

    private static final Map<String, NamedLocation> WARPS = new LinkedHashMap<>();

    private WarpRegistry() {}

    public static NamedLocation get(String name) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) return net.phoenixvine.essentials.hc.hcCompileMerged1.dr_warp_get(name.toLowerCase());
        return WARPS.get(name.toLowerCase());
    }

    public static void set(String name, NamedLocation loc) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) {
            net.phoenixvine.essentials.hc.hcCompileMerged1.dr_warp_set(name.toLowerCase(), loc);
            return;
        }
        WARPS.put(name.toLowerCase(), loc);
        save();
    }

    public static boolean remove(String name) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) return net.phoenixvine.essentials.hc.hcCompileMerged1.dr_warp_remove(name.toLowerCase()) != 0;
        boolean removed = WARPS.remove(name.toLowerCase()) != null;
        if (removed) save();
        return removed;
    }

    public static Map<String, NamedLocation> getAll() {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) {
            // Snapshot (insertion order) of the HC store; callers only read it.
            Map<String, NamedLocation> snapshot = new LinkedHashMap<>();
            String names = net.phoenixvine.essentials.hc.hcCompileMerged1.dr_warp_names();
            if (!names.isEmpty()) {
                for (String n : names.split("\\n")) snapshot.put(n, net.phoenixvine.essentials.hc.hcCompileMerged1.dr_warp_get(n));
            }
            return snapshot;
        }
        return WARPS;
    }

    public static void load() {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) {
            net.phoenixvine.essentials.hc.hcCompileMerged1.dr_warp_load();
            return;
        }
        WARPS.clear();
        if (!Files.exists(FILE)) return;
        try {
            String json = Files.readString(FILE);
            Type type = new TypeToken<Map<String, WarpEntry>>() {}.getType();
            Map<String, WarpEntry> parsed = GSON.fromJson(json, type);
            if (parsed == null) return;
            parsed.forEach((name, e) -> WARPS.put(name, toLocation(e)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Map<String, WarpEntry> out = new LinkedHashMap<>();
            WARPS.forEach((name, loc) -> out.put(name, toEntry(loc)));
            Files.writeString(FILE, GSON.toJson(out));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static WarpEntry toEntry(NamedLocation loc) {
        WarpEntry e = new WarpEntry();
        e.dimension = loc.dimension.location().toString();
        e.x = loc.x;
        e.y = loc.y;
        e.z = loc.z;
        e.yaw = loc.yaw;
        e.pitch = loc.pitch;
        return e;
    }

    private static NamedLocation toLocation(WarpEntry e) {
        ResourceKey<net.minecraft.world.level.Level> dim =
                ResourceKey.create(Registries.DIMENSION, new ResourceLocation(e.dimension));
        return new NamedLocation(dim, e.x, e.y, e.z, e.yaw, e.pitch);
    }
}
