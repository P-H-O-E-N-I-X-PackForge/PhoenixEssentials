package net.phoenixvine.essentials.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class S2CWarpsSyncPacket {

    public final List<String> names;

    public S2CWarpsSyncPacket(List<String> names) {
        this.names = names;
    }

    public S2CWarpsSyncPacket(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        names = new ArrayList<>(count);
        for (int i = 0; i < count; i++) names.add(buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(names.size());
        for (String name : names) buf.writeUtf(name);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        net.phoenixvine.essentials.client.EssentialsClientCache.setWarps(names)));
        ctx.get().setPacketHandled(true);
    }
}
