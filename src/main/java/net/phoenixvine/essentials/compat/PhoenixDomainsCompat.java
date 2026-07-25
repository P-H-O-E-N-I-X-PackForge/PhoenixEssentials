package net.phoenixvine.essentials.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;

import java.util.Optional;
import java.util.UUID;

public final class PhoenixDomainsCompat {

    private static final boolean LOADED = ModList.get().isLoaded("phoenix_domains");

    private PhoenixDomainsCompat() {}

    public static boolean isLoaded() {
        return LOADED;
    }

    public static boolean isClaimedByOther(ServerLevel level, BlockPos pos, ServerPlayer player) {
        if (!LOADED) return false;

        net.phoenixvine.domains.data.ChunkKey key =
                net.phoenixvine.domains.data.ChunkKey.of(level, pos.getX(), pos.getZ());
        Optional<UUID> owner = net.phoenixvine.domains.api.DomainAPI.getOwner(player.getServer(), key);
        if (owner.isEmpty()) return false;

        return !net.phoenixvine.domains.ownership.DomainOwnership.isMemberOrSelf(player.getUUID(), owner.get());
    }
}
