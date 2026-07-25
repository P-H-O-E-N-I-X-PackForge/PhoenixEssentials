package net.phoenixvine.essentials.command;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.phoenixvine.essentials.config.EssentialsPermissions;
import net.phoenixvine.essentials.data.NamedLocation;
import net.phoenixvine.essentials.data.SpawnRegistry;

public final class SpawnCommand {

    private SpawnCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("spawn")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.SPAWN))
                .executes(ctx -> teleportSpawn(ctx.getSource())));

        dispatcher.register(Commands.literal("setspawn")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.SETSPAWN))
                .executes(ctx -> setSpawn(ctx.getSource())));
    }

    private static int teleportSpawn(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        ServerLevel overworld = source.getServer().overworld();
        NamedLocation spawn = SpawnRegistry.getOrDefault(overworld);
        TeleportExecutor.request(player, spawn, "spawn", "spawn");
        return 1;
    }

    private static int setSpawn(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        SpawnRegistry.set(NamedLocation.of(player));
        source.sendSuccess(() -> Component.literal("§aServer spawn set to your current location."), true);
        return 1;
    }
}
