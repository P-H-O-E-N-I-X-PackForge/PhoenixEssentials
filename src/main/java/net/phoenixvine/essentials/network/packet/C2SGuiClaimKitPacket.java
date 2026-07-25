package net.phoenixvine.essentials.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.phoenixvine.essentials.api.EssentialsAPI;
import net.phoenixvine.essentials.capability.EssentialsCapabilityProvider;
import net.phoenixvine.essentials.capability.PlayerEssentialsData;
import net.phoenixvine.essentials.data.KitDefinition;
import net.phoenixvine.essentials.data.KitItemEntry;
import net.phoenixvine.essentials.data.KitRegistry;

import java.util.function.Supplier;

public class C2SGuiClaimKitPacket {

    private final String name;

    public C2SGuiClaimKitPacket(String name) {
        this.name = name;
    }

    public C2SGuiClaimKitPacket(FriendlyByteBuf buf) {
        this.name = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(name);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            if (!EssentialsAPI.isFeatureEnabled(EssentialsAPI.FEATURE_KITS, player.level().dimension().location())) {
                player.sendSystemMessage(Component.literal("§cKits aren't available here right now."));
                return;
            }

            KitDefinition kit = KitRegistry.get(name);
            if (kit == null) return;

            PlayerEssentialsData data = player.getCapability(EssentialsCapabilityProvider.PLAYER_ESSENTIALS)
                    .orElse(null);
            if (data == null) return;

            long lastClaimedMs = data.getKitLastClaimedMs(name);
            long remainingMs = kit.cooldownSeconds * 1000L - (System.currentTimeMillis() - lastClaimedMs);
            if (lastClaimedMs > 0 && remainingMs > 0) {
                long seconds = (remainingMs + 999) / 1000;
                player.sendSystemMessage(Component.literal("§cYou can claim \"" + name + "\" again in " +
                        seconds + "s."));
                return;
            }

            for (KitItemEntry entry : kit.items) {
                ItemStack stack = entry.toStack();
                if (stack.isEmpty()) continue;
                if (!player.getInventory().add(stack)) {
                    player.drop(stack, false);
                }
            }

            data.recordKitClaimed(name);
            player.sendSystemMessage(Component.literal("§aClaimed kit \"" + name + "\"."));
        });
        ctx.get().setPacketHandled(true);
    }
}
