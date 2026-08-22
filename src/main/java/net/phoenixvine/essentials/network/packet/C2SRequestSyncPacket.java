package net.phoenixvine.essentials.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.phoenixvine.essentials.capability.EssentialsCapabilityProvider;
import net.phoenixvine.essentials.capability.PlayerEssentialsData;
import net.phoenixvine.essentials.config.EssentialsServerConfig;
import net.phoenixvine.essentials.data.KitDefinition;
import net.phoenixvine.essentials.data.KitRegistry;
import net.phoenixvine.essentials.data.WarpRegistry;
import net.phoenixvine.essentials.network.EssentialsNetwork;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class C2SRequestSyncPacket {

    public enum Kind { HOMES, WARPS, KITS, TRASH }

    private final Kind kind;

    public C2SRequestSyncPacket(Kind kind) {
        this.kind = kind;
    }

    public C2SRequestSyncPacket(FriendlyByteBuf buf) {
        this.kind = buf.readEnum(Kind.class);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(kind);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            switch (kind) {
                case HOMES -> {
                    PlayerEssentialsData data = player.getCapability(EssentialsCapabilityProvider.PLAYER_ESSENTIALS)
                            .orElse(null);
                    List<String> names = data == null ? List.of() :
                            data.getHomes().keySet().stream().sorted().toList();
                    EssentialsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                            new S2CHomesSyncPacket(names, EssentialsServerConfig.HOMES_PER_PLAYER.get()));
                }
                case WARPS -> {
                    List<String> names = WarpRegistry.getAll().keySet().stream().sorted().toList();
                    EssentialsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                            new S2CWarpsSyncPacket(names));
                }
                case KITS -> {
                    PlayerEssentialsData data = player.getCapability(EssentialsCapabilityProvider.PLAYER_ESSENTIALS)
                            .orElse(null);
                    List<S2CKitsSyncPacket.Entry> entries = new ArrayList<>();
                    for (KitDefinition kit : KitRegistry.getAll().values()) {
                        long lastClaimedMs = data == null ? 0 : data.getKitLastClaimedMs(kit.name);
                        long remainingMs = kit.cooldownSeconds * 1000L - (System.currentTimeMillis() - lastClaimedMs);
                        int remainingSeconds = lastClaimedMs <= 0 || remainingMs <= 0 ?
                                0 : (int) ((remainingMs + 999) / 1000);
                        entries.add(new S2CKitsSyncPacket.Entry(kit.name, remainingSeconds));
                    }
                    entries.sort((a, b) -> a.name().compareToIgnoreCase(b.name()));
                    EssentialsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                            new S2CKitsSyncPacket(entries));
                }
                case TRASH -> {
                    PlayerEssentialsData data = player.getCapability(EssentialsCapabilityProvider.PLAYER_ESSENTIALS)
                            .orElse(null);
                    List<String> ids = data == null ? List.of() :
                            data.getAlwaysTrash().stream().map(Object::toString).sorted().toList();
                    EssentialsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                            new S2CAutoTrashSyncPacket(ids));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
