package net.phoenixvine.essentials.command;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.phoenixvine.essentials.EssentialsHcMode;
import net.phoenixvine.essentials.config.EssentialsServerConfig;
import net.phoenixvine.essentials.hc.hcCompileMerged1;

public final class EssentialsCommands {

    private EssentialsCommands() {}

    public static void registerAll(CommandDispatcher<CommandSourceStack> dispatcher) {

        boolean configLoaded = EssentialsServerConfig.SPEC.isLoaded();
        boolean hc = EssentialsHcMode.takeover();

        if (!configLoaded || (EssentialsServerConfig.ENABLE_HOME.get() || net.phoenixvine.essentials.config.EssentialsPolicy.anyTierSetsTrue("enableHome"))) {
            if (hc) {
                hcCompileMerged1.register_real_home_commands(dispatcher);
                hcCompileMerged1.register_real_sethomelimit_command(dispatcher);
            } else {
                HomeCommand.register(dispatcher);
            }
        }
        if (!configLoaded || (EssentialsServerConfig.ENABLE_SPAWN.get() || net.phoenixvine.essentials.config.EssentialsPolicy.anyTierSetsTrue("enableSpawn"))) {
            if (hc) hcCompileMerged1.register_real_spawn_commands(dispatcher); else SpawnCommand.register(dispatcher);
        }
        if (!configLoaded || (EssentialsServerConfig.ENABLE_WARP.get() || net.phoenixvine.essentials.config.EssentialsPolicy.anyTierSetsTrue("enableWarp"))) {
            if (hc) hcCompileMerged1.register_real_warp_commands(dispatcher); else WarpCommand.register(dispatcher);
        }
        if (!configLoaded || (EssentialsServerConfig.ENABLE_BACK.get() || net.phoenixvine.essentials.config.EssentialsPolicy.anyTierSetsTrue("enableBack"))) {
            if (hc) hcCompileMerged1.register_real_back_command(dispatcher); else BackCommand.register(dispatcher);
        }
        if (!configLoaded || (EssentialsServerConfig.ENABLE_RTP.get() || net.phoenixvine.essentials.config.EssentialsPolicy.anyTierSetsTrue("enableRtp"))) {
            if (hc) hcCompileMerged1.register_real_rtp_command(dispatcher); else RtpCommand.register(dispatcher);
        }
        if (!configLoaded || (EssentialsServerConfig.ENABLE_TPA.get() || net.phoenixvine.essentials.config.EssentialsPolicy.anyTierSetsTrue("enableTpa"))) {
            if (hc) hcCompileMerged1.register_real_tpa_commands(dispatcher); else TpaCommand.register(dispatcher);
        }
        if (!configLoaded || (EssentialsServerConfig.ENABLE_MSG.get() || net.phoenixvine.essentials.config.EssentialsPolicy.anyTierSetsTrue("enableMsg"))) {
            if (hc) hcCompileMerged1.register_real_message_commands(dispatcher); else MessageCommand.register(dispatcher);
        }
        if (!configLoaded || (EssentialsServerConfig.ENABLE_NICK.get() || net.phoenixvine.essentials.config.EssentialsPolicy.anyTierSetsTrue("enableNick"))) {
            if (hc) hcCompileMerged1.register_real_nick_command(dispatcher); else NickCommand.register(dispatcher);
        }
        if (!configLoaded || (EssentialsServerConfig.ENABLE_IGNORE.get() || net.phoenixvine.essentials.config.EssentialsPolicy.anyTierSetsTrue("enableIgnore"))) {
            if (hc) hcCompileMerged1.register_real_ignore_commands(dispatcher); else IgnoreCommand.register(dispatcher);
        }
        if (!configLoaded || (EssentialsServerConfig.ENABLE_AFK.get() || net.phoenixvine.essentials.config.EssentialsPolicy.anyTierSetsTrue("enableAfk"))) {
            if (hc) hcCompileMerged1.register_real_afk_command(dispatcher); else AfkCommand.register(dispatcher);
        }
        if (!configLoaded || (EssentialsServerConfig.ENABLE_PLAYERINFO.get() || net.phoenixvine.essentials.config.EssentialsPolicy.anyTierSetsTrue("enablePlayerInfo"))) {
            if (hc) hcCompileMerged1.register_real_playerinfo_command(dispatcher); else PlayerInfoCommand.register(dispatcher);
        }
        if (!configLoaded || (EssentialsServerConfig.ENABLE_SEEN.get() || net.phoenixvine.essentials.config.EssentialsPolicy.anyTierSetsTrue("enableSeen"))) {
            if (hc) hcCompileMerged1.register_real_seen_command(dispatcher); else SeenCommand.register(dispatcher);
        }
        if (!configLoaded || (EssentialsServerConfig.ENABLE_PLAYTIME.get() || net.phoenixvine.essentials.config.EssentialsPolicy.anyTierSetsTrue("enablePlaytime"))) {
            if (hc) hcCompileMerged1.register_real_playtime_command(dispatcher); else PlaytimeCommand.register(dispatcher);
        }
        if (!configLoaded || (EssentialsServerConfig.ENABLE_KIT.get() || net.phoenixvine.essentials.config.EssentialsPolicy.anyTierSetsTrue("enableKit"))) {
            if (hc) hcCompileMerged1.register_real_kit_commands(dispatcher); else KitCommand.register(dispatcher);
        }
        if (!configLoaded || (EssentialsServerConfig.ENABLE_HEAL_FEED.get() || net.phoenixvine.essentials.config.EssentialsPolicy.anyTierSetsTrue("enableHealFeed"))) {
            if (hc) hcCompileMerged1.register_real_heal_feed_commands(dispatcher); else HealFeedCommand.register(dispatcher);
        }
        if (!configLoaded || (EssentialsServerConfig.ENABLE_INVSEE.get() || net.phoenixvine.essentials.config.EssentialsPolicy.anyTierSetsTrue("enableInvsee"))) {
            if (hc) hcCompileMerged1.register_real_invsee_command(dispatcher); else InvseeCommand.register(dispatcher);
        }
        if (!configLoaded || (EssentialsServerConfig.ENABLE_GODFLY.get() || net.phoenixvine.essentials.config.EssentialsPolicy.anyTierSetsTrue("enableGodFly"))) {
            if (hc) hcCompileMerged1.register_real_god_fly_commands(dispatcher); else GodFlyCommand.register(dispatcher);
        }
        if (!configLoaded || (EssentialsServerConfig.ENABLE_GAMEMODE_SHORTCUTS.get() || net.phoenixvine.essentials.config.EssentialsPolicy.anyTierSetsTrue("enableGamemodeShortcuts"))) {
            if (hc) hcCompileMerged1.register_real_gamemode_commands(dispatcher); else GamemodeShortcutCommand.register(dispatcher);
        }
        if (!configLoaded || (EssentialsServerConfig.ENABLE_TOP.get() || net.phoenixvine.essentials.config.EssentialsPolicy.anyTierSetsTrue("enableTop"))) {
            if (hc) hcCompileMerged1.register_real_top_command(dispatcher); else TopCommand.register(dispatcher);
        }
        if (!configLoaded || (EssentialsServerConfig.ENABLE_TPX.get() || net.phoenixvine.essentials.config.EssentialsPolicy.anyTierSetsTrue("enableTpx"))) {
            if (hc) hcCompileMerged1.register_real_tpx_command(dispatcher); else TpxCommand.register(dispatcher);
        }
        if (!configLoaded || (EssentialsServerConfig.ENABLE_TPFORCE.get() || net.phoenixvine.essentials.config.EssentialsPolicy.anyTierSetsTrue("enableTpforce"))) {
            if (hc) hcCompileMerged1.register_real_tpforce_commands(dispatcher); else TpForceCommand.register(dispatcher);
        }
        if (hc && (!configLoaded || (EssentialsServerConfig.ENABLE_LEADERBOARDS.get() || net.phoenixvine.essentials.config.EssentialsPolicy.anyTierSetsTrue("enableLeaderboards")))) {
            hcCompileMerged1.register_real_leaderboards_command(dispatcher);
        }

        if (hc) {
            hcCompileMerged1.register_real_trash_commands(dispatcher);
        } else {
            EssentialsTrashCommand.register(dispatcher);
            EssentialsAutoTrashCommand.register(dispatcher);
        }
    }
}
