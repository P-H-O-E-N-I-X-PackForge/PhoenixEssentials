package net.phoenixvine.essentials.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.phoenixvine.wiki.api.gate.SuiteGates;

public final class EssentialsGates {

    public static final String TELEPORT = "essentials:" + EssentialsAPI.FEATURE_TELEPORT;
    public static final String KITS = "essentials:" + EssentialsAPI.FEATURE_KITS;
    public static final String SOCIAL = "essentials:" + EssentialsAPI.FEATURE_SOCIAL;

    private EssentialsGates() {}

    public static String feature(String featureId) {
        return "essentials:" + featureId;
    }

    public static boolean allowed(ServerPlayer player, String featureId, ResourceLocation dimension) {
        return SuiteGates.allowed(player, feature(featureId), dimension.toString());
    }
}
