package net.phoenixvine.essentials.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.phoenixvine.essentials.api.EssentialsAPI;
import net.phoenixvine.essentials.capability.EssentialsCapabilityProvider;
import net.phoenixvine.essentials.capability.PlayerEssentialsData;
import net.phoenixvine.essentials.config.EssentialsPermissions;
import net.phoenixvine.essentials.data.KitDefinition;
import net.phoenixvine.essentials.data.KitItemEntry;
import net.phoenixvine.essentials.data.KitRegistry;

import java.util.List;

public final class KitCommand {

    private KitCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("kit")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.KIT))
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(ctx -> claim(ctx.getSource(), StringArgumentType.getString(ctx, "name")))));

        dispatcher.register(Commands.literal("kits")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.KIT))
                .executes(ctx -> list(ctx.getSource())));
    }

    private static int claim(CommandSourceStack source, String name) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        if (!EssentialsAPI.isFeatureEnabled(EssentialsAPI.FEATURE_KITS, player.level().dimension().location())) {
            source.sendFailure(Component.literal("§cKits aren't available here right now."));
            return 0;
        }

        KitDefinition kit = KitRegistry.get(name);
        if (kit == null) {
            source.sendFailure(Component.literal("§cNo kit named \"" + name + "\"."));
            return 0;
        }

        PlayerEssentialsData data = player.getCapability(EssentialsCapabilityProvider.PLAYER_ESSENTIALS).orElse(null);
        if (data == null) return 0;

        long lastClaimedMs = data.getKitLastClaimedMs(name);
        long remainingMs = kit.cooldownSeconds * 1000L - (System.currentTimeMillis() - lastClaimedMs);
        if (lastClaimedMs > 0 && remainingMs > 0) {
            long seconds = (remainingMs + 999) / 1000;
            source.sendFailure(Component.literal("§cYou can claim \"" + name + "\" again in " + seconds + "s."));
            return 0;
        }

        for (KitItemEntry entry : kit.items) {
            ItemStack stack = entry.toStack();
            if (stack.isEmpty()) continue;
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }

        data.recordKitClaimed(name);
        source.sendSuccess(() -> Component.literal("§aClaimed kit \"" + name + "\"."), false);
        return 1;
    }

    private static int list(CommandSourceStack source) {
        List<String> names = KitRegistry.getAll().keySet().stream().sorted().toList();
        if (names.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§7No kits are configured."), false);
        } else {
            source.sendSuccess(() -> Component.literal("§7Kits: §f" + String.join("§7, §f", names)), false);
        }
        return 1;
    }
}
