package net.phoenixvine.essentials.command;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.phoenixvine.essentials.config.EssentialsPermissions;
import net.phoenixvine.essentials.config.EssentialsServerConfig;
import net.phoenixvine.essentials.data.NamedLocation;
import net.phoenixvine.essentials.rtp.RtpFinder;

public final class RtpCommand {

    private RtpCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("rtp")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.RTP))
                .executes(ctx -> randomTeleport(ctx.getSource())));
    }

    private static int randomTeleport(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        if (!(player.level() instanceof ServerLevel level)) return 0;

        var allowedDims = EssentialsServerConfig.RTP_ALLOWED_DIMENSIONS.get();
        if (!allowedDims.isEmpty() && !allowedDims.contains(level.dimension().location().toString())) {
            source.sendFailure(Component.literal("§c/rtp isn't allowed in this dimension."));
            return 0;
        }

        BlockPos origin = player.blockPosition();
        int minRadius = EssentialsServerConfig.RTP_MIN_RADIUS.get();
        int maxRadius = EssentialsServerConfig.RTP_MAX_RADIUS.get();
        int maxAttempts = EssentialsServerConfig.RTP_MAX_ATTEMPTS.get();

        RtpFinder.Result result = RtpFinder.find(level, origin, player, minRadius, maxRadius, maxAttempts);
        if (result == null) {
            source.sendFailure(Component.literal("§cCouldn't find a safe spot to land after " + maxAttempts +
                    " tries - try again."));
            return 0;
        }

        BlockPos landing = result.pos();
        double dx = landing.getX() - origin.getX();
        double dz = landing.getZ() - origin.getZ();
        int distance = (int) Math.round(Math.sqrt(dx * dx + dz * dz));

        NamedLocation target = new NamedLocation(level.dimension(), landing.getX() + 0.5, landing.getY(),
                landing.getZ() + 0.5, player.getYRot(), player.getXRot());
        String detail = distance + " blocks away, " + result.attempts() + " " +
                (result.attempts() == 1 ? "try" : "tries");

        int cooldown = EssentialsServerConfig.RTP_COOLDOWN_SECONDS.get();
        TeleportExecutor.request(player, target, "rtp", "a random location", detail, false, cooldown);
        return 1;
    }
}