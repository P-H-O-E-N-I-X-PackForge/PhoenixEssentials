package net.phoenixvine.essentials.capability;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.phoenixvine.essentials.PhoenixEssentials;
import net.phoenixvine.essentials.config.EssentialsServerConfig;
import net.phoenixvine.essentials.data.NamedLocation;
import net.phoenixvine.essentials.data.OfflinePlayerIndex;

@EventBusSubscriber(modid = PhoenixEssentials.MOD_ID)
public class EssentialsPlayerEvents {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        EssentialsCapabilityProvider.get(player).addPlaytimeTicks(1);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) return;
        if (event.getEntity() instanceof ServerPlayer player) {
            OfflinePlayerIndex.recordSeen(player.getUUID(), player.getGameProfile().getName());
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) return;
        if (event.getEntity() instanceof ServerPlayer player) {
            OfflinePlayerIndex.recordSeen(player.getUUID(), player.getGameProfile().getName());
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) return;
        if (!EssentialsServerConfig.BACK_AFTER_DEATH.get()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        EssentialsCapabilityProvider.get(player).setBack(NamedLocation.of(player));
    }
}
