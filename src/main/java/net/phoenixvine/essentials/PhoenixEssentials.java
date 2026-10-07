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
        // Everything the constructor and the old @SubscribeEvent methods did (mod/Forge-bus listeners, the
        // server-config registration) lives in the Hot Chocolate port: src/main/hotc/EssentialsModEntry.hotc.
        hcCompileMerged1.mod_construct();

        // Client-only: must stay Java so the client proxy class is never loaded on a dedicated server.
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> EssentialsClientProxy::registerConfigScreen);
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
