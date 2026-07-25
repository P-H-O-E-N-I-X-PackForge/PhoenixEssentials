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

public final class PlaytimeCommand {

    private PlaytimeCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("playtime")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.PLAYTIME))
                .executes(ctx -> playtime(ctx.getSource(), null))
                .then(Commands.argument("player", StringArgumentType.word())
                        .executes(ctx -> playtime(ctx.getSource(), StringArgumentType.getString(ctx, "player")))));
    }

    private static int playtime(CommandSourceStack source, String targetName) {
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

        PlayerEssentialsData data = target.getCapability(EssentialsCapabilityProvider.PLAYER_ESSENTIALS).orElse(null);
        if (data == null) return 0;

        ServerPlayer finalTarget = target;
        source.sendSuccess(() -> Component.literal("§7" + finalTarget.getGameProfile().getName() +
                "'s playtime: §f" + format(data.getPlaytimeTicks())), false);
        return 1;
    }

    static String format(long ticks) {
        long totalSeconds = ticks / 20;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();
        if (hours > 0) sb.append(hours).append("h ");
        if (hours > 0 || minutes > 0) sb.append(minutes).append("m ");
        sb.append(seconds).append("s");
        return sb.toString();
    }
}
