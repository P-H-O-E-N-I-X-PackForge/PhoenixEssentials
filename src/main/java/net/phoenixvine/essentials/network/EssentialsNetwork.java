package net.phoenixvine.essentials.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.phoenixvine.essentials.network.packet.C2SGuiClaimKitPacket;
import net.phoenixvine.essentials.network.packet.C2SGuiTeleportPacket;
import net.phoenixvine.essentials.network.packet.C2SRequestSyncPacket;
import net.phoenixvine.essentials.network.packet.S2CAutoTrashSyncPacket;
import net.phoenixvine.essentials.network.packet.S2CHomesSyncPacket;
import net.phoenixvine.essentials.network.packet.S2CKitsSyncPacket;
import net.phoenixvine.essentials.network.packet.S2CWarpsSyncPacket;

import java.util.Optional;

public class EssentialsNetwork {

    private static final String PROTOCOL = "1";

    public static SimpleChannel CHANNEL;
    private static int id = 0;

    public static void init() {
        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation("phoenix_essentials", "main"),
                () -> PROTOCOL,
                PROTOCOL::equals,
                PROTOCOL::equals);

        CHANNEL.registerMessage(id++,
                C2SRequestSyncPacket.class,
                C2SRequestSyncPacket::encode,
                C2SRequestSyncPacket::new,
                C2SRequestSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        CHANNEL.registerMessage(id++,
                C2SGuiTeleportPacket.class,
                C2SGuiTeleportPacket::encode,
                C2SGuiTeleportPacket::new,
                C2SGuiTeleportPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        CHANNEL.registerMessage(id++,
                C2SGuiClaimKitPacket.class,
                C2SGuiClaimKitPacket::encode,
                C2SGuiClaimKitPacket::new,
                C2SGuiClaimKitPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        CHANNEL.registerMessage(id++,
                S2CHomesSyncPacket.class,
                S2CHomesSyncPacket::encode,
                S2CHomesSyncPacket::new,
                S2CHomesSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        CHANNEL.registerMessage(id++,
                S2CWarpsSyncPacket.class,
                S2CWarpsSyncPacket::encode,
                S2CWarpsSyncPacket::new,
                S2CWarpsSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        CHANNEL.registerMessage(id++,
                S2CKitsSyncPacket.class,
                S2CKitsSyncPacket::encode,
                S2CKitsSyncPacket::new,
                S2CKitsSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        CHANNEL.registerMessage(id++,
                S2CAutoTrashSyncPacket.class,
                S2CAutoTrashSyncPacket::encode,
                S2CAutoTrashSyncPacket::new,
                S2CAutoTrashSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}
