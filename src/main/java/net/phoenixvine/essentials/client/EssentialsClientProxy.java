package net.phoenixvine.essentials.client;

import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.phoenixvine.essentials.PhoenixEssentials;

/**
 * All client-only bootstrap lives here so {@link PhoenixEssentials} never references a client class.
 * <p>
 * The class is only ever loaded on the physical client: the {@code value = Dist.CLIENT} subscriber is
 * skipped by Forge before the class is loaded, and {@link #registerConfigScreen()} is only reached
 * through {@code DistExecutor}.
 */
public final class EssentialsClientProxy {

    private EssentialsClientProxy() {}

    public static void registerConfigScreen() {
        // Client listeners (setup event, key registration, key tick) are registered by the HC port.
        net.phoenixvine.essentials.hc.hcCompileMerged1.client_construct();
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, screen) -> new EssentialsConfigScreen(screen)));
    }
}
