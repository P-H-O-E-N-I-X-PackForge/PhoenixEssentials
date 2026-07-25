package net.phoenixvine.essentials.command;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.phoenixvine.essentials.config.EssentialsPermissions;
import net.phoenixvine.essentials.data.NamedLocation;

public final class TpxCommand {

    private TpxCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tpx")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.TPX))
                .then(Commands.argument("dimension", DimensionArgument.dimension())
                        .executes(ctx -> tpx(ctx.getSource(), DimensionArgument.getDimension(ctx, "dimension")))));
    }

    private static int tpx(CommandSourceStack source, ServerLevel targetLevel) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        if (player.level() == targetLevel) {
            source.sendFailure(Component.literal("§cYou're already in that dimension."));
            return 0;
        }

        int x = player.getBlockX();
        int z = player.getBlockZ();
        int y = targetLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);

        NamedLocation target = new NamedLocation(targetLevel.dimension(), x + 0.5, y, z + 0.5,
                player.getYRot(), player.getXRot());
        TeleportExecutor.request(player, target, "tpx",
                targetLevel.dimension().location().toString());
        return 1;
    }
}
