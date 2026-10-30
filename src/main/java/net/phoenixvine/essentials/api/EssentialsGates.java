package net.phoenixvine.essentials.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.phoenixvine.wiki.api.gate.SuiteGates;

/**
 * Essentials features as features of the shared gate API ({@link SuiteGates}): another mod binds them to research or
 * anything else, e.g. {@code SuiteGates.bind(EssentialsGates.TELEPORT, "*", "conflux:node/foo")}. A feature that nothing
 * is bound to is allowed. The scope is the dimension id the player is in.
 */
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
