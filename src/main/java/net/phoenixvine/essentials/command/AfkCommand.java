package net.phoenixvine.essentials.command;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.phoenixvine.essentials.api.EssentialsAPI;
import net.phoenixvine.essentials.capability.EssentialsCapabilityProvider;
import net.phoenixvine.essentials.capability.PlayerEssentialsData;
import net.phoenixvine.essentials.config.EssentialsPermissions;

public final class AfkCommand {

    private AfkCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("afk")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.AFK))
                .executes(ctx -> toggle(ctx.getSource())));
    }

    private static int toggle(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        if (!EssentialsAPI.isFeatureEnabled(EssentialsAPI.FEATURE_SOCIAL, player.level().dimension().location())) {
            source.sendFailure(Component.literal("§cSocial commands aren't available here right now."));
            return 0;
        }

        PlayerEssentialsData data = player.getCapability(EssentialsCapabilityProvider.PLAYER_ESSENTIALS).orElse(null);
        if (data == null) return 0;

        boolean nowAfk = !data.isAfk();
        data.setAfk(nowAfk);

        Component broadcast = Component.literal("§7" + player.getName().getString() +
                (nowAfk ? " is now AFK." : " is no longer AFK."));
        source.getServer().getPlayerList().broadcastSystemMessage(broadcast, false);
        return 1;
    }
}
