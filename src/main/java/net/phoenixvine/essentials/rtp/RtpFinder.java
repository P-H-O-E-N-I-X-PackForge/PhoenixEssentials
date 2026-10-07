package net.phoenixvine.essentials.rtp;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.phoenixvine.essentials.compat.ClaimProtectionCompat;

import java.util.concurrent.ThreadLocalRandom;

public final class RtpFinder {

    public record Result(BlockPos pos, int attempts) {}

    private RtpFinder() {}

    public static Result find(ServerLevel level, BlockPos origin, ServerPlayer player, int minRadius,
                              int maxRadius, int maxAttempts) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            double angle = random.nextDouble(0, Math.PI * 2);
            double radius = minRadius + random.nextDouble(0, Math.max(1, maxRadius - minRadius));
            int x = origin.getX() + (int) Math.round(Math.cos(angle) * radius);
            int z = origin.getZ() + (int) Math.round(Math.sin(angle) * radius);

            if (!level.isInWorldBounds(new BlockPos(x, 0, z))) continue;

            level.getChunkSource().getChunk(x >> 4, z >> 4, true);

            if (ClaimProtectionCompat.isClaimedByOther(level, new BlockPos(x, 0, z), player)) continue;

            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos surface = findSafeSurface(level, x, y, z);
            if (surface != null) return new Result(surface, attempt + 1);
        }
        return null;
    }

    private static BlockPos findSafeSurface(ServerLevel level, int x, int startY, int z) {
        int minY = level.getMinBuildHeight();
        for (int y = Math.min(startY, level.getMaxBuildHeight() - 2); y > minY; y--) {
            BlockPos ground = new BlockPos(x, y, z);
            BlockPos head1 = ground.above();
            BlockPos head2 = ground.above(2);

            BlockState groundState = level.getBlockState(ground);
            if (groundState.isAir() || isUnsafeGround(level, ground)) continue;

            if (!level.getBlockState(head1).isAir() || !level.getBlockState(head2).isAir()) continue;
            if (!level.getFluidState(head1).isEmpty() || !level.getFluidState(head2).isEmpty()) continue;

            return head1.immutable();
        }
        return null;
    }

    private static boolean isUnsafeGround(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);

        return !level.getFluidState(pos).isEmpty() ||
                state.is(net.minecraft.world.level.block.Blocks.FIRE) ||
                state.is(net.minecraft.world.level.block.Blocks.MAGMA_BLOCK) ||
                state.is(net.minecraft.world.level.block.Blocks.CACTUS);
    }
}
