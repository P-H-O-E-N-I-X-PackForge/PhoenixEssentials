package net.phoenixvine.essentials.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.phoenixvine.essentials.PhoenixEssentials;
import net.phoenixvine.essentials.client.EssentialsUIKit;

@GameTestHolder(PhoenixEssentials.MOD_ID)
@PrefixGameTestTemplate(false)
public class EssentialsUiKitGameTests {

    @GameTest(template = "gametest_empty", timeoutTicks = 100)
    public static void parseHexColorHandlesCommonInputs(GameTestHelper helper) {
        helper.assertTrue(EssentialsUIKit.parseHexColor("#FF8800", 7) == 0xFF8800, "#RRGGBB");
        helper.assertTrue(EssentialsUIKit.parseHexColor("ff8800", 7) == 0xFF8800, "no hash, lower-case");
        helper.assertTrue(EssentialsUIKit.parseHexColor("#FFFFFFFF", 7) == -1, "8 digits truncate to a 32-bit int");
        helper.assertTrue(EssentialsUIKit.parseHexColor(null, 7) == 7, "null -> fallback");
        helper.assertTrue(EssentialsUIKit.parseHexColor("   ", 7) == 7, "blank -> fallback");
        helper.assertTrue(EssentialsUIKit.parseHexColor("zzzzzz", 7) == 7, "garbage -> fallback");
        helper.succeed();
    }

    @GameTest(template = "gametest_empty", timeoutTicks = 100)
    public static void formatHexColorDropsAlphaAndUppercases(GameTestHelper helper) {
        helper.assertTrue("#ABCDEF".equals(EssentialsUIKit.formatHexColor(0x00ABCDEF)), "plain");
        helper.assertTrue("#112233".equals(EssentialsUIKit.formatHexColor(0xFF112233)), "alpha dropped");
        helper.assertTrue("#000000".equals(EssentialsUIKit.formatHexColor(0)), "zero pads to 6 digits");
        helper.assertTrue("#00000A".equals(EssentialsUIKit.formatHexColor(10)), "leading zeros kept");
        helper.succeed();
    }
}
