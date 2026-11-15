package net.phoenixvine.essentials;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.phoenixvine.essentials.client.EssentialsClientProxy;
import net.phoenixvine.essentials.hc.hcCompileMerged1;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(PhoenixEssentials.MOD_ID)
@SuppressWarnings("removal")
public class PhoenixEssentials {

    public static final String MOD_ID = "phoenix_essentials";
    public static final Logger LOGGER = LogManager.getLogger();

    public PhoenixEssentials() {

        hcCompileMerged1.mod_construct();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> EssentialsClientProxy::registerConfigScreen);
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
