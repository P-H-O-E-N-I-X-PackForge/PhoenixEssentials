package net.phoenixvine.essentials.command;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.phoenixvine.essentials.capability.EssentialsCapabilityProvider;
import net.phoenixvine.essentials.capability.PlayerEssentialsData;
import net.phoenixvine.essentials.config.EssentialsPermissions;
import net.phoenixvine.essentials.config.EssentialsServerConfig;
import net.phoenixvine.essentials.data.NamedLocation;

public final class BackCommand {

    private BackCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("back")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.BACK))
                .executes(ctx -> teleportBack(ctx.getSource())));
    }

    private static int teleportBack(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        PlayerEssentialsData data = player.getCapability(EssentialsCapabilityProvider.PLAYER_ESSENTIALS).orElse(null);
        if (data == null) return 0;

        NamedLocation back = data.getBack();
        if (back == null) {
            source.sendFailure(Component.literal("§cYou have nowhere to go back to."));
            return 0;
        }

        int cooldown = EssentialsServerConfig.TELEPORT_COOLDOWN_SECONDS.get();
        TeleportExecutor.request(player, back, "back", "your previous location", null, false, cooldown);
        return 1;
    }
}