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

public final class PlayerInfoCommand {

    private PlayerInfoCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("playerinfo")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.PLAYERINFO))
                .executes(ctx -> info(ctx.getSource(), null))
                .then(Commands.argument("player", StringArgumentType.word())
                        .executes(ctx -> info(ctx.getSource(), StringArgumentType.getString(ctx, "player")))));
    }

    private static int info(CommandSourceStack source, String targetName) {
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

        String nickname = data.getNickname();
        String displayName = nickname.isEmpty() ? target.getGameProfile().getName() :
                nickname + " §7(" + target.getGameProfile().getName() + "§7)";

        source.sendSuccess(() -> Component.literal("§7--- §f" + displayName + " §7---"), false);
        source.sendSuccess(() -> Component.literal("§7Gamemode: §f" +
                target.gameMode.getGameModeForPlayer().getName()), false);
        source.sendSuccess(() -> Component.literal("§7Health: §f" + Math.round(target.getHealth()) + "/" +
                Math.round(target.getMaxHealth()) + " §7Food: §f" + target.getFoodData().getFoodLevel() + "/20"), false);
        source.sendSuccess(() -> Component.literal("§7Playtime: §f" + PlaytimeCommand.format(data.getPlaytimeTicks())),
                false);
        source.sendSuccess(() -> Component.literal("§7Homes: §f" + data.getHomes().size() + "/" +
                EssentialsServerConfig.HOMES_PER_PLAYER.get()), false);
        source.sendSuccess(() -> Component.literal("§7AFK: §f" + (data.isAfk() ? "yes" : "no")), false);
        return 1;
    }
}
