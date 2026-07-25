package net.phoenixvine.essentials.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.phoenixvine.essentials.config.EssentialsPermissions;

public final class GamemodeShortcutCommand {

    private GamemodeShortcutCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        register(dispatcher, "gmc", GameType.CREATIVE);
        register(dispatcher, "gms", GameType.SURVIVAL);
        register(dispatcher, "gma", GameType.ADVENTURE);
        register(dispatcher, "gmsp", GameType.SPECTATOR);
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher, String literal, GameType mode) {
        dispatcher.register(Commands.literal(literal)
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.GAMEMODE))
                .executes(ctx -> setGamemode(ctx.getSource(), null, mode))
                .then(Commands.argument("player", StringArgumentType.word())
                        .requires(source -> source.hasPermission(2))
                        .executes(ctx -> setGamemode(ctx.getSource(), StringArgumentType.getString(ctx, "player"), mode))));
    }

    private static int setGamemode(CommandSourceStack source, String targetName, GameType mode) {
        ServerPlayer target;
        if (targetName == null) {
            target = source.getPlayer();
            if (target == null) return 0;
        } else {
            target = source.getServer().getPlayerList().getPlayerByName(targetName);
            if (target == null) {
                source.sendFailure(Component.literal("§cPlayer \"" + targetName + "\" isn't online."));
                return 0;
            }
        }

        target.setGameMode(mode);
        target.sendSystemMessage(Component.literal("§7Gamemode set to §f" + mode.getName() + "§7."));
        if (targetName != null) {
            source.sendSuccess(() -> Component.literal("§7Set §f" + target.getGameProfile().getName() +
                    "§7's gamemode to §f" + mode.getName() + "§7."), true);
        }
        return 1;
    }
}
