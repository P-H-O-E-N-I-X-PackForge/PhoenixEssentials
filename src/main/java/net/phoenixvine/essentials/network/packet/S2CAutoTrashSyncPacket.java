package net.phoenixvine.essentials.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import hotc.mc.net.HcChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class S2CAutoTrashSyncPacket {

    public final List<String> ids;

    public S2CAutoTrashSyncPacket(List<String> ids) {
        this.ids = ids;
    }

    public S2CAutoTrashSyncPacket(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        ids = new ArrayList<>(count);
        for (int i = 0; i < count; i++) ids.add(buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(ids.size());
        for (String id : ids) buf.writeUtf(id);
    }

    public void handle(Supplier<HcChannel.Context> ctx) {
        ctx.get().enqueueWork(() ->
                net.phoenixvine.essentials.client.EssentialsClientCache.setAutoTrash(ids));
        ctx.get().setPacketHandled(true);
    }
}
