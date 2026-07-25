package net.phoenixvine.essentials.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class ClaimProtectionCompat {

    private ClaimProtectionCompat() {}

    public static boolean isClaimedByOther(ServerLevel level, BlockPos pos, ServerPlayer player) {
        return FtbChunksCompat.isClaimedByOther(level, pos, player) ||
                PhoenixDomainsCompat.isClaimedByOther(level, pos, player);
    }
}
