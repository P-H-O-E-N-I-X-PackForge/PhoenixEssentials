package net.phoenixvine.essentials;

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
import net.phoenixvine.essentials.command.EssentialsCommands;
import net.phoenixvine.essentials.config.EssentialsServerConfig;
import net.phoenixvine.essentials.data.KitRegistry;
import net.phoenixvine.essentials.data.SpawnRegistry;
import net.phoenixvine.essentials.data.WarpRegistry;
import net.phoenixvine.essentials.network.EssentialsNetwork;

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
