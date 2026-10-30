package net.phoenixvine.essentials;

import hotc.mc.net.HcChannel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.phoenixvine.essentials.capability.EssentialsCapabilityProvider;
import net.phoenixvine.essentials.client.EssentialsClientProxy;
import net.phoenixvine.essentials.hc.hcCompileMerged1;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(PhoenixEssentials.MOD_ID)
public class PhoenixEssentials {

    public static final String MOD_ID = "phoenix_essentials";
    public static final Logger LOGGER = LogManager.getLogger();

    public PhoenixEssentials(IEventBus modBus, ModContainer container) {
        EssentialsCapabilityProvider.register(modBus);
        modBus.addListener(HcChannel::registerAll);

        hcCompileMerged1.mod_construct(modBus, container);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            EssentialsClientProxy.registerConfigScreen(modBus, container);
        }
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
