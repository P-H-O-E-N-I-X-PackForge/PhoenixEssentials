package net.phoenixvine.essentials.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.phoenixvine.essentials.api.EssentialsAPI;
import net.phoenixvine.essentials.capability.EssentialsCapabilityProvider;
import net.phoenixvine.essentials.capability.PlayerEssentialsData;
import net.phoenixvine.essentials.config.EssentialsPermissions;

public final class NickCommand {

    private NickCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nick")
                .requires(source -> EssentialsPermissions.check(source, EssentialsPermissions.NICK))
                .executes(ctx -> clearNick(ctx.getSource()))
                .then(Commands.argument("nickname", StringArgumentType.word())
                        .executes(ctx -> setNick(ctx.getSource(), StringArgumentType.getString(ctx, "nickname")))));
    }

    private static int setNick(CommandSourceStack source, String nickname) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        if (!EssentialsAPI.isFeatureEnabled(EssentialsAPI.FEATURE_SOCIAL, player.level().dimension().location())) {
            source.sendFailure(Component.literal("§cSocial commands aren't available here right now."));
            return 0;
        }

        if (nickname.length() > 24) {
            source.sendFailure(Component.literal("§cNicknames can be at most 24 characters."));
            return 0;
        }

        PlayerEssentialsData data = data(player);
        if (data == null) return 0;

        data.setNickname(nickname);
        source.sendSuccess(() -> Component.literal("§aNickname set to \"" + nickname + "\"."), false);
        return 1;
    }

    private static int clearNick(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        if (!EssentialsAPI.isFeatureEnabled(EssentialsAPI.FEATURE_SOCIAL, player.level().dimension().location())) {
            source.sendFailure(Component.literal("§cSocial commands aren't available here right now."));
            return 0;
        }

        PlayerEssentialsData data = data(player);
        if (data == null) return 0;

        data.setNickname("");
        source.sendSuccess(() -> Component.literal("§aNickname cleared."), false);
        return 1;
    }

    private static PlayerEssentialsData data(ServerPlayer player) {
        return player.getCapability(EssentialsCapabilityProvider.PLAYER_ESSENTIALS).orElse(null);
    }
}
