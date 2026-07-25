package net.phoenixvine.essentials.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Abilities;
import net.phoenixvine.essentials.config.EssentialsPermissions;

public final class GodFlyCommand {

    private GodFlyCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("god")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.GOD))
                .executes(ctx -> god(ctx.getSource(), null))
                .then(Commands.argument("player", StringArgumentType.word())
                        .requires(source -> source.hasPermission(2))
                        .executes(ctx -> god(ctx.getSource(), StringArgumentType.getString(ctx, "player")))));

        dispatcher.register(Commands.literal("fly")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.FLY))
                .executes(ctx -> fly(ctx.getSource(), null))
                .then(Commands.argument("player", StringArgumentType.word())
                        .requires(source -> source.hasPermission(2))
                        .executes(ctx -> fly(ctx.getSource(), StringArgumentType.getString(ctx, "player")))));
    }

    private static int god(CommandSourceStack source, String targetName) {
        ServerPlayer target = resolve(source, targetName);
        if (target == null) return 0;

        boolean nowInvulnerable = !target.getAbilities().invulnerable;
        target.getAbilities().invulnerable = nowInvulnerable;
        target.onUpdateAbilities();

        target.sendSystemMessage(Component.literal("§7God mode " + (nowInvulnerable ? "enabled." : "disabled.")));
        if (targetName != null) {
            source.sendSuccess(() -> Component.literal("§7Set god mode for §f" + target.getGameProfile().getName() +
                    " §7to " + nowInvulnerable + "."), true);
        }
        return 1;
    }

    private static int fly(CommandSourceStack source, String targetName) {
        ServerPlayer target = resolve(source, targetName);
        if (target == null) return 0;

        Abilities abilities = target.getAbilities();
        boolean nowFlying = !abilities.mayfly;
        abilities.mayfly = nowFlying;
        if (!nowFlying) abilities.flying = false;
        target.onUpdateAbilities();

        target.sendSystemMessage(Component.literal("§7Flight " + (nowFlying ? "enabled." : "disabled.")));
        if (targetName != null) {
            source.sendSuccess(() -> Component.literal("§7Set flight for §f" + target.getGameProfile().getName() +
                    " §7to " + nowFlying + "."), true);
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
