package net.phoenixvine.essentials.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.phoenixvine.essentials.api.EssentialsAPI;
import net.phoenixvine.essentials.capability.EssentialsCapabilityProvider;
import net.phoenixvine.essentials.capability.PlayerEssentialsData;
import net.phoenixvine.essentials.config.EssentialsPermissions;

public final class IgnoreCommand {

    private IgnoreCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ignore")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.IGNORE))
                .then(Commands.argument("player", StringArgumentType.word())
                        .executes(ctx -> setIgnored(ctx.getSource(), StringArgumentType.getString(ctx, "player"), true))));

        dispatcher.register(Commands.literal("unignore")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.IGNORE))
                .then(Commands.argument("player", StringArgumentType.word())
                        .executes(ctx -> setIgnored(ctx.getSource(), StringArgumentType.getString(ctx, "player"), false))));
    }

    private static int setIgnored(CommandSourceStack source, String targetName, boolean ignore) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        if (!EssentialsAPI.isFeatureEnabled(EssentialsAPI.FEATURE_SOCIAL, player.level().dimension().location())) {
            source.sendFailure(Component.literal("§cSocial commands aren't available here right now."));
            return 0;
        }

        ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(targetName);
        if (target == null) {
            source.sendFailure(Component.literal("§cPlayer \"" + targetName + "\" isn't online."));
            return 0;
        }
        if (target == player) {
            source.sendFailure(Component.literal("§cYou can't ignore yourself."));
            return 0;
        }

        PlayerEssentialsData data = player.getCapability(EssentialsCapabilityProvider.PLAYER_ESSENTIALS).orElse(null);
        if (data == null) return 0;

        if (ignore) {
            data.getIgnored().add(target.getUUID());
            source.sendSuccess(() -> Component.literal("§7Now ignoring §f" + target.getName().getString() + "§7."), false);
        } else {
            data.getIgnored().remove(target.getUUID());
            source.sendSuccess(() -> Component.literal("§7No longer ignoring §f" + target.getName().getString() + "§7."), false);
        }
        return 1;
    }
}
