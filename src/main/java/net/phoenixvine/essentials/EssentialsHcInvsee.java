package net.phoenixvine.essentials;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = PhoenixEssentials.MOD_ID)
public final class EssentialsHcInvsee {

    private record Session(UUID targetUuid, SimpleContainer container) {}

    private static final Map<UUID, Session> OPEN = new ConcurrentHashMap<>();

    private EssentialsHcInvsee() {}

    public static String open(ServerPlayer viewer, ServerPlayer target) {
        if (target == viewer) return "You can't /hcinvsee yourself - just open your own inventory.";

        SimpleContainer snapshot = new SimpleContainer(36);
        for (int i = 0; i < 36; i++) {
            snapshot.setItem(i, target.getInventory().getItem(i).copy());
        }
        OPEN.put(viewer.getUUID(), new Session(target.getUUID(), snapshot));

        viewer.openMenu(new SimpleMenuProvider(
                (id, playerInv, p) -> new ChestMenu(MenuType.GENERIC_9x4, id, playerInv, snapshot, 4),
                Component.literal(target.getGameProfile().getName() + "'s Inventory")));
        return null;
    }

    @SubscribeEvent
    public static void onContainerClose(PlayerContainerEvent.Close event) {
        if (!(event.getEntity() instanceof ServerPlayer viewer)) return;

        Session session = OPEN.remove(viewer.getUUID());
        if (session == null) return;

        ServerPlayer target = viewer.getServer() == null ? null
                : viewer.getServer().getPlayerList().getPlayer(session.targetUuid());
        if (target == null) return;

        for (int i = 0; i < 36; i++) {
            target.getInventory().setItem(i, session.container().getItem(i).copy());
        }
    }
}
