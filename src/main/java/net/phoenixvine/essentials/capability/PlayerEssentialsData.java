package net.phoenixvine.essentials.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.phoenixvine.essentials.data.NamedLocation;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerEssentialsData {

    private final Map<String, NamedLocation> homes = new LinkedHashMap<>();
    private NamedLocation back;
    private String nickname = "";
    private boolean afk = false;
    private long afkSinceMs = 0L;
    private final Set<UUID> ignored = new HashSet<>();
    private long playtimeTicks = 0L;
    private final Map<String, Long> kitCooldownsMs = new LinkedHashMap<>();
    private int homeLimitOverride = -1;

    public Map<String, NamedLocation> getHomes() {
        return homes;
    }

    public NamedLocation getHome(String name) {
        return homes.get(name.toLowerCase());
    }

    public void setHome(String name, NamedLocation loc) {
        homes.put(name.toLowerCase(), loc);
    }

    public boolean deleteHome(String name) {
        return homes.remove(name.toLowerCase()) != null;
    }

    public NamedLocation getBack() {
        return back;
    }

    public void setBack(NamedLocation back) {
        this.back = back;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname == null ? "" : nickname;
    }

    public boolean isAfk() {
        return afk;
    }

    public long getAfkSinceMs() {
        return afkSinceMs;
    }

    public void setAfk(boolean afk) {
        this.afk = afk;
        this.afkSinceMs = afk ? System.currentTimeMillis() : 0L;
    }

    public Set<UUID> getIgnored() {
        return ignored;
    }

    public boolean isIgnoring(UUID other) {
        return ignored.contains(other);
    }

    public long getPlaytimeTicks() {
        return playtimeTicks;
    }

    public void addPlaytimeTicks(long ticks) {
        playtimeTicks += ticks;
    }

    public long getKitLastClaimedMs(String kitName) {
        return kitCooldownsMs.getOrDefault(kitName.toLowerCase(), 0L);
    }

    public void recordKitClaimed(String kitName) {
        kitCooldownsMs.put(kitName.toLowerCase(), System.currentTimeMillis());
    }

    public int getHomeLimitOverride() {
        return homeLimitOverride;
    }

    public void setHomeLimitOverride(int homeLimitOverride) {
        this.homeLimitOverride = homeLimitOverride;
    }

    public CompoundTag serializeNBT() {
        CompoundTag root = new CompoundTag();

        ListTag homesList = new ListTag();
        homes.forEach((name, loc) -> {
            CompoundTag e = new CompoundTag();
            e.putString("name", name);
            e.put("loc", loc.serialize());
            homesList.add(e);
        });
        root.put("Homes", homesList);

        if (back != null) root.put("Back", back.serialize());
        root.putString("Nickname", nickname);
        root.putBoolean("Afk", afk);
        root.putLong("AfkSince", afkSinceMs);
        root.putLong("PlaytimeTicks", playtimeTicks);
        root.putInt("HomeLimitOverride", homeLimitOverride);

        ListTag ignoredList = new ListTag();
        for (UUID u : ignored) ignoredList.add(net.minecraft.nbt.StringTag.valueOf(u.toString()));
        root.put("Ignored", ignoredList);

        ListTag kitCooldownList = new ListTag();
        kitCooldownsMs.forEach((name, ms) -> {
            CompoundTag e = new CompoundTag();
            e.putString("name", name);
            e.putLong("time", ms);
            kitCooldownList.add(e);
        });
        root.put("KitCooldowns", kitCooldownList);

        return root;
    }

    public void deserializeNBT(CompoundTag root) {
        homes.clear();
        back = null;
        ignored.clear();
        kitCooldownsMs.clear();

        ListTag homesList = root.getList("Homes", Tag.TAG_COMPOUND);
        for (int i = 0; i < homesList.size(); i++) {
            CompoundTag e = homesList.getCompound(i);
            try {
                homes.put(e.getString("name"), NamedLocation.deserialize(e.getCompound("loc")));
            } catch (Exception ignoredEx) {}
        }

        if (root.contains("Back")) {
            try {
                back = NamedLocation.deserialize(root.getCompound("Back"));
            } catch (Exception ignoredEx) {}
        }

        nickname = root.getString("Nickname");
        afk = root.getBoolean("Afk");
        afkSinceMs = root.getLong("AfkSince");
        playtimeTicks = root.getLong("PlaytimeTicks");
        homeLimitOverride = root.contains("HomeLimitOverride") ? root.getInt("HomeLimitOverride") : -1;

        ListTag ignoredList = root.getList("Ignored", Tag.TAG_STRING);
        for (int i = 0; i < ignoredList.size(); i++) {
            try {
                ignored.add(UUID.fromString(ignoredList.getString(i)));
            } catch (IllegalArgumentException ignoredEx) {}
        }

        ListTag kitCooldownList = root.getList("KitCooldowns", Tag.TAG_COMPOUND);
        for (int i = 0; i < kitCooldownList.size(); i++) {
            CompoundTag e = kitCooldownList.getCompound(i);
            kitCooldownsMs.put(e.getString("name"), e.getLong("time"));
        }
    }
}
