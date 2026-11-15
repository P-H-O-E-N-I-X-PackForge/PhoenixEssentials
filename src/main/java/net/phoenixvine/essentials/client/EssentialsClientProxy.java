package net.phoenixvine.essentials.client;

import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.phoenixvine.essentials.PhoenixEssentials;

public final class EssentialsClientProxy {

    private EssentialsClientProxy() {}

    public static void registerConfigScreen() {
        
        net.phoenixvine.essentials.hc.hcCompileMerged1.client_construct();
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, screen) -> new EssentialsConfigScreen(screen)));
    }
}
