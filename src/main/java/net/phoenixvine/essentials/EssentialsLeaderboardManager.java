package net.phoenixvine.essentials;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.block.Block;
import net.phoenixvine.essentials.config.EssentialsServerConfig;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Tracks the cached, persisted leaderboard data: one row per player who has ever been
 * seen, refreshed from their live vanilla stats + playtime while online. Rows store both
 * an all-time value and a "season baseline" snapshot, so a season's value is just
 * current - baseline; rolling the season over resets the baseline and hands out crowns.
 */
public final class EssentialsLeaderboardManager {

    private EssentialsLeaderboardManager() {}

    public enum Category {
        PLAYTIME("Playtime"),
        MOB_KILLS("Mob Kills"),
        DEATHS("Deaths"),
        BLOCKS_MINED("Blocks Mined"),
        DISTANCE("Distance Traveled");

        public final String label;

        Category(String label) {
            this.label = label;
        }

        public static Category byId(int id) {
            Category[] values = values();
            return values[Math.floorMod(id, values.length)];
        }
    }

    private static final class Row {
        String name = "";
        long playtime;
        long kills;
        long deaths;
        long mined;
        long distance;
        long basePlaytime;
        long baseKills;
        long baseDeaths;
        long baseMined;
        long baseDistance;
        int crowns;
    }

    private static final class SeasonState {
        long startMs;
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = Paths.get("config", "phoenix_essentials", "leaderboards.json");
    private static final Path SEASON_FILE = Paths.get("config", "phoenix_essentials", "leaderboard_season.json");

    private static final Map<UUID, Row> ROWS = new LinkedHashMap<>();
    private static long seasonStartMs = System.currentTimeMillis();
    private static boolean loaded = false;

