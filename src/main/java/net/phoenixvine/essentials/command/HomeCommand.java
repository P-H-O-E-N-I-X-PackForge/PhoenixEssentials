package net.phoenixvine.essentials.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.phoenixvine.essentials.capability.EssentialsCapabilityProvider;
import net.phoenixvine.essentials.capability.PlayerEssentialsData;
import net.phoenixvine.essentials.config.EssentialsPermissions;
import net.phoenixvine.essentials.config.EssentialsServerConfig;
import net.phoenixvine.essentials.data.NamedLocation;
import net.phoenixvine.essentials.team.TeamCompat;

import java.util.List;
import java.util.UUID;

public final class HomeCommand {

    private static final String DEFAULT_NAME = "home";

    private static final SuggestionProvider<CommandSourceStack> HOME_NAME_SUGGESTIONS = (ctx, builder) -> {
        if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
            PlayerEssentialsData data = data(player);
            if (data != null) {
                return SharedSuggestionProvider.suggest(data.getHomes().keySet(), builder);
            }
        }
        return builder.buildFuture();
    };

    private HomeCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("home")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.HOME))
                .executes(ctx -> teleportHome(ctx.getSource(), DEFAULT_NAME))
                .then(Commands.argument("name", StringArgumentType.word())
                        .suggests(HOME_NAME_SUGGESTIONS)
                        .executes(ctx -> teleportHome(ctx.getSource(), StringArgumentType.getString(ctx, "name")))));

        dispatcher.register(Commands.literal("sethome")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.SETHOME))
                .executes(ctx -> setHome(ctx.getSource(), DEFAULT_NAME))
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(ctx -> setHome(ctx.getSource(), StringArgumentType.getString(ctx, "name")))));

        dispatcher.register(Commands.literal("delhome")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.DELHOME))
                .then(Commands.argument("name", StringArgumentType.word())
                        .suggests(HOME_NAME_SUGGESTIONS)
                        .executes(ctx -> deleteHome(ctx.getSource(), StringArgumentType.getString(ctx, "name")))));

        dispatcher.register(Commands.literal("homes")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.HOMES))
                .executes(ctx -> listHomes(ctx.getSource())));
        registerLimit(dispatcher);
    }

    // Split out so the Hot Chocolate takeover (EssentialsHcMode) can register the HC /home family
    // while still keeping this Java-only /sethomelimit.
    public static void registerLimit(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("sethomelimit")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.SETHOMELIMIT))
                .then(Commands.argument("player", StringArgumentType.word())
                        .then(Commands.argument("count", IntegerArgumentType.integer(-1, 256))
                                .executes(ctx -> setHomeLimit(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "player"),
                                        IntegerArgumentType.getInteger(ctx, "count")))))
                .then(Commands.literal("team")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .then(Commands.argument("count", IntegerArgumentType.integer(-1, 256))
                                        .executes(ctx -> setTeamHomeLimit(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "player"),
                                                IntegerArgumentType.getInteger(ctx, "count")))))));
    }

    private static int setHomeLimit(CommandSourceStack source, String targetName, int count) {
        ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(targetName);
        if (target == null) {
            source.sendFailure(Component.literal("§cPlayer \"" + targetName + "\" isn't online."));
            return 0;
        }

        PlayerEssentialsData data = data(target);
        if (data == null) {
            source.sendFailure(Component.literal("§cCouldn't access essentials data for \"" + targetName + "\"."));
            return 0;
        }

        data.setHomeLimitOverride(count);

        if (count < 0) {
            source.sendSuccess(() -> Component.literal("§aCleared §f" + target.getGameProfile().getName() +
                    "§a's home-limit override - they now use the server default of §f" +
                    EssentialsServerConfig.HOMES_PER_PLAYER.get() + "§a."), true);
        } else {
            source.sendSuccess(() -> Component.literal("§a" + target.getGameProfile().getName() +
                    "§a's home limit is now §f" + count + "§a."), true);
        }
        return 1;
    }

    private static int setTeamHomeLimit(CommandSourceStack source, String anchorName, int count) {
        ServerPlayer anchor = source.getServer().getPlayerList().getPlayerByName(anchorName);
        if (anchor == null) {
            source.sendFailure(Component.literal("§cPlayer \"" + anchorName + "\" isn't online."));
            return 0;
        }

        if (!TeamCompat.isAnyBackendLoaded()) {
            source.sendFailure(Component.literal("§cNo team/guild backend is loaded - use /sethomelimit <player> <count> instead."));
            return 0;
        }

        if (TeamCompat.getTeamId(anchor).isEmpty()) {
            source.sendFailure(Component.literal("§c" + anchor.getGameProfile().getName() + " isn't on a team."));
            return 0;
        }

        List<ServerPlayer> targets = new java.util.ArrayList<>();
        targets.add(anchor);
        for (UUID mateId : TeamCompat.getTeammates(anchor)) {
            ServerPlayer mate = source.getServer().getPlayerList().getPlayer(mateId);
            if (mate != null) targets.add(mate);
        }

        int updated = 0;
        for (ServerPlayer target : targets) {
            PlayerEssentialsData data = data(target);
            if (data == null) continue;
            data.setHomeLimitOverride(count);
            updated++;
        }

        String teamName = TeamCompat.getTeamName(anchor).orElse("their team");
        int updatedCount = updated;
        if (count < 0) {
            source.sendSuccess(() -> Component.literal("§aCleared the home-limit override for §f" + updatedCount +
                    "§a online member(s) of §f" + teamName + "§a - they now use the server default of §f" +
                    EssentialsServerConfig.HOMES_PER_PLAYER.get() + "§a."), true);
        } else {
            source.sendSuccess(() -> Component.literal("§aSet the home limit to §f" + count +
                    "§a for §f" + updatedCount + "§a online member(s) of §f" + teamName + "§a."), true);
        }
        return updated;
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

        int cooldown = EssentialsServerConfig.TELEPORT_COOLDOWN_SECONDS.get();
        TeleportExecutor.request(player, home, "home", "home \"" + name + "\"", null, false, cooldown);
        return 1;
    }

    private static int setHome(CommandSourceStack source, String name) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        PlayerEssentialsData data = data(player);
        if (data == null) return 0;

        int limit = effectiveHomeLimit(data);
        if (data.getHome(name) == null && data.getHomes().size() >= limit) {
            source.sendFailure(Component.literal("§cYou've reached your limit of " +
                    limit + " homes - delete one with /delhome first."));
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
                    effectiveHomeLimit(data) + "): §f" + String.join("§7, §f", names)), false);
        }
        return 1;
    }

    private static PlayerEssentialsData data(ServerPlayer player) {
        return player.getCapability(EssentialsCapabilityProvider.PLAYER_ESSENTIALS).orElse(null);
    }

    private static int effectiveHomeLimit(PlayerEssentialsData data) {
        int override = data.getHomeLimitOverride();
        return override >= 0 ? override : EssentialsServerConfig.HOMES_PER_PLAYER.get();
    }
}