package net.phoenixvine.essentials.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.phoenixvine.essentials.capability.EssentialsCapabilityProvider;
import net.phoenixvine.essentials.capability.PlayerEssentialsData;
import net.phoenixvine.essentials.command.TeleportExecutor;
import net.phoenixvine.essentials.data.KitDefinition;
import net.phoenixvine.essentials.data.KitRegistry;
import net.phoenixvine.essentials.data.NamedLocation;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;

public final class EssentialsAPI {

    private EssentialsAPI() {}

    public static Map<String, NamedLocation> getHomes(ServerPlayer player) {
        PlayerEssentialsData data = data(player);
        return data == null ? Map.of() : Map.copyOf(data.getHomes());
    }

    public static NamedLocation getBack(ServerPlayer player) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) return net.phoenixvine.essentials.hc.hcCompileMerged1.api_get_back(player);
        PlayerEssentialsData data = data(player);
        return data == null ? null : data.getBack();
    }

    public static String getNickname(ServerPlayer player) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) return net.phoenixvine.essentials.hc.hcCompileMerged1.api_get_nickname(player);
        PlayerEssentialsData data = data(player);
        return data == null ? "" : data.getNickname();
    }

    public static boolean isAfk(ServerPlayer player) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) return net.phoenixvine.essentials.hc.hcCompileMerged1.api_is_afk(player) != 0;
        PlayerEssentialsData data = data(player);
        return data != null && data.isAfk();
    }

    public static long getPlaytimeTicks(ServerPlayer player) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) return net.phoenixvine.essentials.hc.hcCompileMerged1.api_get_playtime_ticks(player);
        PlayerEssentialsData data = data(player);
        return data == null ? 0 : data.getPlaytimeTicks();
    }

    public static boolean isIgnoring(ServerPlayer player, java.util.UUID other) {
        PlayerEssentialsData data = data(player);
        return data != null && data.isIgnoring(other);
    }

    private static PlayerEssentialsData data(ServerPlayer player) {
        return player.getCapability(EssentialsCapabilityProvider.PLAYER_ESSENTIALS).orElse(null);
    }

    public static void registerKit(KitDefinition kit) {
        KitRegistry.set(kit);
    }

    public static KitDefinition getKit(String name) {
        return KitRegistry.get(name);
    }

    public static void teleport(ServerPlayer player, NamedLocation target, String category, String destinationLabel) {
        TeleportExecutor.request(player, target, category, destinationLabel);
    }

    public static final String FEATURE_TELEPORT = "teleport";
    public static final String FEATURE_KITS = "kits";
    public static final String FEATURE_SOCIAL = "social";

    private static final Map<String, BooleanSupplier> FEATURE_GATES = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, Integer> DIMENSION_TIERS = new ConcurrentHashMap<>();
    private static final Map<String, Map<ResourceLocation, Integer>> TIER_REQUIREMENTS = new ConcurrentHashMap<>();
    private static final Map<String, Map<ResourceLocation, EssentialsFeatureState>> FEATURE_STATES =
            new ConcurrentHashMap<>();

    private static final Set<String> KNOWN_FEATURE_IDS = ConcurrentHashMap.newKeySet();
    private static final Set<String> WARNED_UNKNOWN_FEATURE_IDS = ConcurrentHashMap.newKeySet();

    static {
        KNOWN_FEATURE_IDS.addAll(Set.of(FEATURE_TELEPORT, FEATURE_KITS, FEATURE_SOCIAL));
    }

    public static synchronized void registerFeatureGate(String featureId, BooleanSupplier check) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) {
            net.phoenixvine.essentials.hc.hcCompileMerged1.api_register_gate(featureId, check);
            return;
        }
        KNOWN_FEATURE_IDS.add(featureId);
        FEATURE_GATES.put(featureId, check);
    }

    public static void setFeatureEnabled(String featureId, boolean enabled) {
        registerFeatureGate(featureId, () -> enabled);
    }

    public static synchronized void clearFeatureGate(String featureId) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) {
            net.phoenixvine.essentials.hc.hcCompileMerged1.api_clear_gate(featureId);
            return;
        }
        FEATURE_GATES.remove(featureId);
    }

    public static synchronized void setTier(ResourceLocation dimension, int tier) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) {
            net.phoenixvine.essentials.hc.hcCompileMerged1.api_set_tier(dimension.toString(), tier);
            return;
        }
        DIMENSION_TIERS.put(dimension, tier);
    }

    public static synchronized int getTier(ResourceLocation dimension) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) return net.phoenixvine.essentials.hc.hcCompileMerged1.api_get_tier(dimension.toString());
        return DIMENSION_TIERS.getOrDefault(dimension, 0);
    }

    public static synchronized void requireTier(String featureId, ResourceLocation dimension, int requiredTier) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) {
            net.phoenixvine.essentials.hc.hcCompileMerged1.api_require_tier(featureId, dimension.toString(), requiredTier);
            return;
        }
        KNOWN_FEATURE_IDS.add(featureId);
        TIER_REQUIREMENTS.computeIfAbsent(featureId, id -> new ConcurrentHashMap<>()).put(dimension, requiredTier);
    }

    public static synchronized void clearTierRequirement(String featureId, ResourceLocation dimension) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) {
            net.phoenixvine.essentials.hc.hcCompileMerged1.api_clear_tier_requirement(featureId, dimension.toString());
            return;
        }
        Map<ResourceLocation, Integer> perDimension = TIER_REQUIREMENTS.get(featureId);
        if (perDimension != null) perDimension.remove(dimension);
    }

    /** The feature is on for the dimension, and the shared gate API lets this player use it. */
    public static boolean isFeatureEnabled(String featureId, ResourceLocation dimension,
                                           net.minecraft.server.level.ServerPlayer player) {
        return isFeatureEnabled(featureId, dimension) && EssentialsGates.allowed(player, featureId, dimension);
    }

    public static synchronized boolean isFeatureEnabled(String featureId, ResourceLocation dimension) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) return net.phoenixvine.essentials.hc.hcCompileMerged1.api_is_feature_enabled(featureId, dimension.toString()) != 0;
        warnIfUnknown(featureId);
        if (!checkGate(featureId)) return false;

        Map<ResourceLocation, Integer> perDimension = TIER_REQUIREMENTS.get(featureId);
        if (perDimension == null) return true;

        Integer required = perDimension.get(dimension);
        return required == null || getTier(dimension) >= required;
    }

    private static boolean checkGate(String featureId) {
        BooleanSupplier check = FEATURE_GATES.get(featureId);
        if (check == null) return true;
        return check.getAsBoolean();
    }

    private static void warnIfUnknown(String featureId) {
        if (!KNOWN_FEATURE_IDS.contains(featureId) && WARNED_UNKNOWN_FEATURE_IDS.add(featureId)) {
            net.phoenixvine.essentials.PhoenixEssentials.LOGGER.debug(
                    "Feature id '{}' was queried but has never been gated, tiered, or given an explicit" +
                            " state - defaulting to enabled. Fine if that's intentional; if not, check for a" +
                            " typo against whatever was supposed to configure it.",
                    featureId);
        }
    }

    public static synchronized void setFeatureState(String featureId, ResourceLocation dimension, EssentialsFeatureState state) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) {
            net.phoenixvine.essentials.hc.hcCompileMerged1.api_set_feature_state(featureId, dimension.toString(), state.ordinal());
            return;
        }
        KNOWN_FEATURE_IDS.add(featureId);
        FEATURE_STATES.computeIfAbsent(featureId, id -> new ConcurrentHashMap<>()).put(dimension, state);
    }

    public static synchronized EssentialsFeatureState getFeatureState(String featureId, ResourceLocation dimension) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) return EssentialsFeatureState.values()[net.phoenixvine.essentials.hc.hcCompileMerged1.api_get_feature_state(featureId, dimension.toString())];
        warnIfUnknown(featureId);
        Map<ResourceLocation, EssentialsFeatureState> perDimension = FEATURE_STATES.get(featureId);
        return perDimension == null ? EssentialsFeatureState.ENABLED :
                perDimension.getOrDefault(dimension, EssentialsFeatureState.ENABLED);
    }
}
