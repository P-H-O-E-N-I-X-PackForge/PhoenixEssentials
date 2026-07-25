package net.phoenixvine.essentials.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.phoenixvine.essentials.PhoenixEssentials;
import net.phoenixvine.essentials.config.EssentialsPermissions;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = PhoenixEssentials.MOD_ID)
public final class InvseeCommand {

    private record Session(UUID targetUuid, SimpleContainer container) {}

    private static final Map<UUID, Session> OPEN = new ConcurrentHashMap<>();

    private InvseeCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("invsee")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.INVSEE))
                .then(Commands.argument("player", StringArgumentType.word())
                        .executes(ctx -> open(ctx.getSource(), StringArgumentType.getString(ctx, "player")))));
    }

    private static int open(CommandSourceStack source, String targetName) {
        ServerPlayer viewer = source.getPlayer();
        if (viewer == null) return 0;

        ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(targetName);
        if (target == null) {
            source.sendFailure(Component.literal("§cPlayer \"" + targetName + "\" isn't online."));
            return 0;
        }
        if (target == viewer) {
            source.sendFailure(Component.literal("§cYou can't /invsee yourself - just open your own inventory."));
            return 0;
        }

        SimpleContainer snapshot = new SimpleContainer(36);
        for (int i = 0; i < 36; i++) {
            snapshot.setItem(i, target.getInventory().getItem(i).copy());
        }

        OPEN.put(viewer.getUUID(), new Session(target.getUUID(), snapshot));

        viewer.openMenu(new SimpleMenuProvider(
                (id, playerInv, p) -> new ChestMenu(MenuType.GENERIC_9x4, id, playerInv, snapshot, 4),
                Component.literal(target.getGameProfile().getName() + "'s Inventory")));
        return 1;
    }

    @SubscribeEvent
    public static void onContainerClose(PlayerContainerEvent.Close event) {
        if (!(event.getEntity() instanceof ServerPlayer viewer)) return;

        Session session = OPEN.remove(viewer.getUUID());
        if (session == null) return;

        ServerPlayer target = viewer.getServer() == null ? null :
                viewer.getServer().getPlayerList().getPlayer(session.targetUuid());
        if (target == null) return; 

        for (int i = 0; i < 36; i++) {
            target.getInventory().setItem(i, session.container().getItem(i).copy());
        }
    }
}
