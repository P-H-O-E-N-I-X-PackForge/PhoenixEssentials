package net.phoenixvine.essentials.command;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;

public final class EssentialsTrashCommand {

    private static final int SLOTS = 36;
    private static final int ROWS = SLOTS / 9;

    private EssentialsTrashCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("essentialstrash")
                .executes(EssentialsTrashCommand::open));
    }

    private static int open(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        SimpleContainer voidContainer = new SimpleContainer(SLOTS);
        player.openMenu(new SimpleMenuProvider(
                (id, playerInv, p) -> new ChestMenu(MenuType.GENERIC_9x4, id, playerInv, voidContainer, ROWS),
                Component.literal("Trash")));
        return 1;
    }
}
