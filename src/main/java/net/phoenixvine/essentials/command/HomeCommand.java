package net.phoenixvine.essentials.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.phoenixvine.essentials.capability.EssentialsCapabilityProvider;
import net.phoenixvine.essentials.capability.PlayerEssentialsData;
import net.phoenixvine.essentials.config.EssentialsPermissions;
import net.phoenixvine.essentials.config.EssentialsServerConfig;
import net.phoenixvine.essentials.data.NamedLocation;

import java.util.List;

public final class HomeCommand {

    private static final String DEFAULT_NAME = "home";

    private HomeCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("home")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.HOME))
                .executes(ctx -> teleportHome(ctx.getSource(), DEFAULT_NAME))
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(ctx -> teleportHome(ctx.getSource(), StringArgumentType.getString(ctx, "name")))));

        dispatcher.register(Commands.literal("sethome")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.SETHOME))
                .executes(ctx -> setHome(ctx.getSource(), DEFAULT_NAME))
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(ctx -> setHome(ctx.getSource(), StringArgumentType.getString(ctx, "name")))));

        dispatcher.register(Commands.literal("delhome")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.DELHOME))
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(ctx -> deleteHome(ctx.getSource(), StringArgumentType.getString(ctx, "name")))));

        dispatcher.register(Commands.literal("homes")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.HOMES))
                .executes(ctx -> listHomes(ctx.getSource())));
    }

    private static int teleportHome(CommandSourceStack source, String name) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        PlayerEssentialsData data = data(player);
        if (data == null) return 0;

        NamedLocation home = data.getHome(name);
        if (home == null) {
            source.sendFailure(Component.literal("§cNo home named \"" + name + "\" - set one with /sethome " + name));
            return 0;
        }

        TeleportExecutor.request(player, home, "home", "home \"" + name + "\"");
        return 1;
    }

    private static int setHome(CommandSourceStack source, String name) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        PlayerEssentialsData data = data(player);
        if (data == null) return 0;

        if (data.getHome(name) == null && data.getHomes().size() >= EssentialsServerConfig.HOMES_PER_PLAYER.get()) {
            source.sendFailure(Component.literal("§cYou've reached your limit of " +
                    EssentialsServerConfig.HOMES_PER_PLAYER.get() + " homes - delete one with /delhome first."));
            return 0;
        }

        data.setHome(name, NamedLocation.of(player));
        source.sendSuccess(() -> Component.literal("§aHome \"" + name + "\" set."), false);
        return 1;
    }

    private static int deleteHome(CommandSourceStack source, String name) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        PlayerEssentialsData data = data(player);
        if (data == null) return 0;

        if (!data.deleteHome(name)) {
            source.sendFailure(Component.literal("§cNo home named \"" + name + "\"."));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("§aHome \"" + name + "\" deleted."), false);
        return 1;
    }

    private static int listHomes(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        PlayerEssentialsData data = data(player);
        if (data == null) return 0;

        List<String> names = data.getHomes().keySet().stream().sorted().toList();
        if (names.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§7You have no homes set. Use /sethome <name>."), false);
        } else {
            source.sendSuccess(() -> Component.literal("§7Homes (" + names.size() + "/" +
                    EssentialsServerConfig.HOMES_PER_PLAYER.get() + "): §f" + String.join("§7, §f", names)), false);
        }
        return 1;
    }

    private static PlayerEssentialsData data(ServerPlayer player) {
        return player.getCapability(EssentialsCapabilityProvider.PLAYER_ESSENTIALS).orElse(null);
    }
}
