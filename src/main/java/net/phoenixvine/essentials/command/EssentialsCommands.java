package net.phoenixvine.essentials.command;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.phoenixvine.essentials.EssentialsHcMode;
import net.phoenixvine.essentials.config.EssentialsServerConfig;
import net.phoenixvine.essentials.hc.hcCompileMerged1;

public final class EssentialsCommands {

    private EssentialsCommands() {}

    // Each group is gated by the same `ENABLE_*` config switch as before. By default the Hot Chocolate
    // port (src/main/hotc) owns the real command names; launch with
    // `-Dphoenix_essentials.use_java=true` to register the original Java classes instead (the HC
    // versions then come up as `hc`-prefixed twins -- see PhoenixEssentials#onRegisterCommands).
    // `/sethomelimit` has no HC port yet and always stays Java.
    public static void registerAll(CommandDispatcher<CommandSourceStack> dispatcher) {

        boolean configLoaded = EssentialsServerConfig.SPEC.isLoaded();
        boolean hc = EssentialsHcMode.takeover();

        if (!configLoaded || EssentialsServerConfig.ENABLE_HOME.get()) {
            if (hc) {
                hcCompileMerged1.register_real_home_commands(dispatcher);
                HomeCommand.registerLimit(dispatcher);
            } else {
                HomeCommand.register(dispatcher);
            }
        }
        if (!configLoaded || EssentialsServerConfig.ENABLE_SPAWN.get()) {
            if (hc) hcCompileMerged1.register_real_spawn_commands(dispatcher); else SpawnCommand.register(dispatcher);
        }
        if (!configLoaded || EssentialsServerConfig.ENABLE_WARP.get()) {
            if (hc) hcCompileMerged1.register_real_warp_commands(dispatcher); else WarpCommand.register(dispatcher);
        }
        if (!configLoaded || EssentialsServerConfig.ENABLE_BACK.get()) {
            if (hc) hcCompileMerged1.register_real_back_command(dispatcher); else BackCommand.register(dispatcher);
        }
        if (!configLoaded || EssentialsServerConfig.ENABLE_RTP.get()) {
            if (hc) hcCompileMerged1.register_real_rtp_command(dispatcher); else RtpCommand.register(dispatcher);
        }
        if (!configLoaded || EssentialsServerConfig.ENABLE_TPA.get()) {
            if (hc) hcCompileMerged1.register_real_tpa_commands(dispatcher); else TpaCommand.register(dispatcher);
        }
        if (!configLoaded || EssentialsServerConfig.ENABLE_MSG.get()) {
            if (hc) hcCompileMerged1.register_real_message_commands(dispatcher); else MessageCommand.register(dispatcher);
        }
        if (!configLoaded || EssentialsServerConfig.ENABLE_NICK.get()) {
            if (hc) hcCompileMerged1.register_real_nick_command(dispatcher); else NickCommand.register(dispatcher);
        }
        if (!configLoaded || EssentialsServerConfig.ENABLE_IGNORE.get()) {
            if (hc) hcCompileMerged1.register_real_ignore_commands(dispatcher); else IgnoreCommand.register(dispatcher);
        }
        if (!configLoaded || EssentialsServerConfig.ENABLE_AFK.get()) {
            if (hc) hcCompileMerged1.register_real_afk_command(dispatcher); else AfkCommand.register(dispatcher);
        }
        if (!configLoaded || EssentialsServerConfig.ENABLE_PLAYERINFO.get()) {
            if (hc) hcCompileMerged1.register_real_playerinfo_command(dispatcher); else PlayerInfoCommand.register(dispatcher);
        }
        if (!configLoaded || EssentialsServerConfig.ENABLE_SEEN.get()) {
            if (hc) hcCompileMerged1.register_real_seen_command(dispatcher); else SeenCommand.register(dispatcher);
        }
        if (!configLoaded || EssentialsServerConfig.ENABLE_PLAYTIME.get()) {
            if (hc) hcCompileMerged1.register_real_playtime_command(dispatcher); else PlaytimeCommand.register(dispatcher);
        }
        if (!configLoaded || EssentialsServerConfig.ENABLE_KIT.get()) {
            if (hc) hcCompileMerged1.register_real_kit_commands(dispatcher); else KitCommand.register(dispatcher);
        }
        if (!configLoaded || EssentialsServerConfig.ENABLE_HEAL_FEED.get()) {
            if (hc) hcCompileMerged1.register_real_heal_feed_commands(dispatcher); else HealFeedCommand.register(dispatcher);
        }
        if (!configLoaded || EssentialsServerConfig.ENABLE_INVSEE.get()) {
            if (hc) hcCompileMerged1.register_real_invsee_command(dispatcher); else InvseeCommand.register(dispatcher);
        }
        if (!configLoaded || EssentialsServerConfig.ENABLE_GODFLY.get()) {
            if (hc) hcCompileMerged1.register_real_god_fly_commands(dispatcher); else GodFlyCommand.register(dispatcher);
        }
        if (!configLoaded || EssentialsServerConfig.ENABLE_GAMEMODE_SHORTCUTS.get()) {
            if (hc) hcCompileMerged1.register_real_gamemode_commands(dispatcher); else GamemodeShortcutCommand.register(dispatcher);
        }
        if (!configLoaded || EssentialsServerConfig.ENABLE_TOP.get()) {
            if (hc) hcCompileMerged1.register_real_top_command(dispatcher); else TopCommand.register(dispatcher);
        }
        if (!configLoaded || EssentialsServerConfig.ENABLE_TPX.get()) {
            if (hc) hcCompileMerged1.register_real_tpx_command(dispatcher); else TpxCommand.register(dispatcher);
        }
        if (!configLoaded || EssentialsServerConfig.ENABLE_TPFORCE.get()) {
            if (hc) hcCompileMerged1.register_real_tpforce_commands(dispatcher); else TpForceCommand.register(dispatcher);
        }

        if (hc) {
            hcCompileMerged1.register_real_trash_commands(dispatcher);
        } else {
            EssentialsTrashCommand.register(dispatcher);
            EssentialsAutoTrashCommand.register(dispatcher);
        }
    }
}
