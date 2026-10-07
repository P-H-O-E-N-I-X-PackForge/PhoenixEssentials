package net.phoenixvine.essentials;

import net.minecraft.server.level.ServerPlayer;
import net.phoenixvine.essentials.api.EssentialsAPI;


// Switch + parity helpers for the Hot Chocolate takeover (src/main/hotc, see its README).
//
// By default the HC ports own the real command names (/home, /warp, ...) and the Y key. Launch with
// `-Dphoenix_essentials.use_java=true` to put the original Java commands/screen back exactly as
// they were -- the HC versions then register as their `hc`-prefixed twins on the pilot key again.
public final class EssentialsHcMode {

    private static final boolean TAKEOVER = !Boolean.getBoolean("phoenix_essentials.use_java");

    private EssentialsHcMode() {}

    public static boolean takeover() {
        return TAKEOVER;
    }

    // The HC side builds every literal as `prefix + name`-style via this: real name when taking
    // over, the `hc` twin otherwise.
    public static String literal(String realName, String twinName) {
        return TAKEOVER ? realName : twinName;
    }

    // `feature` is one of "teleport" / "kits" / "social" (EssentialsAPI.FEATURE_*).
    public static boolean featureEnabled(ServerPlayer player, String feature) {
        return EssentialsAPI.isFeatureEnabled(feature, player.level().dimension().location());
    }
}
