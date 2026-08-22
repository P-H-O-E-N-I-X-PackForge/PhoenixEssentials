package net.phoenixvine.essentials.command;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.phoenixvine.essentials.config.EssentialsServerConfig;

public final class EssentialsCommands {

    private EssentialsCommands() {}

    public static void registerAll(CommandDispatcher<CommandSourceStack> dispatcher) {

        boolean configLoaded = EssentialsServerConfig.SPEC.isLoaded();

        if (!configLoaded || EssentialsServerConfig.ENABLE_HOME.get()) HomeCommand.register(dispatcher);
        if (!configLoaded || EssentialsServerConfig.ENABLE_SPAWN.get()) SpawnCommand.register(dispatcher);
        if (!configLoaded || EssentialsServerConfig.ENABLE_WARP.get()) WarpCommand.register(dispatcher);
        if (!configLoaded || EssentialsServerConfig.ENABLE_BACK.get()) BackCommand.register(dispatcher);
        if (!configLoaded || EssentialsServerConfig.ENABLE_RTP.get()) RtpCommand.register(dispatcher);
        if (!configLoaded || EssentialsServerConfig.ENABLE_TPA.get()) TpaCommand.register(dispatcher);
        if (!configLoaded || EssentialsServerConfig.ENABLE_MSG.get()) MessageCommand.register(dispatcher);
        if (!configLoaded || EssentialsServerConfig.ENABLE_NICK.get()) NickCommand.register(dispatcher);
        if (!configLoaded || EssentialsServerConfig.ENABLE_IGNORE.get()) IgnoreCommand.register(dispatcher);
        if (!configLoaded || EssentialsServerConfig.ENABLE_AFK.get()) AfkCommand.register(dispatcher);
        if (!configLoaded || EssentialsServerConfig.ENABLE_PLAYERINFO.get()) PlayerInfoCommand.register(dispatcher);
        if (!configLoaded || EssentialsServerConfig.ENABLE_SEEN.get()) SeenCommand.register(dispatcher);
        if (!configLoaded || EssentialsServerConfig.ENABLE_PLAYTIME.get()) PlaytimeCommand.register(dispatcher);
        if (!configLoaded || EssentialsServerConfig.ENABLE_KIT.get()) KitCommand.register(dispatcher);
        if (!configLoaded || EssentialsServerConfig.ENABLE_HEAL_FEED.get()) HealFeedCommand.register(dispatcher);
        if (!configLoaded || EssentialsServerConfig.ENABLE_INVSEE.get()) InvseeCommand.register(dispatcher);
        if (!configLoaded || EssentialsServerConfig.ENABLE_GODFLY.get()) GodFlyCommand.register(dispatcher);
        if (!configLoaded || EssentialsServerConfig.ENABLE_GAMEMODE_SHORTCUTS.get()) {
            GamemodeShortcutCommand.register(dispatcher);
        }
        if (!configLoaded || EssentialsServerConfig.ENABLE_TOP.get()) TopCommand.register(dispatcher);
        if (!configLoaded || EssentialsServerConfig.ENABLE_TPX.get()) TpxCommand.register(dispatcher);
        if (!configLoaded || EssentialsServerConfig.ENABLE_TPFORCE.get()) TpForceCommand.register(dispatcher);

        EssentialsTrashCommand.register(dispatcher);
        EssentialsAutoTrashCommand.register(dispatcher);
    }
}
