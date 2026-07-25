package net.phoenixvine.essentials.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.phoenixvine.essentials.config.EssentialsPermissions;
import net.phoenixvine.essentials.data.NamedLocation;
import net.phoenixvine.essentials.data.WarpRegistry;

import java.util.List;

public final class WarpCommand {

    private WarpCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("warp")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.WARP))
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(ctx -> teleportWarp(ctx.getSource(), StringArgumentType.getString(ctx, "name")))));

        dispatcher.register(Commands.literal("setwarp")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.SETWARP))
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(ctx -> setWarp(ctx.getSource(), StringArgumentType.getString(ctx, "name")))));

        dispatcher.register(Commands.literal("delwarp")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.DELWARP))
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(ctx -> deleteWarp(ctx.getSource(), StringArgumentType.getString(ctx, "name")))));

        dispatcher.register(Commands.literal("warps")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.WARPS))
                .executes(ctx -> listWarps(ctx.getSource())));
    }

    private static int teleportWarp(CommandSourceStack source, String name) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        NamedLocation warp = WarpRegistry.get(name);
        if (warp == null) {
            source.sendFailure(Component.literal("§cNo warp named \"" + name + "\"."));
            return 0;
        }

        TeleportExecutor.request(player, warp, "warp", "warp \"" + name + "\"");
        return 1;
    }

    private static int setWarp(CommandSourceStack source, String name) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        WarpRegistry.set(name, NamedLocation.of(player));
        source.sendSuccess(() -> Component.literal("§aWarp \"" + name + "\" set."), true);
        return 1;
    }

    private static int deleteWarp(CommandSourceStack source, String name) {
        if (!WarpRegistry.remove(name)) {
            source.sendFailure(Component.literal("§cNo warp named \"" + name + "\"."));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("§aWarp \"" + name + "\" deleted."), true);
        return 1;
    }

    private static int listWarps(CommandSourceStack source) {
        List<String> names = WarpRegistry.getAll().keySet().stream().sorted().toList();
        if (names.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§7No warps have been set."), false);
        } else {
            source.sendSuccess(() -> Component.literal("§7Warps: §f" + String.join("§7, §f", names)), false);
        }
        return 1;
    }
}
