package net.phoenixvine.essentials.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.phoenixvine.essentials.PhoenixEssentials;
import net.phoenixvine.essentials.hc.hcCompileMerged1;

// Client side of the AFK overlay (src/main/hotc/EssentialsAfkOverlay.hotc). The ORIGINAL mod has the
// "Show AFK overlay indicator" setting (EssentialsSettings / the config screen toggle) but no code
// that reads it -- there is no overlay to port -- so this is a new feature: a HUD badge drawn by HC
// whenever the server says this player is AFK and the setting is on.
//
// This class owns the two things HC can't express yet: registering a HUD overlay (needs the
// RegisterGuiOverlaysEvent mod-bus event + an IGuiOverlay lambda) and the small piece of client
// state (the synced AFK flag + when it started, for the "AFK 2m 5s" label).
@Mod.EventBusSubscriber(modid = PhoenixEssentials.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class EssentialsHcAfkOverlay {

    private static volatile boolean active = false;
    private static volatile long sinceMs = 0L;

    private EssentialsHcAfkOverlay() {}

    // Called by the HC packet handler (main thread, via enqueueWork) when the server syncs the flag.
    public static void setActive(boolean afk) {
        active = afk;
        sinceMs = afk ? System.currentTimeMillis() : 0L;
    }

    public static boolean isActive() {
        return active;
    }

    // "AFK" plus elapsed time once it passes a second, e.g. "AFK 2m 5s".
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
