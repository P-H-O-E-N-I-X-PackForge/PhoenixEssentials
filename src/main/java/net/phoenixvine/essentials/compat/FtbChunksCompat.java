package net.phoenixvine.essentials.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;

public final class FtbChunksCompat {

    private static final boolean LOADED = ModList.get().isLoaded("ftbchunks");

    private FtbChunksCompat() {}

    public static boolean isLoaded() {
        return LOADED;
    }

    public static boolean isClaimedByOther(ServerLevel level, BlockPos pos, ServerPlayer player) {
        if (!LOADED) return false;

        dev.ftb.mods.ftblibrary.math.ChunkDimPos chunkPos =
                new dev.ftb.mods.ftblibrary.math.ChunkDimPos(level, pos);
        dev.ftb.mods.ftbchunks.api.ClaimedChunk claim =
                dev.ftb.mods.ftbchunks.api.FTBChunksAPI.api().getManager().getChunk(chunkPos);
        if (claim == null) return false;

        return !claim.getTeamData().isTeamMember(player.getUUID());
    }
}