    public static void load() {
        ROWS.clear();
        loaded = true;
        try {
            if (Files.exists(FILE)) {
                Type type = new TypeToken<Map<String, Row>>() {}.getType();
                Map<String, Row> parsed = GSON.fromJson(Files.readString(FILE), type);
                if (parsed != null) {
                    parsed.forEach((key, row) -> {
                        try {
                            ROWS.put(UUID.fromString(key), row);
                        } catch (IllegalArgumentException ignored) {}
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (Files.exists(SEASON_FILE)) {
                SeasonState state = GSON.fromJson(Files.readString(SEASON_FILE), SeasonState.class);
                if (state != null && state.startMs > 0) seasonStartMs = state.startMs;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void ensureLoaded() {
        if (!loaded) load();
    }

    public static void persist() {
        ensureLoaded();
        try {
            Files.createDirectories(FILE.getParent());
            Map<String, Row> out = new LinkedHashMap<>();
            ROWS.forEach((uuid, row) -> out.put(uuid.toString(), row));
            Files.writeString(FILE, GSON.toJson(out));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void persistSeason() {
        try {
            Files.createDirectories(SEASON_FILE.getParent());
            SeasonState state = new SeasonState();
            state.startMs = seasonStartMs;
            Files.writeString(SEASON_FILE, GSON.toJson(state));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static long statMobKills(ServerPlayer player) {
        return player.getStats().getValue(Stats.CUSTOM.get(Stats.MOB_KILLS));
    }

    private static long statDeaths(ServerPlayer player) {
        return player.getStats().getValue(Stats.CUSTOM.get(Stats.DEATHS));
    }

    private static long statBlocksMined(ServerPlayer player) {
        long total = 0L;
        for (Block block : BuiltInRegistries.BLOCK) {
            total += player.getStats().getValue(Stats.BLOCK_MINED.get(block));
        }
        return total;
    }

    /** Sum of every vanilla "moved one cm" stat, converted from centimeters to whole blocks. */
    private static long statDistanceBlocks(ServerPlayer player) {
        long cm = 0L;
        cm += player.getStats().getValue(Stats.CUSTOM.get(Stats.WALK_ONE_CM));
        cm += player.getStats().getValue(Stats.CUSTOM.get(Stats.SPRINT_ONE_CM));
        cm += player.getStats().getValue(Stats.CUSTOM.get(Stats.CROUCH_ONE_CM));
        cm += player.getStats().getValue(Stats.CUSTOM.get(Stats.SWIM_ONE_CM));
        cm += player.getStats().getValue(Stats.CUSTOM.get(Stats.FALL_ONE_CM));
        cm += player.getStats().getValue(Stats.CUSTOM.get(Stats.CLIMB_ONE_CM));
        cm += player.getStats().getValue(Stats.CUSTOM.get(Stats.FLY_ONE_CM));
        cm += player.getStats().getValue(Stats.CUSTOM.get(Stats.BOAT_ONE_CM));
        cm += player.getStats().getValue(Stats.CUSTOM.get(Stats.MINECART_ONE_CM));
        cm += player.getStats().getValue(Stats.CUSTOM.get(Stats.HORSE_ONE_CM));
        cm += player.getStats().getValue(Stats.CUSTOM.get(Stats.PIG_ONE_CM));
        cm += player.getStats().getValue(Stats.CUSTOM.get(Stats.AVIATE_ONE_CM));
        return cm / 100L;
    }

    /**
     * Refreshes one online player's cached row in memory from their live stats. Cheap and safe
     * to call often; call {@link #persist()} separately to actually write it to disk.
     */
    public static void refresh(ServerPlayer player, long playtimeTicks) {
        ensureLoaded();
        Row row = ROWS.computeIfAbsent(player.getUUID(), uuid -> {
            Row fresh = new Row();
            fresh.baseKills = statMobKills(player);
            fresh.baseDeaths = statDeaths(player);
            fresh.baseMined = statBlocksMined(player);
            fresh.baseDistance = statDistanceBlocks(player);
            return fresh;
        });
        row.name = player.getGameProfile().getName();
        row.playtime = playtimeTicks / 20L;
        row.kills = statMobKills(player);
        row.deaths = statDeaths(player);
        row.mined = statBlocksMined(player);
        row.distance = statDistanceBlocks(player);
    }

    private static long currentValue(Row row, Category category) {
        return switch (category) {
            case PLAYTIME -> row.playtime;
            case MOB_KILLS -> row.kills;
            case DEATHS -> row.deaths;
            case BLOCKS_MINED -> row.mined;
            case DISTANCE -> row.distance;
        };
    }

    private static long baseValue(Row row, Category category) {
        return switch (category) {
            case PLAYTIME -> row.basePlaytime;
            case MOB_KILLS -> row.baseKills;
            case DEATHS -> row.baseDeaths;
            case BLOCKS_MINED -> row.baseMined;
            case DISTANCE -> row.baseDistance;
        };
    }

    private static long seasonOrAllTime(Row row, Category category, boolean season) {
        long current = currentValue(row, category);
        if (!season) return current;
        return Math.max(0L, current - baseValue(row, category));
    }

    public static final class Entry {
        public final String name;
        public final long value;
        public final int crowns;

        Entry(String name, long value, int crowns) {
            this.name = name;
            this.value = value;
            this.crowns = crowns;
        }
    }

    public static List<Entry> top(Category category, boolean season, int limit) {
        ensureLoaded();
        return ROWS.values().stream()
                .filter(row -> !row.name.isEmpty())
                .sorted((a, b) -> Long.compare(seasonOrAllTime(b, category, season), seasonOrAllTime(a, category, season)))
                .limit(Math.max(0, limit))
                .map(row -> new Entry(row.name, seasonOrAllTime(row, category, season), row.crowns))
                .collect(Collectors.toList());
    }

    /** "name:value:crowns,name2:value2:crowns2,..." - the raw numeric value, unformatted. */
    public static String rawCsvFor(int categoryId, boolean season, int limit) {
        Category category = Category.byId(categoryId);
        List<Entry> entries = top(category, season, limit);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < entries.size(); i++) {
            if (i > 0) sb.append(',');
            Entry entry = entries.get(i);
            sb.append(entry.name).append(':').append(entry.value).append(':').append(entry.crowns);
        }
        return sb.toString();
    }

    public static int seasonDaysConfigured() {
        return EssentialsServerConfig.LEADERBOARD_SEASON_DAYS.get();
    }

    public static long seasonDaysLeft() {
        ensureLoaded();
        int days = seasonDaysConfigured();
        if (days <= 0) return -1L;
        long elapsedMs = System.currentTimeMillis() - seasonStartMs;
        long remainingMs = (days * 86_400_000L) - elapsedMs;
        return Math.max(0L, remainingMs / 86_400_000L);
    }

    /**
     * Checked periodically from a tick handler. When the configured season length has elapsed,
     * crowns the current #1 player in each category (if they have a nonzero season value),
     * broadcasts it, and resets every row's season baseline so the next season starts at zero.
     */
    public static void maybeRollover(MinecraftServer server) {
        ensureLoaded();
        int days = seasonDaysConfigured();
        if (days <= 0) return;
        long elapsedMs = System.currentTimeMillis() - seasonStartMs;
        if (elapsedMs < days * 86_400_000L) return;

        for (Category category : Category.values()) {
            List<Entry> winners = top(category, true, 1);
            if (winners.isEmpty() || winners.get(0).value <= 0) continue;
            Entry winner = winners.get(0);
            ROWS.values().stream()
                    .filter(row -> row.name.equals(winner.name))
                    .findFirst()
                    .ifPresent(row -> row.crowns++);
            if (server != null) {
                server.getPlayerList().broadcastSystemMessage(Component.literal(
                        "§6✦ " + winner.name + " wins this season's " + category.label
                                + " leaderboard with " + winner.value + "!"), false);
            }
        }

        for (Row row : ROWS.values()) {
            row.basePlaytime = row.playtime;
            row.baseKills = row.kills;
            row.baseDeaths = row.deaths;
            row.baseMined = row.mined;
            row.baseDistance = row.distance;
        }
        seasonStartMs = System.currentTimeMillis();
        persist();
        persistSeason();
    }
}
