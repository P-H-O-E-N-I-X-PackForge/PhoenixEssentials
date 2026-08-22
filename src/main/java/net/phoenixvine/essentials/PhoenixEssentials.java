package net.phoenixvine.essentials;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.phoenixvine.essentials.capability.EssentialsCapabilityProvider;
import net.phoenixvine.essentials.client.EssentialsClientProxy;
import net.phoenixvine.essentials.client.EssentialsConfigScreen;
import net.phoenixvine.essentials.client.EssentialsSuiteBarButton;
import net.phoenixvine.essentials.command.EssentialsCommands;
import net.phoenixvine.essentials.config.EssentialsConfigOverrides;
import net.phoenixvine.essentials.config.EssentialsServerConfig;
import net.phoenixvine.essentials.data.KitRegistry;
import net.phoenixvine.essentials.data.SpawnRegistry;
import net.phoenixvine.essentials.data.WarpRegistry;
import net.phoenixvine.essentials.network.EssentialsNetwork;
import net.phoenixvine.wiki.client.suite.SuiteHudBar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(PhoenixEssentials.MOD_ID)
@SuppressWarnings("removal")
public class PhoenixEssentials {

    public static final String MOD_ID = "phoenix_essentials";
    public static final Logger LOGGER = LogManager.getLogger();

    public PhoenixEssentials() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(EssentialsCapabilityProvider::register);
        modEventBus.addListener(EssentialsConfigOverrides::onLoading);
        modEventBus.addListener(EssentialsConfigOverrides::onReloading);

        MinecraftForge.EVENT_BUS.register(this);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> EssentialsClientProxy::registerConfigScreen);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, EssentialsServerConfig.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            LOGGER.info("[Phoenix Essentials] Bootstrapping server essentials...");
            EssentialsNetwork.init();
        });
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("[Phoenix Essentials] Client setup complete.");

        SuiteHudBar.register("phoenix_essentials.settings", SuiteHudBar.PRIORITY_ESSENTIALS,
                new ResourceLocation(MOD_ID, "textures/gui/suite_bar_icon.png"),
                Component.literal("§fOpen Essentials Menu"),
                () -> Minecraft.getInstance()
                        .setScreen(new EssentialsConfigScreen(Minecraft.getInstance().screen)));

        SuiteHudBar.register("phoenix_essentials.trash", SuiteHudBar.PRIORITY_ESSENTIALS + 1,
                new ResourceLocation(MOD_ID, "textures/gui/trash_icon.png"),
                () -> Component.literal("§fOpen Trash"), () -> 1,
                () -> Minecraft.getInstance().player.connection.sendCommand("essentialstrash"),
                16, 16, true);

        SuiteHudBar.register("phoenix_essentials.gamemode", SuiteHudBar.PRIORITY_ESSENTIALS + 2,
                new ResourceLocation(MOD_ID, "textures/gui/gamemode_icon.png"),
                () -> Component.literal("§fSwitch Gamemode"),
                () -> EssentialsSuiteBarButton.isLocalPlayerOp() ? 1 : 0,
                () -> {
                    Minecraft mc = Minecraft.getInstance();
                    mc.player.connection.sendCommand("gamemode " +
                            EssentialsSuiteBarButton.nextGameMode(mc).getName());
                },
                16, 16, true);

        SuiteHudBar.register("phoenix_essentials.weather", SuiteHudBar.PRIORITY_ESSENTIALS + 3,
                new ResourceLocation(MOD_ID, "textures/gui/weather_icon.png"),
                () -> Component.literal("§fSwitch Weather"),
                () -> EssentialsSuiteBarButton.isLocalPlayerOp() ? 1 : 0,
                () -> EssentialsSuiteBarButton.cycleWeather(Minecraft.getInstance()),
                16, 16, true);

        SuiteHudBar.register("phoenix_essentials.time", SuiteHudBar.PRIORITY_ESSENTIALS + 4,
                new ResourceLocation(MOD_ID, "textures/gui/time_icon.png"),
                () -> Component.literal("§fSwitch Time of Day"),
                () -> EssentialsSuiteBarButton.isLocalPlayerOp() ? 1 : 0,
                () -> {
                    Minecraft mc = Minecraft.getInstance();
                    mc.player.connection.sendCommand("time set " + EssentialsSuiteBarButton.nextTime(mc));
                },
                16, 16, true);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        WarpRegistry.load();
        KitRegistry.load();
        SpawnRegistry.load();
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        EssentialsCommands.registerAll(event.getDispatcher());
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
