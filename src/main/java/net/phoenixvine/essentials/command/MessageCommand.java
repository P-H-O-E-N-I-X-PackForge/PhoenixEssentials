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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MessageCommand {

    private static final Map<UUID, UUID> LAST_MESSAGED = new ConcurrentHashMap<>();

    private MessageCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (String alias : new String[] { "msg", "tell" }) {
            dispatcher.register(Commands.literal(alias)
                    .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.MSG))
                    .then(Commands.argument("player", StringArgumentType.word())
                            .then(Commands.argument("message", StringArgumentType.greedyString())
                                    .executes(ctx -> send(ctx.getSource(), StringArgumentType.getString(ctx, "player"),
                                            StringArgumentType.getString(ctx, "message"))))));
        }

        for (String alias : new String[] { "r", "reply" }) {
            dispatcher.register(Commands.literal(alias)
                    .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.MSG))
                    .then(Commands.argument("message", StringArgumentType.greedyString())
                            .executes(ctx -> reply(ctx.getSource(), StringArgumentType.getString(ctx, "message")))));
        }
    }

    private static int send(CommandSourceStack source, String targetName, String message) {
        ServerPlayer sender = source.getPlayer();
        if (sender == null) return 0;

        if (!EssentialsAPI.isFeatureEnabled(EssentialsAPI.FEATURE_SOCIAL, sender.level().dimension().location())) {
            source.sendFailure(Component.literal("§cSocial commands aren't available here right now."));
            return 0;
        }

        ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(targetName);
        if (target == null) {
            source.sendFailure(Component.literal("§cPlayer \"" + targetName + "\" isn't online."));
            return 0;
        }
        if (target == sender) {
            source.sendFailure(Component.literal("§cYou can't message yourself."));
            return 0;
        }

        PlayerEssentialsData targetData = target.getCapability(EssentialsCapabilityProvider.PLAYER_ESSENTIALS)
                .orElse(null);
        if (targetData != null && targetData.isIgnoring(sender.getUUID())) {
            source.sendFailure(Component.literal("§cThat player isn't receiving messages right now."));
            return 0;
        }

        sender.sendSystemMessage(Component.literal("§7[me -> " + target.getName().getString() + "] §f" + message));
        target.sendSystemMessage(Component.literal("§7[" + sender.getName().getString() + " -> me] §f" + message));

        LAST_MESSAGED.put(sender.getUUID(), target.getUUID());
        LAST_MESSAGED.put(target.getUUID(), sender.getUUID());
        return 1;
    }

    private static int reply(CommandSourceStack source, String message) {
        ServerPlayer sender = source.getPlayer();
        if (sender == null) return 0;

        UUID targetUuid = LAST_MESSAGED.get(sender.getUUID());
        if (targetUuid == null) {
            source.sendFailure(Component.literal("§cYou have no one to reply to."));
            return 0;
        }

        ServerPlayer target = source.getServer().getPlayerList().getPlayer(targetUuid);
        if (target == null) {
            source.sendFailure(Component.literal("§cThat player is no longer online."));
            return 0;
        }

        return send(source, target.getGameProfile().getName(), message);
    }
}
