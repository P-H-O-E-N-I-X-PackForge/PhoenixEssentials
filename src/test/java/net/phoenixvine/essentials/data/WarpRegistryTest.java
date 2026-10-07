package net.phoenixvine.essentials.data;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarpRegistryTest {

    private static final Path FILE = Paths.get("config", "phoenix_essentials", "warps.json");

    @AfterEach
    void cleanUp() throws IOException {
        WarpRegistry.getAll().clear();
        Files.deleteIfExists(FILE);
    }

    private static NamedLocation stubLocation(double x) {
        NamedLocation loc = new NamedLocation();
        loc.x = x;
        loc.y = 64;
        loc.z = 0;
        return loc;
    }

    @Test
    void setAndGetAreCaseInsensitive() {
        WarpRegistry.set("Spawn", stubLocation(1));

        assertTrue(WarpRegistry.get("spawn") != null);
        assertTrue(WarpRegistry.get("SPAWN") != null);
        assertTrue(WarpRegistry.get("spawn").x == 1);
    }

    @Test
    void settingTheSameNameOverwritesTheOldLocation() {
        WarpRegistry.set("spawn", stubLocation(1));
        WarpRegistry.set("SPAWN", stubLocation(2));

        assertTrue(WarpRegistry.get("spawn").x == 2, "re-setting an existing warp should overwrite, not duplicate");
    }

    @Test
    void removeReportsWhetherAWarpExisted() {
        WarpRegistry.set("spawn", stubLocation(1));

        assertTrue(WarpRegistry.remove("SPAWN"), "remove should succeed and be case-insensitive like set/get");
        assertFalse(WarpRegistry.remove("spawn"), "removing an already-gone warp should report false");
        assertNull(WarpRegistry.get("spawn"));
    }
}
