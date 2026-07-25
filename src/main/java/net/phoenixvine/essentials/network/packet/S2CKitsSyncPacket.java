package net.phoenixvine.essentials.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.phoenixvine.essentials.client.EssentialsClientCache;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class S2CKitsSyncPacket {

    public record Entry(String name, int cooldownRemainingSeconds) {}

    public final List<Entry> entries;

    public S2CKitsSyncPacket(List<Entry> entries) {
        this.entries = entries;
    }

    public S2CKitsSyncPacket(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            entries.add(new Entry(buf.readUtf(), buf.readVarInt()));
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entries.size());
        for (Entry e : entries) {
            buf.writeUtf(e.name());
            buf.writeVarInt(e.cooldownRemainingSeconds());
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    List<EssentialsClientCache.KitEntry> mapped = entries.stream()
                            .map(e -> new EssentialsClientCache.KitEntry(e.name(), e.cooldownRemainingSeconds()))
                            .toList();
                    EssentialsClientCache.setKits(mapped);
                }));
        ctx.get().setPacketHandled(true);
    }
}
