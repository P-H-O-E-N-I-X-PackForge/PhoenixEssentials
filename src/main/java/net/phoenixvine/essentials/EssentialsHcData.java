package net.phoenixvine.essentials;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.phoenixvine.essentials.PhoenixEssentials;
import net.phoenixvine.essentials.data.NamedLocation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class EssentialsHcData {

    private static final Path DIR = Paths.get("config", "phoenix_essentials");

    private EssentialsHcData() {}

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

    private static final Path ROOT = Paths.get("config");

    public static String readRootFile(String name) {
        try {
            Path p = ROOT.resolve(name);
            return Files.exists(p) ? Files.readString(p) : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void writeRootFile(String name, String content) {
        try {
            Files.createDirectories(ROOT);
            Files.writeString(ROOT.resolve(name), content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Path configPath(String name) {
        return net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get().resolve(name);
    }

    public static boolean pathExists(Path p) {
        return Files.exists(p);
    }

    public static String readPath(Path p) {
        try {
            return Files.exists(p) ? Files.readString(p, java.nio.charset.StandardCharsets.UTF_8) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static void writePath(Path p, String content) {
        try {
            Path parent = p.getParent();
            if (parent != null) Files.createDirectories(parent);
            Files.writeString(p, content, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void logInfo(String msg) {
        PhoenixEssentials.LOGGER.info(msg);
    }

    public static void logDebug(String msg) {
        PhoenixEssentials.LOGGER.debug(msg);
    }

    public static void logWarn(String msg) {
        PhoenixEssentials.LOGGER.warn(msg);
    }

    public static NamedLocation makeLocation(String dim, double x, double y, double z, float yaw, float pitch) {
        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dim));
        return new NamedLocation(key, x, y, z, yaw, pitch);
    }

    public static String dim(NamedLocation l) { return l.dimension.location().toString(); }
    public static double x(NamedLocation l) { return l.x; }
    public static double y(NamedLocation l) { return l.y; }
    public static double z(NamedLocation l) { return l.z; }
    public static float yaw(NamedLocation l) { return l.yaw; }
    public static float pitch(NamedLocation l) { return l.pitch; }

    public static NamedLocation worldSpawn(ServerLevel overworld) {
        return new NamedLocation(overworld.dimension(), overworld.getSharedSpawnPos().getX() + 0.5,
                overworld.getSharedSpawnPos().getY(), overworld.getSharedSpawnPos().getZ() + 0.5,
                overworld.getSharedSpawnAngle(), 0f);
    }
}
