package net.phoenixvine.essentials.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.phoenixvine.essentials.PhoenixEssentials;
import net.phoenixvine.essentials.hc.hcCompileMerged1;

@Mod.EventBusSubscriber(modid = PhoenixEssentials.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class EssentialsHcAfkOverlay {

    private static volatile boolean active = false;
    private static volatile long sinceMs = 0L;

    private EssentialsHcAfkOverlay() {}

    public static void setActive(boolean afk) {
        active = afk;
        sinceMs = afk ? System.currentTimeMillis() : 0L;
    }

    public static boolean isActive() {
        return active;
    }

    public static String label() {
        if (!active) return "";
        long seconds = (System.currentTimeMillis() - sinceMs) / 1000;
        if (seconds < 1) return "AFK";
        long minutes = seconds / 60;
        long hours = minutes / 60;
        String elapsed;
        if (hours > 0) elapsed = hours + "h " + (minutes % 60) + "m";
        else if (minutes > 0) elapsed = minutes + "m " + (seconds % 60) + "s";
        else elapsed = seconds + "s";
        return "AFK " + elapsed;
    }

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("afk_hc", (gui, graphics, partialTick, width, height) ->
                hcCompileMerged1.render_afk_overlay(graphics, width, height));
    }
}
