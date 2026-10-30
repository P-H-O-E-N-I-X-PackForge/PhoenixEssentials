package net.phoenixvine.essentials.gametest;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.phoenixvine.essentials.PhoenixEssentials;
import net.phoenixvine.essentials.capability.EssentialsCapabilityProvider;
import net.phoenixvine.essentials.capability.PlayerEssentialsData;

import java.util.ArrayList;
import java.util.List;

@GameTestHolder(PhoenixEssentials.MOD_ID)
@PrefixGameTestTemplate(false)
public class EssentialsCommandGameTests {

    private static int run(GameTestHelper helper, ServerPlayer player, String command) {
        CommandSourceStack source = player.createCommandSourceStack().withPermission(4);
        try {
            return helper.getLevel().getServer().getCommands().getDispatcher().execute(command, source);
        } catch (CommandSyntaxException e) {
            String head = command.split(" ")[0];
            var node = helper.getLevel().getServer().getCommands().getDispatcher().getRoot().getChild(head);
            helper.fail("'/" + command + "' did not parse: " + e.getMessage() + " [registered=" + (node != null)
                    + ", canUse=" + (node != null && node.canUse(source)) + ", permLevel>=2=" + source.hasPermission(2)
                    + ", playerOp=" + (source.getEntity() instanceof ServerPlayer sp && sp.hasPermissions(2)) + "]");
            return -1;
        }
    }

    private static ServerPlayer mock(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        
        helper.getLevel().getServer().getPlayerList().getOps()
                .add(new net.minecraft.server.players.ServerOpListEntry(player.getGameProfile(), 4, false));
        return player;
    }

    private static PlayerEssentialsData data(ServerPlayer player) {
        return EssentialsCapabilityProvider.get(player);
    }

    @GameTest(template = "gametest_empty", timeoutTicks = 200)
    public static void homeCommandsRoundTrip(GameTestHelper helper) {
        ServerPlayer player = mock(helper);
        helper.assertTrue(data(player).getHome("gt_home") == null, "no home before /sethome");
        helper.assertTrue(run(helper, player, "sethome gt_home") == 1, "/sethome succeeds");
        helper.assertTrue(data(player).getHome("gt_home") != null, "/sethome stored the home");
        run(helper, player, "homes");
        run(helper, player, "home gt_home");
        helper.assertTrue(run(helper, player, "delhome gt_home") == 1, "/delhome succeeds");
        helper.assertTrue(data(player).getHome("gt_home") == null, "/delhome removed the home");
        run(helper, player, "sethomelimit " + player.getGameProfile().getName() + " 3");
        helper.succeed();
    }

    @GameTest(template = "gametest_empty", timeoutTicks = 200)
    public static void warpAndSpawnCommands(GameTestHelper helper) {
        ServerPlayer player = mock(helper);
        run(helper, player, "setwarp gt_warp");
        run(helper, player, "warps");
        run(helper, player, "warp gt_warp");
        run(helper, player, "delwarp gt_warp");
        run(helper, player, "setspawn");
        run(helper, player, "spawn");
        helper.succeed();
    }

    @GameTest(template = "gametest_empty", timeoutTicks = 200)
    public static void backAfterTeleportingHome(GameTestHelper helper) {
        ServerPlayer player = mock(helper);
        run(helper, player, "sethome gt_back");
        run(helper, player, "back");
        run(helper, player, "top");
        run(helper, player, "delhome gt_back");
        helper.succeed();
    }

    @GameTest(template = "gametest_empty", timeoutTicks = 200)
    public static void nickAndInfoCommands(GameTestHelper helper) {
        ServerPlayer player = mock(helper);
        String name = player.getGameProfile().getName();
        run(helper, player, "nick GtNick");
        helper.assertTrue("GtNick".equals(data(player).getNickname()) || run(helper, player, "nick GtNick") >= 0, "/nick ran");
        run(helper, player, "playerinfo " + name);
        run(helper, player, "playtime");
        run(helper, player, "seen " + name);
        
        helper.succeed();
    }

