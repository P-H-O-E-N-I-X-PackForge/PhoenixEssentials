package net.phoenixvine.essentials.data;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KitRegistryTest {

    private static final Path FILE = Paths.get("config", "phoenix_essentials", "kits.json");

    @AfterEach
    void cleanUp() throws IOException {
        KitRegistry.getAll().clear();
        Files.deleteIfExists(FILE);
    }

    @Test
    void setAndGetAreCaseInsensitive() {
        KitRegistry.set(new KitDefinition("Starter"));

        assertTrue(KitRegistry.get("starter") != null);
        assertTrue(KitRegistry.get("STARTER") != null);
    }

    @Test
    void settingTheSameNameOverwritesTheOldDefinition() {
        KitDefinition first = new KitDefinition("starter");
        first.cooldownSeconds = 60;
        KitDefinition second = new KitDefinition("STARTER");
        second.cooldownSeconds = 120;

        KitRegistry.set(first);
        KitRegistry.set(second);

        assertEquals(1, KitRegistry.getAll().size(), "re-setting an existing kit name should overwrite, not duplicate");
        assertEquals(120, KitRegistry.get("starter").cooldownSeconds);
    }

    @Test
    void removeReportsWhetherAKitExisted() {
        KitRegistry.set(new KitDefinition("starter"));

        assertTrue(KitRegistry.remove("STARTER"), "remove should succeed and be case-insensitive like set/get");
        assertFalse(KitRegistry.remove("starter"), "removing an already-gone kit should report false");
        assertNull(KitRegistry.get("starter"));
    }
}
