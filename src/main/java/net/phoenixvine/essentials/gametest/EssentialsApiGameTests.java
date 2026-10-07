package net.phoenixvine.essentials.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.phoenixvine.essentials.PhoenixEssentials;
import net.phoenixvine.essentials.api.EssentialsAPI;
import net.phoenixvine.essentials.api.EssentialsFeatureState;

/** The public feature-gate / tier / state API (HC-backed in takeover mode, Java otherwise). */
@GameTestHolder(PhoenixEssentials.MOD_ID)
@PrefixGameTestTemplate(false)
public class EssentialsApiGameTests {

    private static final ResourceLocation DIM_A = new ResourceLocation("gametest", "dim_a");
    private static final ResourceLocation DIM_B = new ResourceLocation("gametest", "dim_b");

    @GameTest(template = "gametest_empty", timeoutTicks = 100)
    public static void unknownFeatureDefaultsToEnabled(GameTestHelper helper) {
        helper.assertTrue(EssentialsAPI.isFeatureEnabled("gt_never_configured", DIM_A), "unconfigured feature is enabled");
        helper.assertTrue(EssentialsAPI.getFeatureState("gt_never_configured", DIM_A) == EssentialsFeatureState.ENABLED,
                "unconfigured feature state is ENABLED");
        helper.succeed();
    }

    @GameTest(template = "gametest_empty", timeoutTicks = 100)
    public static void featureGateCanBeSetAndCleared(GameTestHelper helper) {
        EssentialsAPI.setFeatureEnabled("gt_gate", false);
        helper.assertTrue(!EssentialsAPI.isFeatureEnabled("gt_gate", DIM_A), "gate=false disables");
        boolean[] open = {false};
        EssentialsAPI.registerFeatureGate("gt_gate", () -> open[0]);
        helper.assertTrue(!EssentialsAPI.isFeatureEnabled("gt_gate", DIM_A), "supplier false -> disabled");
        open[0] = true;
        helper.assertTrue(EssentialsAPI.isFeatureEnabled("gt_gate", DIM_A), "supplier is re-evaluated each call");
        EssentialsAPI.clearFeatureGate("gt_gate");
        EssentialsAPI.setFeatureEnabled("gt_gate", false);
        EssentialsAPI.clearFeatureGate("gt_gate");
        helper.assertTrue(EssentialsAPI.isFeatureEnabled("gt_gate", DIM_A), "cleared gate -> enabled again");
        helper.succeed();
    }

    @GameTest(template = "gametest_empty", timeoutTicks = 100)
    public static void tierRequirementsGateFeaturesPerDimension(GameTestHelper helper) {
        helper.assertTrue(EssentialsAPI.getTier(DIM_A) == 0, "default tier is 0");
        EssentialsAPI.requireTier("gt_tier", DIM_A, 2);
        helper.assertTrue(!EssentialsAPI.isFeatureEnabled("gt_tier", DIM_A), "tier 0 < 2 -> disabled in A");
        helper.assertTrue(EssentialsAPI.isFeatureEnabled("gt_tier", DIM_B), "no requirement for B -> enabled");
        EssentialsAPI.setTier(DIM_A, 1);
        helper.assertTrue(!EssentialsAPI.isFeatureEnabled("gt_tier", DIM_A), "tier 1 < 2 -> disabled");
        EssentialsAPI.setTier(DIM_A, 2);
        helper.assertTrue(EssentialsAPI.isFeatureEnabled("gt_tier", DIM_A), "tier 2 >= 2 -> enabled");
        EssentialsAPI.setTier(DIM_A, 0);
        EssentialsAPI.clearTierRequirement("gt_tier", DIM_A);
        helper.assertTrue(EssentialsAPI.isFeatureEnabled("gt_tier", DIM_A), "requirement cleared -> enabled");
        helper.succeed();
    }

    @GameTest(template = "gametest_empty", timeoutTicks = 100)
    public static void featureStatesArePerDimensionAndOrdered(GameTestHelper helper) {
        EssentialsAPI.setFeatureState("gt_state", DIM_A, EssentialsFeatureState.VISIBLE);
        helper.assertTrue(EssentialsAPI.getFeatureState("gt_state", DIM_A) == EssentialsFeatureState.VISIBLE, "A is VISIBLE");
        helper.assertTrue(EssentialsAPI.getFeatureState("gt_state", DIM_B) == EssentialsFeatureState.ENABLED, "B defaults to ENABLED");
        EssentialsAPI.setFeatureState("gt_state", DIM_A, EssentialsFeatureState.DISABLED);
        helper.assertTrue(EssentialsAPI.getFeatureState("gt_state", DIM_A) == EssentialsFeatureState.DISABLED, "A overwritten to DISABLED");
        helper.succeed();
    }
}
