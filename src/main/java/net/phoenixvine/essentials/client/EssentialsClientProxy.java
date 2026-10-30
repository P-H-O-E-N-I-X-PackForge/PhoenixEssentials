package net.phoenixvine.essentials.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public final class EssentialsClientProxy {

    private EssentialsClientProxy() {}

    public static void registerConfigScreen(IEventBus modBus, ModContainer container) {
        net.phoenixvine.essentials.hc.hcCompileMerged1.client_construct(modBus);
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (mc, screen) -> new EssentialsConfigScreen(screen));
    }
}
