package net.phoenixvine.essentials.command;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.CommandEvent;
import net.phoenixvine.essentials.PhoenixEssentials;
import net.phoenixvine.essentials.config.EssentialsPolicy;
import net.phoenixvine.essentials.config.EssentialsServerConfig;

import java.util.Map;

/**
 * Whether a command is on is a setting that tiers can change per player ({@code enableRtp} and the like), so it is checked
 * when the command is run, not only when it is registered.
 */
@Mod.EventBusSubscriber(modid = PhoenixEssentials.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class EssentialsCommandGate {

    private static final Map<String, net.minecraftforge.common.ForgeConfigSpec.BooleanValue> BY_COMMAND = Map.ofEntries(
            Map.entry("home", EssentialsServerConfig.ENABLE_HOME), Map.entry("homes", EssentialsServerConfig.ENABLE_HOME),
            Map.entry("sethome", EssentialsServerConfig.ENABLE_HOME), Map.entry("delhome", EssentialsServerConfig.ENABLE_HOME),
            Map.entry("spawn", EssentialsServerConfig.ENABLE_SPAWN), Map.entry("warp", EssentialsServerConfig.ENABLE_WARP),
            Map.entry("warps", EssentialsServerConfig.ENABLE_WARP), Map.entry("back", EssentialsServerConfig.ENABLE_BACK),
            Map.entry("rtp", EssentialsServerConfig.ENABLE_RTP), Map.entry("tpa", EssentialsServerConfig.ENABLE_TPA),
            Map.entry("tpahere", EssentialsServerConfig.ENABLE_TPA), Map.entry("tpaccept", EssentialsServerConfig.ENABLE_TPA),
            Map.entry("tpdeny", EssentialsServerConfig.ENABLE_TPA), Map.entry("tpcancel", EssentialsServerConfig.ENABLE_TPA),
            Map.entry("nick", EssentialsServerConfig.ENABLE_NICK), Map.entry("ignore", EssentialsServerConfig.ENABLE_IGNORE),
            Map.entry("unignore", EssentialsServerConfig.ENABLE_IGNORE), Map.entry("afk", EssentialsServerConfig.ENABLE_AFK),
            Map.entry("playerinfo", EssentialsServerConfig.ENABLE_PLAYERINFO),
            Map.entry("seen", EssentialsServerConfig.ENABLE_SEEN), Map.entry("playtime", EssentialsServerConfig.ENABLE_PLAYTIME),
            Map.entry("kit", EssentialsServerConfig.ENABLE_KIT), Map.entry("kits", EssentialsServerConfig.ENABLE_KIT),
            Map.entry("heal", EssentialsServerConfig.ENABLE_HEAL_FEED), Map.entry("feed", EssentialsServerConfig.ENABLE_HEAL_FEED),
            Map.entry("invsee", EssentialsServerConfig.ENABLE_INVSEE), Map.entry("god", EssentialsServerConfig.ENABLE_GODFLY),
            Map.entry("fly", EssentialsServerConfig.ENABLE_GODFLY), Map.entry("top", EssentialsServerConfig.ENABLE_TOP),
            Map.entry("tpx", EssentialsServerConfig.ENABLE_TPX), Map.entry("tpforce", EssentialsServerConfig.ENABLE_TPFORCE),
            Map.entry("tphere", EssentialsServerConfig.ENABLE_TPFORCE));

    private EssentialsCommandGate() {}

    @SubscribeEvent
    public static void onCommand(CommandEvent event) {
        var nodes = event.getParseResults().getContext().getNodes();
        if (nodes.isEmpty()) return;
        var setting = BY_COMMAND.get(nodes.get(0).getNode().getName());
        if (setting == null) return;
        if (!(event.getParseResults().getContext().getSource().getEntity() instanceof ServerPlayer player)) return;
        if (!EssentialsPolicy.get(player, setting)) {
            event.setCanceled(true);
            player.sendSystemMessage(Component.literal("§cThat command is not available to you yet."));
        }
    }
}
