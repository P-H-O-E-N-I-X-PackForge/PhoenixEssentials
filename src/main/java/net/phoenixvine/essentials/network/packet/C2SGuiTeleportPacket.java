package net.phoenixvine.essentials.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.phoenixvine.essentials.capability.EssentialsCapabilityProvider;
import net.phoenixvine.essentials.capability.PlayerEssentialsData;
import net.phoenixvine.essentials.command.TeleportExecutor;
import net.phoenixvine.essentials.data.NamedLocation;
import net.phoenixvine.essentials.data.WarpRegistry;

import java.util.function.Supplier;

public class C2SGuiTeleportPacket {

    public enum Kind { HOME, WARP, BACK }

    private final Kind kind;
    private final String name;

    public C2SGuiTeleportPacket(Kind kind, String name) {
        this.kind = kind;
        this.name = name == null ? "" : name;
    }

    public C2SGuiTeleportPacket(FriendlyByteBuf buf) {
        this.kind = buf.readEnum(Kind.class);
        this.name = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(kind);
        buf.writeUtf(name);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            switch (kind) {
                case HOME -> {
                    PlayerEssentialsData data = player.getCapability(EssentialsCapabilityProvider.PLAYER_ESSENTIALS)
                            .orElse(null);
                    if (data == null) return;
                    NamedLocation home = data.getHome(name);
                    if (home == null) return;
                    TeleportExecutor.request(player, home, "home", "home \"" + name + "\"");
                }
                case WARP -> {
                    NamedLocation warp = WarpRegistry.get(name);
                    if (warp == null) return;
                    TeleportExecutor.request(player, warp, "warp", "warp \"" + name + "\"");
                }
                case BACK -> {
                    PlayerEssentialsData data = player.getCapability(EssentialsCapabilityProvider.PLAYER_ESSENTIALS)
                            .orElse(null);
                    if (data == null) return;
                    NamedLocation back = data.getBack();
                    if (back == null) {
                        player.sendSystemMessage(Component.literal("§cYou have nowhere to go back to."));
                        return;
                    }
                    TeleportExecutor.request(player, back, "back", "your previous location");
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
