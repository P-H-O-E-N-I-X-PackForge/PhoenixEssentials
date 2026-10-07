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
                .executes(EssentialsAutoTrashCommand::toggleHeld)
                .then(Commands.literal("remove")
                        .then(Commands.argument("item", ResourceLocationArgument.id())
                                .executes(EssentialsAutoTrashCommand::removeItem))));
    }

    private static int toggleHeld(CommandContext<CommandSourceStack> ctx) {
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

        boolean nowTrashed = data.toggleAlwaysTrash(id);
        player.sendSystemMessage(Component.literal(nowTrashed
                ? "§aWill now auto-trash \"" + id + "\" on pickup."
                : "§7No longer auto-trashing \"" + id + "\"."));
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
