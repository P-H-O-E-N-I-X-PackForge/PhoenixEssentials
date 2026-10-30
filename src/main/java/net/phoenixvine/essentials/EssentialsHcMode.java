package net.phoenixvine.essentials;

import net.minecraft.server.level.ServerPlayer;
import net.phoenixvine.essentials.api.EssentialsAPI;

public final class EssentialsHcMode {

    private static final boolean TAKEOVER = !Boolean.getBoolean("phoenix_essentials.use_java");

    private EssentialsHcMode() {}

    public static boolean takeover() {
        return TAKEOVER;
    }

    public static String literal(String realName, String twinName) {
        return TAKEOVER ? realName : twinName;
    }

    public static boolean featureEnabled(ServerPlayer player, String feature) {
        return EssentialsAPI.isFeatureEnabled(feature, player.level().dimension().location(), player);
    }
}
