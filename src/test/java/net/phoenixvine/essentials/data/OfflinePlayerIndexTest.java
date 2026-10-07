package net.phoenixvine.essentials.data;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OfflinePlayerIndexTest {

    private static final Path FILE = Paths.get("config", "phoenix_essentials", "player_index.json");

    @AfterEach
    void cleanUp() throws IOException {
        Files.deleteIfExists(FILE);
    }

    @Test
    void unknownPlayerReturnsNull() {
        assertNull(OfflinePlayerIndex.get(UUID.randomUUID()));
    }

    @Test
    void recordSeenMakesThePlayerLookupUpToDate() {
        UUID uuid = UUID.randomUUID();

        OfflinePlayerIndex.recordSeen(uuid, "Steve");

        OfflinePlayerIndex.Entry entry = OfflinePlayerIndex.get(uuid);
        assertTrue(entry != null);
        assertTrue("Steve".equals(entry.lastKnownName));
        assertTrue(entry.lastSeenMs > 0);
    }

    @Test
    void recordingAgainUpdatesTheNameAndTimestamp() throws InterruptedException {
        UUID uuid = UUID.randomUUID();
        OfflinePlayerIndex.recordSeen(uuid, "OldName");
        long firstSeen = OfflinePlayerIndex.get(uuid).lastSeenMs;

        Thread.sleep(2);
        OfflinePlayerIndex.recordSeen(uuid, "NewName");

        OfflinePlayerIndex.Entry entry = OfflinePlayerIndex.get(uuid);
        assertTrue("NewName".equals(entry.lastKnownName));
        assertTrue(entry.lastSeenMs >= firstSeen);
    }
}
