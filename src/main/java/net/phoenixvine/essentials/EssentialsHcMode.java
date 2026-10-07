package net.phoenixvine.essentials;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.phoenixvine.essentials.api.EssentialsAPI;
import net.phoenixvine.essentials.config.EssentialsPermissions;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Switch + parity helpers for the Hot Chocolate takeover (src/main/hotc, see its README).
//
// By default the HC ports own the real command names (/home, /warp, ...) and the Y key. Launch with
// `-Dphoenix_essentials.use_java=true` to put the original Java commands/screen back exactly as
// they were -- the HC versions then register as their `hc`-prefixed twins on the pilot key again.
public final class EssentialsHcMode {

    private static final boolean TAKEOVER = !Boolean.getBoolean("phoenix_essentials.use_java");
    private static final Map<String, PermissionNode<Boolean>> NODES = new ConcurrentHashMap<>();

    private EssentialsHcMode() {}

    public static boolean takeover() {
        return TAKEOVER;
    }

    // The HC side builds every literal as `prefix + name`-style via this: real name when taking
    // over, the `hc` twin otherwise.
    public static String literal(String realName, String twinName) {
        return TAKEOVER ? realName : twinName;
    }

    // Same gate the Java commands apply with `.requires(source -> EssentialsPermissions.check(...))`.
    // `nodeName` is the EssentialsPermissions field name ("HOME", "GOD", ...); empty = no gate.
    @SuppressWarnings("unchecked")
    public static boolean allowed(CommandSourceStack source, String nodeName) {
        if (nodeName == null || nodeName.isEmpty()) return true;
        PermissionNode<Boolean> node = NODES.get(nodeName);
        if (node == null) {
            try {
                Field f = EssentialsPermissions.class.getField(nodeName);
                node = (PermissionNode<Boolean>) f.get(null);
            } catch (ReflectiveOperationException e) {
                PhoenixEssentials.LOGGER.error("[hc] unknown permission node '{}'", nodeName, e);
                return false;
            }
            // The fields are filled during PermissionGatherEvent -- never cache a not-yet-set null.
            if (node != null) NODES.put(nodeName, node);
        }
        if (node == null) return true;
        return EssentialsPermissions.check(source, node);
    }

    // `feature` is one of "teleport" / "kits" / "social" (EssentialsAPI.FEATURE_*).
    public static boolean featureEnabled(ServerPlayer player, String feature) {
        return EssentialsAPI.isFeatureEnabled(feature, player.level().dimension().location());
    }
}
