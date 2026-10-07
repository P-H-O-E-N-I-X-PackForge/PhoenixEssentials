package net.phoenixvine.essentials.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.phoenixvine.essentials.PhoenixEssentials;

@Mod.EventBusSubscriber(modid = PhoenixEssentials.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class EssentialsInputHandler {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) {
            EssentialsKeyBindings.OPEN_MENU.consumeClick();
            return;
        }

        if (EssentialsKeyBindings.OPEN_MENU.consumeClick()) {
            // Hot Chocolate port by default (src/main/hotc); -Dphoenix_essentials.use_java=true restores
            // the original Java screen. See EssentialsHcMode.
            if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) {
                net.phoenixvine.essentials.hc.hcCompileMerged1.open_essentials_config_screen_hc();
            } else {
                mc.setScreen(new EssentialsConfigScreen(null));
            }
        }
    }
}
