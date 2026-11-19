package net.phoenixvine.essentials.config;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.phoenixvine.essentials.PhoenixEssentials;
import net.phoenixvine.wiki.api.gate.SuiteGates;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Any server setting, different for each player as gates open.
 * <p>
 * {@link EssentialsServerConfig} holds the base values. {@code config/phoenix_essentials_gates.json} lists <b>tiers</b>:
 * each has a {@code when} (a {@link SuiteGates} expression such as {@code conflux:node/phoenixcore:foo | !solaris:x}), an
 * optional {@code scope} (a dimension id, or {@code *}, the default) and a {@code set} of settings named as in the
 * server config ({@code homesPerPlayer}, {@code rtpMaxRadius}, {@code enableRtp}...). A player's settings are the base
 * values with every open tier applied in the order written, later tiers winning, so a pack lists its tiers from the
 * lowest up. A blank {@code when} is always open.
 */
public final class EssentialsPolicy {

    private record Tier(String when, String scope, JsonObject set) {}

    private record Cached(long tick, String dimension, Map<String, Object> values) {}

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "phoenix_essentials_gates.json";
    private static final long CACHE_TICKS = 20L;

    /** The server config's value objects, by the name they have in the config. Filled in as the config is built. */
    private static final Map<ModConfigSpec.ConfigValue<?>, String> KEYS = new IdentityHashMap<>();
    private static final Map<String, ModConfigSpec.ConfigValue<?>> BY_KEY = new ConcurrentHashMap<>();

    private static final List<Tier> TIERS = new ArrayList<>();
    private static final Map<UUID, Cached> CACHE = new ConcurrentHashMap<>();
    private static long loadedModified = -1L;
    private static long lastCheckedNanos = 0L;

    private EssentialsPolicy() {}

    static void register(String key, ModConfigSpec.ConfigValue<?> value) {
        KEYS.put(value, key);
        BY_KEY.put(key, value);
    }

    // *** The tier file ***