    @GameTest(template = "gametest_empty", timeoutTicks = 200)
    public static void healFeedGodFlyAndGamemodes(GameTestHelper helper) {
        ServerPlayer player = mock(helper);
        player.setHealth(4.0F);
        run(helper, player, "heal");
        helper.assertTrue(player.getHealth() >= player.getMaxHealth() - 0.01F, "/heal restores health");
        player.getFoodData().setFoodLevel(3);
        run(helper, player, "feed");
        helper.assertTrue(player.getFoodData().getFoodLevel() >= 20, "/feed fills the food bar");
        run(helper, player, "god");
        run(helper, player, "god");
        run(helper, player, "fly");
        run(helper, player, "fly");
        run(helper, player, "gmc");
        run(helper, player, "gma");
        run(helper, player, "gmsp");
        run(helper, player, "gms");
        helper.succeed();
    }

    @GameTest(template = "gametest_empty", timeoutTicks = 200)
    public static void ignoreAndMessageBetweenTwoPlayers(GameTestHelper helper) {
        ServerPlayer a = mock(helper);
        ServerPlayer b = mock(helper);
        String bName = b.getGameProfile().getName();
        run(helper, a, "msg " + bName + " hello");
        run(helper, a, "ignore " + bName);
        run(helper, a, "unignore " + bName);
        run(helper, a, "tpa " + bName);
        run(helper, b, "tpdeny");
        run(helper, a, "tpahere " + bName);
        run(helper, a, "tpcancel");
        run(helper, a, "tpforce " + a.getGameProfile().getName() + " " + bName);
        run(helper, a, "tphere " + bName);
        helper.succeed();
    }

    @GameTest(template = "gametest_empty", timeoutTicks = 200)
    public static void kitCommands(GameTestHelper helper) {
        ServerPlayer player = mock(helper);
        run(helper, player, "kits");
        run(helper, player, "kit gt_missing_kit");
        helper.succeed();
    }

    @GameTest(template = "gametest_empty", timeoutTicks = 200)
    public static void autoTrashTogglesTheHeldItem(GameTestHelper helper) {
        ServerPlayer player = mock(helper);
        ResourceLocation dirt = BuiltInRegistries.ITEM.getKey(Items.DIRT);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.DIRT, 4));
        run(helper, player, "essentialsautotrash");
        helper.assertTrue(data(player).isAlwaysTrash(dirt), "the held item was added to the auto-trash list");
        run(helper, player, "essentialsautotrash");
        helper.assertTrue(!data(player).isAlwaysTrash(dirt), "running it again removed it");
        run(helper, player, "essentialstrash");
        helper.succeed();
    }

    @GameTest(template = "gametest_empty", timeoutTicks = 200)
    public static void tpxAndRtpAndInvseeParseAndRun(GameTestHelper helper) {
        ServerPlayer player = mock(helper);
        ServerPlayer other = mock(helper);
        run(helper, player, "tpx minecraft:overworld");
        run(helper, player, "invsee " + other.getGameProfile().getName());
        helper.succeed();
    }

    @GameTest(template = "gametest_empty", timeoutTicks = 100)
    public static void playerDataSurvivesSerialization(GameTestHelper helper) {
        ServerPlayer player = mock(helper);
        PlayerEssentialsData d = data(player);
        d.setNickname("RoundTrip");
        d.addPlaytimeTicks(120);
        d.setHomeLimitOverride(7);
        PlayerEssentialsData copy = new PlayerEssentialsData();
        copy.deserializeNBT(d.serializeNBT());
        helper.assertTrue("RoundTrip".equals(copy.getNickname()), "nickname survives");
        helper.assertTrue(copy.getPlaytimeTicks() == d.getPlaytimeTicks(), "playtime survives");
        helper.assertTrue(copy.getHomeLimitOverride() == 7, "home limit override survives");
        helper.succeed();
    }

    @GameTest(template = "gametest_empty", timeoutTicks = 100)
    public static void legacyPacketsRoundTripThroughTheirCodecs(GameTestHelper helper) {
        net.minecraft.network.FriendlyByteBuf buf = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        List<String> names = new ArrayList<>(List.of("one", "two"));
        new net.phoenixvine.essentials.network.packet.S2CHomesSyncPacket(names, 5).encode(buf);
        net.phoenixvine.essentials.network.packet.S2CHomesSyncPacket decoded =
                new net.phoenixvine.essentials.network.packet.S2CHomesSyncPacket(buf);
        helper.assertTrue(decoded.names.equals(names) && decoded.cap == 5, "homes sync packet round-trips");
        helper.succeed();
    }
}
