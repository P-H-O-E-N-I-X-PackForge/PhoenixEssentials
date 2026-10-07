package net.phoenixvine.essentials.capability;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.phoenixvine.essentials.PhoenixEssentials;
import net.phoenixvine.essentials.config.EssentialsServerConfig;
import net.phoenixvine.essentials.data.NamedLocation;
import net.phoenixvine.essentials.data.OfflinePlayerIndex;

@Mod.EventBusSubscriber(modid = PhoenixEssentials.MOD_ID)
public class EssentialsPlayerEvents {

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) return; // handled by EssentialsPlayerEvents.hotc
        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(EssentialsCapabilityProvider.PLAYER_ESSENTIALS)
                .ifPresent(oldData -> event.getEntity().getCapability(EssentialsCapabilityProvider.PLAYER_ESSENTIALS)
                        .ifPresent(newData -> newData.deserializeNBT(oldData.serializeNBT())));
        event.getOriginal().invalidateCaps();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) return; // handled by EssentialsPlayerEvents.hotc
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        player.getCapability(EssentialsCapabilityProvider.PLAYER_ESSENTIALS)
                .ifPresent(data -> data.addPlaytimeTicks(1));
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) return; // handled by EssentialsPlayerEvents.hotc
        if (event.getEntity() instanceof ServerPlayer player) {
            OfflinePlayerIndex.recordSeen(player.getUUID(), player.getGameProfile().getName());
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) return; // handled by EssentialsPlayerEvents.hotc
        if (event.getEntity() instanceof ServerPlayer player) {
            OfflinePlayerIndex.recordSeen(player.getUUID(), player.getGameProfile().getName());
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) return; // handled by EssentialsPlayerEvents.hotc
        if (!EssentialsServerConfig.BACK_AFTER_DEATH.get()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        player.getCapability(EssentialsCapabilityProvider.PLAYER_ESSENTIALS)
                .ifPresent(data -> data.setBack(NamedLocation.of(player)));
    }
}
