package net.phoenixvine.essentials.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.phoenixvine.essentials.config.EssentialsPermissions;

public final class HealFeedCommand {

    private HealFeedCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("heal")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.HEAL))
                .executes(ctx -> heal(ctx.getSource(), null))
                .then(Commands.argument("player", StringArgumentType.word())
                        .requires(source -> source.hasPermission(2))
                        .executes(ctx -> heal(ctx.getSource(), StringArgumentType.getString(ctx, "player")))));

        dispatcher.register(Commands.literal("feed")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.FEED))
                .executes(ctx -> feed(ctx.getSource(), null))
                .then(Commands.argument("player", StringArgumentType.word())
                        .requires(source -> source.hasPermission(2))
                        .executes(ctx -> feed(ctx.getSource(), StringArgumentType.getString(ctx, "player")))));
    }

    private static int heal(CommandSourceStack source, String targetName) {
        ServerPlayer target = resolve(source, targetName);
        if (target == null) return 0;

        target.setHealth(target.getMaxHealth());
        target.getFoodData().setFoodLevel(20);
        target.getFoodData().setSaturation(20f);
        target.clearFire();
        target.removeAllEffects();

        target.sendSystemMessage(Component.literal("§aYou have been healed."));
        if (targetName != null) {
            source.sendSuccess(() -> Component.literal("§aHealed §f" + target.getGameProfile().getName() + "§a."), true);
        }
        return 1;
    }

    private static int feed(CommandSourceStack source, String targetName) {
        ServerPlayer target = resolve(source, targetName);
        if (target == null) return 0;

        target.getFoodData().setFoodLevel(20);
        target.getFoodData().setSaturation(20f);

        target.sendSystemMessage(Component.literal("§aYou have been fed."));
        if (targetName != null) {
            source.sendSuccess(() -> Component.literal("§aFed §f" + target.getGameProfile().getName() + "§a."), true);
        }
        return 1;
    }

    private static ServerPlayer resolve(CommandSourceStack source, String targetName) {
        if (targetName == null) return source.getPlayer();

        ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(targetName);
        if (target == null) {
            source.sendFailure(Component.literal("§cPlayer \"" + targetName + "\" isn't online."));
        }
        return target;
    }
}
