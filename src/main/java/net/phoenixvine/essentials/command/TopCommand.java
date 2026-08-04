package net.phoenixvine.essentials.command;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.phoenixvine.essentials.config.EssentialsPermissions;
import net.phoenixvine.essentials.config.EssentialsServerConfig;
import net.phoenixvine.essentials.data.NamedLocation;

public final class TopCommand {

    private TopCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("top")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.TOP))
                .executes(ctx -> top(ctx.getSource())));
    }

    private static int top(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        if (!(player.level() instanceof ServerLevel level)) return 0;

        int x = player.getBlockX();
        int z = player.getBlockZ();
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);

        NamedLocation target = new NamedLocation(level.dimension(), x + 0.5, y, z + 0.5,
                player.getYRot(), player.getXRot());

        int cooldown = EssentialsServerConfig.TOP_COOLDOWN_SECONDS.get();
        TeleportExecutor.request(player, target, "top", "the surface", null, false, cooldown);
        return 1;
    }
}