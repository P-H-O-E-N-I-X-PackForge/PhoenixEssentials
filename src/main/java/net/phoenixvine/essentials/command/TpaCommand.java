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
import net.phoenixvine.essentials.team.TeamCompat;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TpaCommand {

    private record Request(UUID fromUuid, String fromName, boolean here, long expiresAtMs) {}

    private static final Map<UUID, Request> PENDING = new ConcurrentHashMap<>();

    private TpaCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tpa")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.TPA))
                .then(Commands.argument("player", StringArgumentType.word())
                        .executes(ctx -> request(ctx.getSource(), StringArgumentType.getString(ctx, "player"), false))));

        dispatcher.register(Commands.literal("tpahere")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.TPA))
                .then(Commands.argument("player", StringArgumentType.word())
                        .executes(ctx -> request(ctx.getSource(), StringArgumentType.getString(ctx, "player"), true))));

        dispatcher.register(Commands.literal("tpaccept")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.TPA))
                .executes(ctx -> respond(ctx.getSource(), true)));

        dispatcher.register(Commands.literal("tpdeny")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.TPA))
                .executes(ctx -> respond(ctx.getSource(), false)));

        dispatcher.register(Commands.literal("tpcancel")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.TPA))
                .executes(ctx -> cancel(ctx.getSource())));
    }

    private static int request(CommandSourceStack source, String targetName, boolean here) {
        ServerPlayer requester = source.getPlayer();
        if (requester == null) return 0;

        ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(targetName);
        if (target == null) {
            source.sendFailure(Component.literal("§cPlayer \"" + targetName + "\" isn't online."));
            return 0;
        }
        if (target == requester) {
            source.sendFailure(Component.literal("§cYou can't send a teleport request to yourself."));
            return 0;
        }

        PlayerEssentialsData targetData = target.getCapability(EssentialsCapabilityProvider.PLAYER_ESSENTIALS)
                .orElse(null);
        if (targetData != null && targetData.isIgnoring(requester.getUUID())) {
            source.sendFailure(Component.literal("§cThat player isn't accepting teleport requests right now."));
            return 0;
        }

        int timeoutSeconds = EssentialsServerConfig.TPA_TIMEOUT_SECONDS.get();
        PENDING.put(target.getUUID(), new Request(requester.getUUID(), requester.getName().getString(), here,
                System.currentTimeMillis() + timeoutSeconds * 1000L));

        requester.sendSystemMessage(Component.literal("§7Teleport request sent to §f" + target.getName().getString() +
                "§7. Expires in " + timeoutSeconds + "s."));
        String action = here ? "wants you to teleport to them" : "wants to teleport to you";
        target.sendSystemMessage(Component.literal("§f" + requester.getName().getString() + " §7" + action +
                ". §a/tpaccept §7or §c/tpdeny §7(expires in " + timeoutSeconds + "s)"));
        return 1;
    }

    private static int respond(CommandSourceStack source, boolean accept) {
        ServerPlayer target = source.getPlayer();
        if (target == null) return 0;

        Request req = PENDING.remove(target.getUUID());
        if (req == null || System.currentTimeMillis() > req.expiresAtMs()) {
            source.sendFailure(Component.literal("§cYou have no pending teleport request."));
            return 0;
        }

        ServerPlayer requester = source.getServer().getPlayerList().getPlayer(req.fromUuid());
        if (requester == null) {
            source.sendFailure(Component.literal("§c" + req.fromName() + " is no longer online."));
            return 0;
        }

        if (!accept) {
            target.sendSystemMessage(Component.literal("§7Teleport request denied."));
            requester.sendSystemMessage(Component.literal("§f" + target.getName().getString() +
                    " §7denied your teleport request."));
            return 1;
        }

        boolean bypassDelay = EssentialsServerConfig.TPA_TEAMMATE_BYPASS_DELAY.get() &&
                TeamCompat.areTeammates(requester, target);

        int cooldown = EssentialsServerConfig.TPA_COOLDOWN_SECONDS.get();

        if (req.here()) {
            TeleportExecutor.request(target, NamedLocation.of(requester), "tpa", requester.getName().getString(),
                    null, bypassDelay, cooldown);
        } else {
            TeleportExecutor.request(requester, NamedLocation.of(target), "tpa", target.getName().getString(),
                    null, bypassDelay, cooldown);
        }
        return 1;
    }

    private static int cancel(CommandSourceStack source) {
        ServerPlayer requester = source.getPlayer();
        if (requester == null) return 0;

        boolean removedAny = PENDING.entrySet().removeIf(e -> e.getValue().fromUuid().equals(requester.getUUID()));
        if (!removedAny) {
            source.sendFailure(Component.literal("§cYou have no pending outgoing teleport request."));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("§7Teleport request cancelled."), false);
        return 1;
    }
}