    private static Path file() {
        return FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);
    }

    private static synchronized void reloadIfChanged() {
        long now = System.nanoTime();
        if (now - lastCheckedNanos < 1_000_000_000L && loadedModified != -1L) return;
        lastCheckedNanos = now;
        Path path = file();
        try {
            if (!Files.exists(path)) writeExample(path);
            long modified = Files.getLastModifiedTime(path).toMillis();
            if (modified == loadedModified) return;
            JsonObject root = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
            List<Tier> read = new ArrayList<>();
            if (root.has("tiers")) {
                for (JsonElement e : root.getAsJsonArray("tiers")) {
                    JsonObject o = e.getAsJsonObject();
                    String when = o.has("when") ? o.get("when").getAsString() : "";
                    String scope = o.has("scope") ? o.get("scope").getAsString() : SuiteGates.ANY_SCOPE;
                    JsonObject set = o.has("set") ? o.getAsJsonObject("set") : new JsonObject();
                    for (String key : set.keySet()) {
                        if (!BY_KEY.containsKey(key)) {
                            PhoenixEssentials.LOGGER.warn("[phoenix_essentials] {}: '{}' is not a server setting", FILE_NAME,
                                    key);
                        }
                    }
                    read.add(new Tier(when, scope, set));
                }
            }
            TIERS.clear();
            TIERS.addAll(read);
            CACHE.clear();
            loadedModified = modified;
            PhoenixEssentials.LOGGER.info("[phoenix_essentials] {} tier(s) from {}", TIERS.size(), FILE_NAME);
        } catch (Exception e) {
            PhoenixEssentials.LOGGER.warn("[phoenix_essentials] could not read {}: {}", FILE_NAME, e.toString());
            if (loadedModified == -1L) loadedModified = 0L;
        }
    }

    private static void writeExample(Path path) throws IOException {
        JsonObject root = new JsonObject();
        root.addProperty("_about", "Tiers change Phoenix Essentials' settings per player as gates open. 'when' is a gate " +
                "expression (a | b & !c; blank is always open), 'scope' a dimension id or *, and 'set' any setting of " +
                "the server config by its name, e.g. homesPerPlayer, teleportCooldownSeconds, rtpMaxRadius, " +
                "rtpAllowedDimensions, enableRtp. Tiers apply in order, so list them from the lowest up. Example: " +
                "{ \"when\": \"conflux:discipline\", \"set\": { \"homesPerPlayer\": 3 } }");
        root.add("tiers", new JsonArray());
        Files.createDirectories(path.getParent());
        Files.writeString(path, GSON.toJson(root));
    }

    private static boolean scopeMatches(String scope, String dimension) {
        return scope == null || scope.isBlank() || SuiteGates.ANY_SCOPE.equals(scope) || scope.equals(dimension);
    }

    // *** Resolving ***

    private static Map<String, Object> resolve(ServerPlayer player) {
        reloadIfChanged();
        String dimension = player.level().dimension().location().toString();
        long now = player.level().getGameTime();
        Cached cached = CACHE.get(player.getUUID());
        if (cached != null && now - cached.tick < CACHE_TICKS && cached.dimension.equals(dimension)) {
            return cached.values;
        }
        Map<String, Object> values = new ConcurrentHashMap<>();
        for (Tier tier : TIERS) {
            if (!scopeMatches(tier.scope, dimension)) continue;
            if (!SuiteGates.isOpen(player, tier.when)) continue;
            for (Map.Entry<String, JsonElement> e : tier.set.entrySet()) {
                ModConfigSpec.ConfigValue<?> base = BY_KEY.get(e.getKey());
                if (base == null) continue;
                try {
                    values.put(e.getKey(), coerce(base.get(), e.getValue()));
                } catch (RuntimeException bad) {
                    PhoenixEssentials.LOGGER.warn("[phoenix_essentials] {}: bad value for '{}'", FILE_NAME, e.getKey());
                }
            }
        }
        CACHE.put(player.getUUID(), new Cached(now, dimension, values));
        return values;
    }

    /** The JSON value as the same kind of thing the config value holds. */
    private static Object coerce(Object like, JsonElement v) {
        if (like instanceof Boolean) return v.getAsBoolean();
        if (like instanceof Integer) return v.getAsInt();
        if (like instanceof Long) return v.getAsLong();
        if (like instanceof Double) return v.getAsDouble();
        if (like instanceof List) {
            List<String> out = new ArrayList<>();
            if (v.isJsonArray()) v.getAsJsonArray().forEach(x -> out.add(x.getAsString()));
            else out.add(v.getAsString());
            return out;
        }
        return v.getAsString();
    }

    public static void forget(UUID player) {
        CACHE.remove(player);
    }

    /** The value of a server setting for this player: the last open tier that sets it, else the config's own. */
    @SuppressWarnings("unchecked")
    public static <T> T get(ServerPlayer player, ModConfigSpec.ConfigValue<T> config) {
        if (player == null) return config.get();
        String key = KEYS.get(config);
        if (key == null) return config.get();
        Object over = resolve(player).get(key);
        return over == null ? config.get() : (T) over;
    }

    public static boolean get(ServerPlayer player, ModConfigSpec.BooleanValue config) {
        return get(player, (ModConfigSpec.ConfigValue<Boolean>) config);
    }

    public static int get(ServerPlayer player, ModConfigSpec.IntValue config) {
        return get(player, (ModConfigSpec.ConfigValue<Integer>) config);
    }

    /** Whether a setting is set to true in any tier: a command that is off in the config must still exist to be turned on. */
    public static boolean anyTierSetsTrue(String key) {
        reloadIfChanged();
        for (Tier tier : TIERS) {
            JsonElement v = tier.set.get(key);
            if (v != null && v.isJsonPrimitive() && v.getAsBoolean()) return true;
        }
        return false;
    }
}
