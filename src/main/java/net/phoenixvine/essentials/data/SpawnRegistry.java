package net.phoenixvine.essentials.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class SpawnRegistry {

    private static final class SpawnEntry {

        String dimension;
        double x, y, z;
        float yaw, pitch;
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = Paths.get("config", "phoenix_essentials", "spawn.json");

    private static NamedLocation spawn = null;

    private SpawnRegistry() {}

    public static void set(NamedLocation loc) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) {
            net.phoenixvine.essentials.hc.hcCompileMerged1.dr_spawn_set(loc);
            return;
        }
        spawn = loc;
        save();
    }

    public static NamedLocation getOrDefault(ServerLevel overworld) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) {
            NamedLocation saved = net.phoenixvine.essentials.hc.hcCompileMerged1.dr_spawn_get();
            return saved != null ? saved : net.phoenixvine.essentials.EssentialsHcData.worldSpawn(overworld);
        }
        if (spawn != null) return spawn;
        return new NamedLocation(overworld.dimension(), overworld.getSharedSpawnPos().getX() + 0.5,
                overworld.getSharedSpawnPos().getY(), overworld.getSharedSpawnPos().getZ() + 0.5,
                overworld.getSharedSpawnAngle(), 0f);
    }

    public static void load() {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) {
            net.phoenixvine.essentials.hc.hcCompileMerged1.dr_spawn_load();
            return;
        }
        spawn = null;
        if (!Files.exists(FILE)) return;
        try {
            String json = Files.readString(FILE);
            SpawnEntry e = GSON.fromJson(json, SpawnEntry.class);
            if (e == null) return;
            ResourceKey<net.minecraft.world.level.Level> dim =
                    ResourceKey.create(Registries.DIMENSION, new ResourceLocation(e.dimension));
            spawn = new NamedLocation(dim, e.x, e.y, e.z, e.yaw, e.pitch);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        if (spawn == null) return;
        try {
            Files.createDirectories(FILE.getParent());
            SpawnEntry e = new SpawnEntry();
            e.dimension = spawn.dimension.location().toString();
            e.x = spawn.x;
            e.y = spawn.y;
            e.z = spawn.z;
            e.yaw = spawn.yaw;
            e.pitch = spawn.pitch;
            Files.writeString(FILE, GSON.toJson(e));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
