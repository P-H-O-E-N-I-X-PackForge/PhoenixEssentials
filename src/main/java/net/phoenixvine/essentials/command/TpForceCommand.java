package net.phoenixvine.essentials.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.phoenixvine.essentials.config.EssentialsPermissions;
import net.phoenixvine.essentials.data.NamedLocation;

public final class TpForceCommand {

    private TpForceCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tpforce")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.TPFORCE))
                .then(Commands.argument("player", StringArgumentType.word())
                        .then(Commands.argument("target", StringArgumentType.word())
                                .executes(ctx -> tpforce(ctx.getSource(), StringArgumentType.getString(ctx, "player"),
                                        StringArgumentType.getString(ctx, "target"))))));

        dispatcher.register(Commands.literal("tphere")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.TPFORCE))
                .then(Commands.argument("player", StringArgumentType.word())
                        .executes(ctx -> tphere(ctx.getSource(), StringArgumentType.getString(ctx, "player")))));
    }

    private static int tpforce(CommandSourceStack source, String playerName, String targetName) {
        ServerPlayer player = source.getServer().getPlayerList().getPlayerByName(playerName);
        if (player == null) {
            source.sendFailure(Component.literal("§cPlayer \"" + playerName + "\" isn't online."));
            return 0;
        }
        ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(targetName);
        if (target == null) {
            source.sendFailure(Component.literal("§cPlayer \"" + targetName + "\" isn't online."));
            return 0;
        }

        player.sendSystemMessage(Component.literal("§7You were teleported to §f" + target.getGameProfile().getName() + "§7."));
        TeleportExecutor.forceTeleport(player, NamedLocation.of(target), "tpforce", target.getGameProfile().getName());
        source.sendSuccess(() -> Component.literal("§aTeleported §f" + player.getGameProfile().getName() +
                " §ato §f" + target.getGameProfile().getName() + "§a."), true);
        return 1;
    }

    private static int tphere(CommandSourceStack source, String playerName) {
        ServerPlayer executor = source.getPlayer();
        if (executor == null) return 0;

        ServerPlayer player = source.getServer().getPlayerList().getPlayerByName(playerName);
        if (player == null) {
            source.sendFailure(Component.literal("§cPlayer \"" + playerName + "\" isn't online."));
            return 0;
        }
        if (player == executor) {
            source.sendFailure(Component.literal("§cYou're already here."));
            return 0;
        }

        player.sendSystemMessage(Component.literal("§7You were teleported to §f" + executor.getGameProfile().getName() + "§7."));
        TeleportExecutor.forceTeleport(player, NamedLocation.of(executor), "tpforce", executor.getGameProfile().getName());
        source.sendSuccess(() -> Component.literal("§aTeleported §f" + player.getGameProfile().getName() +
                " §ato you."), false);
        return 1;
    }
}
