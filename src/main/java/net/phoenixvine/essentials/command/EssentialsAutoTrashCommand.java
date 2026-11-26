package net.phoenixvine.essentials.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.phoenixvine.essentials.capability.EssentialsCapabilityProvider;
import net.phoenixvine.essentials.capability.PlayerEssentialsData;

public final class EssentialsAutoTrashCommand {

    private EssentialsAutoTrashCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("essentialsautotrash")
                .requires(source -> net.phoenixvine.essentials.config.EssentialsPermissions.check(source,
                        net.phoenixvine.essentials.config.EssentialsPermissions.AUTOTRASH))
                .executes(ctx -> toggleHeld(ctx, false))
                .then(Commands.literal("confirm").executes(ctx -> toggleHeld(ctx, true)))
                .then(Commands.literal("remove")
                        .then(Commands.argument("item", ResourceLocationArgument.id())
                                .executes(EssentialsAutoTrashCommand::removeItem))));
    }

    private static int toggleHeld(CommandContext<CommandSourceStack> ctx, boolean confirmed) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            player.sendSystemMessage(Component.literal("§cHold an item first."));
            return 0;
        }

        ResourceLocation id = ForgeRegistries.ITEMS.getKey(held.getItem());
        if (id == null) return 0;

        PlayerEssentialsData data = player.getCapability(EssentialsCapabilityProvider.PLAYER_ESSENTIALS)
                .orElse(null);
        if (data == null) return 0;

        if (data.isAlwaysTrash(id)) {
            data.removeAlwaysTrash(id);
            player.sendSystemMessage(Component.literal("§7No longer auto-trashing \"" + id + "\"."));
            return 1;
        }
        int have = net.phoenixvine.essentials.capability.EssentialsAutoTrashEvents.countTrashable(player, id);
        if (have > 0 && !confirmed) {
            player.sendSystemMessage(Component.literal("§eAuto-trash deletes the " + have + " \"" + id +
                    "\" you carry now, and every one you pick up. Items with data on them (enchanted, named, " +
                    "configured) are kept. Run §f/essentialsautotrash confirm§e to turn it on."));
            return 0;
        }
        data.toggleAlwaysTrash(id);
        player.sendSystemMessage(Component.literal("§aWill now auto-trash \"" + id + "\"."));
        return 1;
    }

    private static int removeItem(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        ResourceLocation id = ResourceLocationArgument.getId(ctx, "item");
        PlayerEssentialsData data = player.getCapability(EssentialsCapabilityProvider.PLAYER_ESSENTIALS)
                .orElse(null);
        if (data == null) return 0;

        data.removeAlwaysTrash(id);
        player.sendSystemMessage(Component.literal("§7No longer auto-trashing \"" + id + "\"."));
        return 1;
    }
}
