package net.phoenixvine.essentials.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.phoenixvine.essentials.config.EssentialsPermissions;
import net.phoenixvine.essentials.data.OfflinePlayerIndex;

import java.util.UUID;

public final class SeenCommand {

    private SeenCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("seen")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.SEEN))
                .then(Commands.argument("player", StringArgumentType.word())
                        .executes(ctx -> seen(ctx.getSource(), StringArgumentType.getString(ctx, "player")))));
    }

    private static int seen(CommandSourceStack source, String name) {
        ServerPlayer online = source.getServer().getPlayerList().getPlayerByName(name);
        if (online != null) {
            source.sendSuccess(() -> Component.literal("§f" + online.getGameProfile().getName() + " §7is online now."),
                    false);
            return 1;
        }

        UUID uuid = source.getServer().getProfileCache() == null ? null :
                source.getServer().getProfileCache().get(name).map(p -> p.getId()).orElse(null);
        OfflinePlayerIndex.Entry entry = uuid == null ? null : OfflinePlayerIndex.get(uuid);
        if (entry == null) {
            source.sendFailure(Component.literal("§cNo record of a player named \"" + name + "\"."));
            return 0;
        }

        long elapsedMs = System.currentTimeMillis() - entry.lastSeenMs;
        source.sendSuccess(() -> Component.literal("§f" + entry.lastKnownName + " §7was last seen " +
                formatElapsed(elapsedMs) + " ago."), false);
        return 1;
    }

    private static String formatElapsed(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) return days + "d " + (hours % 24) + "h";
        if (hours > 0) return hours + "h " + (minutes % 60) + "m";
        if (minutes > 0) return minutes + "m";
        return seconds + "s";
    }
}
