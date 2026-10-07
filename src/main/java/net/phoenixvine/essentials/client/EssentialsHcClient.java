package net.phoenixvine.essentials.client;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.phoenixvine.essentials.EssentialsHcMode;
import net.phoenixvine.essentials.PhoenixEssentials;
import net.phoenixvine.essentials.hc.hcCompileMerged1;

import org.lwjgl.glfw.GLFW;

// Second menu key (GLFW_KEY_H). With the Hot Chocolate takeover (the default) Y opens the HC config
// screen (src/main/hotc/EssentialsConfigScreenHc.hotc) and H opens the ORIGINAL Java screen as a
// fallback for side-by-side comparison; with -Dphoenix_essentials.use_java=true Y is the original
// and H is the HC pilot, exactly as before the takeover. See EssentialsHcMode.
@Mod.EventBusSubscriber(modid = PhoenixEssentials.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class EssentialsHcClient {

    public static final KeyMapping OPEN_MENU_HC = new KeyMapping(
            "key.phoenix_essentials.open_menu_hc",
            GLFW.GLFW_KEY_H,
            "key.categories.phoenix_essentials");

    // The original screen closes when its own open-key (Y) is pressed again; the HC screen asks here.
    public static boolean isOpenMenuKey(int key, int scanCode) {
        return EssentialsKeyBindings.OPEN_MENU.matches(key, scanCode);
    }

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
                if (EssentialsHcMode.takeover()) {
                    net.minecraft.client.Minecraft.getInstance().setScreen(new EssentialsConfigScreen(null));
                } else {
                    hcCompileMerged1.open_essentials_config_screen_hc();
                }
            }
        }
    }
}
