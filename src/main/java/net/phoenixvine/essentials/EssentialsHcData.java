package net.phoenixvine.essentials;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.phoenixvine.essentials.data.NamedLocation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Tiny disclosed shim for the Hot Chocolate data registries ({@code EssentialsDataRegistries.hotc}):
 * config-file text IO and {@link NamedLocation} construction/field access (it holds a
 * {@code ResourceKey}, which HC doesn't bind). Everything else -- the stores, lookups, JSON codec --
 * is HC.
 */
public final class EssentialsHcData {

    private static final Path DIR = Paths.get("config", "phoenix_essentials");

    private EssentialsHcData() {}

    /** File contents under {@code config/phoenix_essentials/}, or {@code null} if missing/unreadable. */
    public static String readFile(String name) {
        try {
            Path p = DIR.resolve(name);
            return Files.exists(p) ? Files.readString(p) : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void writeFile(String name, String content) {
        try {
            Files.createDirectories(DIR);
            Files.writeString(DIR.resolve(name), content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static NamedLocation makeLocation(String dim, double x, double y, double z, float yaw, float pitch) {
        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(dim));
        return new NamedLocation(key, x, y, z, yaw, pitch);
    }

    public static String dim(NamedLocation l) { return l.dimension.location().toString(); }
    public static double x(NamedLocation l) { return l.x; }
    public static double y(NamedLocation l) { return l.y; }
    public static double z(NamedLocation l) { return l.z; }
    public static float yaw(NamedLocation l) { return l.yaw; }
    public static float pitch(NamedLocation l) { return l.pitch; }

    /** The world spawn used when no {@code /setspawn} has been saved. */
    public static NamedLocation worldSpawn(ServerLevel overworld) {
        return new NamedLocation(overworld.dimension(), overworld.getSharedSpawnPos().getX() + 0.5,
                overworld.getSharedSpawnPos().getY(), overworld.getSharedSpawnPos().getZ() + 0.5,
                overworld.getSharedSpawnAngle(), 0f);
    }
}
