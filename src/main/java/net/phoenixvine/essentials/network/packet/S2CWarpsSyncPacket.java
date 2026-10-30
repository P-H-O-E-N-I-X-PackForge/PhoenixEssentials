package net.phoenixvine.essentials.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import hotc.mc.net.HcChannel;

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

    public void handle(Supplier<HcChannel.Context> ctx) {
        ctx.get().enqueueWork(() ->
                net.phoenixvine.essentials.client.EssentialsClientCache.setWarps(names));
        ctx.get().setPacketHandled(true);
    }
}
