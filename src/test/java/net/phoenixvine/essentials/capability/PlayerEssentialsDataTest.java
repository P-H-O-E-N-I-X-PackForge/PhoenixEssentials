package net.phoenixvine.essentials.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerEssentialsDataTest {

    @Test
    void settingAfkTrueRecordsATimestampAndSettingFalseClearsIt() {
        PlayerEssentialsData data = new PlayerEssentialsData();

        data.setAfk(true);
        assertTrue(data.isAfk());
        assertTrue(data.getAfkSinceMs() > 0, "going AFK should stamp a nonzero afkSince time");

        data.setAfk(false);
        assertFalse(data.isAfk());
        assertEquals(0L, data.getAfkSinceMs(), "clearing AFK should reset afkSince back to 0");
    }

    @Test
    void ignoringTracksArbitraryUuidsIndependently() {
        PlayerEssentialsData data = new PlayerEssentialsData();
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        data.getIgnored().add(a);

        assertTrue(data.isIgnoring(a));
        assertFalse(data.isIgnoring(b));
    }

    @Test
    void kitCooldownLookupIsCaseInsensitiveAndDefaultsToZero() {
        PlayerEssentialsData data = new PlayerEssentialsData();

        assertEquals(0L, data.getKitLastClaimedMs("starter"), "an unclaimed kit should report 0, not throw/null");

        data.recordKitClaimed("Starter");
        assertTrue(data.getKitLastClaimedMs("STARTER") > 0);
    }

    @Test
    void homeLimitOverrideDefaultsToMinusOne() {
        PlayerEssentialsData data = new PlayerEssentialsData();
        assertEquals(-1, data.getHomeLimitOverride(), "-1 is the documented 'use the config default' sentinel");
    }

    @Test
    void deserializeDefaultsHomeLimitOverrideToMinusOneWhenAbsentFromOlderSaveData() {
        CompoundTag tag = new CompoundTag(); 

        PlayerEssentialsData restored = new PlayerEssentialsData();
        restored.deserializeNBT(tag);

        assertEquals(-1, restored.getHomeLimitOverride());
    }

    @Test
    void serializeNbtWritesEveryFieldToItsExpectedKey() {
        PlayerEssentialsData data = new PlayerEssentialsData();
        data.setNickname("Steve");
        data.setAfk(true);
        data.addPlaytimeTicks(12345);
        data.setHomeLimitOverride(7);
        UUID ignored = UUID.randomUUID();
        data.getIgnored().add(ignored);
        data.recordKitClaimed("starter");

        CompoundTag tag = data.serializeNBT();

        assertEquals("Steve", tag.getString("Nickname"));
        assertTrue(tag.getBoolean("Afk"));
        assertTrue(tag.getLong("AfkSince") > 0);
        assertEquals(12345L, tag.getLong("PlaytimeTicks"));
        assertEquals(7, tag.getInt("HomeLimitOverride"));
        assertEquals(1, tag.getList("Ignored", Tag.TAG_STRING).size());
        assertEquals(ignored.toString(), tag.getList("Ignored", Tag.TAG_STRING).getString(0));
        assertEquals(1, tag.getList("KitCooldowns", Tag.TAG_COMPOUND).size());
    }
}
