package net.phoenixvine.essentials.client;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.phoenixvine.essentials.PhoenixEssentials;
import net.phoenixvine.essentials.hc.hcCompileMerged1;

import org.lwjgl.glfw.GLFW;

// Real, disclosed pilot trigger for the hotc port of EssentialsConfigScreen (src/main/hotc/
// EssentialsConfigScreenHc.hotc) -- deliberately a SEPARATE key from EssentialsKeyBindings.
// OPEN_MENU (Y), not a replacement of it, while the HC version is still being verified. The real
// menu stays reachable on Y exactly as before; GLFW_KEY_H opens the HC pilot alongside it. Once
// the HC port (and a ported EssentialsListScreen) are trusted, this can replace
// EssentialsInputHandler's own `new EssentialsConfigScreen(null)` call instead of living beside it.
@Mod.EventBusSubscriber(modid = PhoenixEssentials.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class EssentialsHcClient {

    public static final KeyMapping OPEN_MENU_HC = new KeyMapping(
            "key.phoenix_essentials.open_menu_hc",
            GLFW.GLFW_KEY_H,
            "key.categories.phoenix_essentials");

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_MENU_HC);
    }

    @Mod.EventBusSubscriber(modid = PhoenixEssentials.MOD_ID, value = Dist.CLIENT)
    public static class ClientTicker {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            while (OPEN_MENU_HC.consumeClick()) {
                hcCompileMerged1.open_essentials_config_screen_hc();
            }
        }
    }
}
