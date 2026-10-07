package net.phoenixvine.essentials.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.phoenixvine.essentials.PhoenixEssentials;
import net.phoenixvine.wiki.client.suite.SuiteHudBar;
import net.phoenixvine.wiki.theme.PhoenixTheme;

/**
 * All client-only bootstrap lives here so {@link PhoenixEssentials} never references a client class.
 * <p>
 * The class is only ever loaded on the physical client: the {@code value = Dist.CLIENT} subscriber is
 * skipped by Forge before the class is loaded, and {@link #registerConfigScreen()} is only reached
 * through {@code DistExecutor}.
 */
@Mod.EventBusSubscriber(modid = PhoenixEssentials.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class EssentialsClientProxy {

    private EssentialsClientProxy() {}

    public static void registerConfigScreen() {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, screen) -> new EssentialsConfigScreen(screen)));
    }

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event) {
        PhoenixEssentials.LOGGER.info("[Phoenix Essentials] Client setup complete.");

        // Lets the suite's shared/per-mod theme toggle (see PhoenixTheme#setSharedMode) tell this
        // mod's own code apart from every other Phoenix mod's when they call the no-arg theme
        // accessors -- see PhoenixTheme#resolveCallerModId.
        PhoenixTheme.registerMod("net.phoenixvine.essentials", PhoenixEssentials.MOD_ID);

        SuiteHudBar.register("phoenix_essentials.settings", SuiteHudBar.PRIORITY_ESSENTIALS,
                new ResourceLocation(PhoenixEssentials.MOD_ID, "textures/gui/suite_bar_icon.png"),
                Component.literal("§fOpen Essentials Menu"),
                () -> Minecraft.getInstance()
                        .setScreen(new EssentialsConfigScreen(Minecraft.getInstance().screen)));

        SuiteHudBar.register("phoenix_essentials.trash", SuiteHudBar.PRIORITY_ESSENTIALS + 1,
                new ResourceLocation(PhoenixEssentials.MOD_ID, "textures/gui/trash_icon.png"),
                () -> Component.literal("§fOpen Trash"), () -> 1,
                () -> Minecraft.getInstance().player.connection.sendCommand("essentialstrash"),
                16, 16, true);

        SuiteHudBar.register("phoenix_essentials.gamemode", SuiteHudBar.PRIORITY_ESSENTIALS + 2,
                new ResourceLocation(PhoenixEssentials.MOD_ID, "textures/gui/gamemode_icon.png"),
                () -> Component.literal("§fSwitch Gamemode"),
                () -> EssentialsSuiteBarButton.isLocalPlayerOp() ? 1 : 0,
                () -> {
                    Minecraft mc = Minecraft.getInstance();
                    mc.player.connection.sendCommand("gamemode " +
                            EssentialsSuiteBarButton.nextGameMode(mc).getName());
                },
                16, 16, true);

        SuiteHudBar.register("phoenix_essentials.weather", SuiteHudBar.PRIORITY_ESSENTIALS + 3,
                new ResourceLocation(PhoenixEssentials.MOD_ID, "textures/gui/weather_icon.png"),
                () -> Component.literal("§fSwitch Weather"),
                () -> EssentialsSuiteBarButton.isLocalPlayerOp() ? 1 : 0,
                () -> EssentialsSuiteBarButton.cycleWeather(Minecraft.getInstance()),
                16, 16, true);

        SuiteHudBar.register("phoenix_essentials.time", SuiteHudBar.PRIORITY_ESSENTIALS + 4,
                new ResourceLocation(PhoenixEssentials.MOD_ID, "textures/gui/time_icon.png"),
                () -> Component.literal("§fSwitch Time of Day"),
                () -> EssentialsSuiteBarButton.isLocalPlayerOp() ? 1 : 0,
                () -> {
                    Minecraft mc = Minecraft.getInstance();
                    mc.player.connection.sendCommand("time set " + EssentialsSuiteBarButton.nextTime(mc));
                },
                16, 16, true);
    }
}
