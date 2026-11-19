package net.phoenixvine.essentials.config;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLPaths;
import net.phoenixvine.essentials.PhoenixEssentials;
import net.phoenixvine.wiki.api.gate.SuiteGates;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Which warps and kits a player may use, by gate: {@code config/phoenix_essentials/gate_rules.json}.
 * <pre>
 * { "warps": { "deep_vault": "conflux:node/phoenixcore:vault_access" },
 *   "kits":  { "starter": "!conflux:discipline", "builder": "conflux:discipline/sculk | conflux:discipline/void" } }
 * </pre>
 * A name that is not listed is open to everyone. The expression is a {@link SuiteGates} expression, so it can ask about
 * research ({@code conflux:node/...}), a committed discipline, or any gate another mod registers. A warp or kit that is
 * not open to the player is hidden from their lists and refuses to work.
 */
public final class EssentialsGateRules {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, String> WARPS = new HashMap<>();
    private static final Map<String, String> KITS = new HashMap<>();
    private static long loadedModified = -1L;
    private static long lastCheckedNanos = 0L;

    private EssentialsGateRules() {}

    private static Path file() {
        return FMLPaths.CONFIGDIR.get().resolve("phoenix_essentials").resolve("gate_rules.json");
    }

    private static synchronized void reloadIfChanged() {
        long now = System.nanoTime();
        if (now - lastCheckedNanos < 1_000_000_000L && loadedModified != -1L) return;
        lastCheckedNanos = now;
        Path path = file();
        try {
            if (!Files.exists(path)) {
                JsonObject root = new JsonObject();
                root.addProperty("_about", "Gate expressions for single warps and kits, by name. A name that is not " +
                        "listed is open to everyone. Example: \"warps\": { \"deep_vault\": \"conflux:node/phoenixcore:vault_access\" }");
                root.add("warps", new JsonObject());
                root.add("kits", new JsonObject());
                Files.createDirectories(path.getParent());
                Files.writeString(path, GSON.toJson(root));
            }
            long modified = Files.getLastModifiedTime(path).toMillis();
            if (modified == loadedModified) return;
            JsonObject root = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
            WARPS.clear();
            KITS.clear();
            read(root, "warps", WARPS);
            read(root, "kits", KITS);
            loadedModified = modified;
        } catch (Exception e) {
            PhoenixEssentials.LOGGER.warn("[phoenix_essentials] could not read gate_rules.json: {}", e.toString());
            if (loadedModified == -1L) loadedModified = 0L;
        }
    }

    private static void read(JsonObject root, String key, Map<String, String> into) {
        if (!root.has(key)) return;
        for (var e : root.getAsJsonObject(key).entrySet()) {
            into.put(e.getKey().toLowerCase(Locale.ROOT), e.getValue().getAsString());
        }
    }

    public static boolean warpAllowed(ServerPlayer player, String name) {
        reloadIfChanged();
        String expression = WARPS.get(name.toLowerCase(Locale.ROOT));
        return expression == null || SuiteGates.isOpen(player, expression);
    }

    public static boolean kitAllowed(ServerPlayer player, String name) {
        reloadIfChanged();
        String expression = KITS.get(name.toLowerCase(Locale.ROOT));
        return expression == null || SuiteGates.isOpen(player, expression);
    }
}
