package net.phoenixvine.essentials.config;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionTypes;
import net.phoenixvine.essentials.PhoenixEssentials;

@Mod.EventBusSubscriber(modid = PhoenixEssentials.MOD_ID)
public final class EssentialsPermissions {

    public static PermissionNode<Boolean> HOME;
    public static PermissionNode<Boolean> SETHOME;
    public static PermissionNode<Boolean> DELHOME;
    public static PermissionNode<Boolean> HOMES;
    public static PermissionNode<Boolean> SPAWN;
    public static PermissionNode<Boolean> SETSPAWN;
    public static PermissionNode<Boolean> WARP;
    public static PermissionNode<Boolean> SETWARP;
    public static PermissionNode<Boolean> DELWARP;
    public static PermissionNode<Boolean> WARPS;
    public static PermissionNode<Boolean> BACK;
    public static PermissionNode<Boolean> RTP;
    public static PermissionNode<Boolean> TPA;
    public static PermissionNode<Boolean> MSG;
    public static PermissionNode<Boolean> NICK;
    public static PermissionNode<Boolean> IGNORE;
    public static PermissionNode<Boolean> AFK;
    public static PermissionNode<Boolean> PLAYERINFO;
    public static PermissionNode<Boolean> SEEN;
    public static PermissionNode<Boolean> PLAYTIME;
    public static PermissionNode<Boolean> KIT;
    public static PermissionNode<Boolean> HEAL;
    public static PermissionNode<Boolean> FEED;
    public static PermissionNode<Boolean> INVSEE;
    public static PermissionNode<Boolean> GOD;
    public static PermissionNode<Boolean> FLY;
    public static PermissionNode<Boolean> GAMEMODE;
    public static PermissionNode<Boolean> TOP;
    public static PermissionNode<Boolean> TPX;
    public static PermissionNode<Boolean> TPFORCE;

    private EssentialsPermissions() {}

    @SubscribeEvent
    public static void gather(PermissionGatherEvent.Nodes event) {
        HOME = allByDefault("home");
        SETHOME = allByDefault("sethome");
        DELHOME = allByDefault("delhome");
        HOMES = allByDefault("homes");
        SPAWN = allByDefault("spawn");
        SETSPAWN = opByDefault("setspawn");
        WARP = allByDefault("warp");
        SETWARP = opByDefault("setwarp");
        DELWARP = opByDefault("delwarp");
        WARPS = allByDefault("warps");
        BACK = allByDefault("back");
        RTP = allByDefault("rtp");
        TPA = allByDefault("tpa");
        MSG = allByDefault("msg");
        NICK = allByDefault("nick");
        IGNORE = allByDefault("ignore");
        AFK = allByDefault("afk");
        PLAYERINFO = allByDefault("playerinfo");
        SEEN = allByDefault("seen");
        PLAYTIME = allByDefault("playtime");
        KIT = allByDefault("kit");
        HEAL = allByDefault("heal");
        FEED = allByDefault("feed");

        GOD = allByDefault("god");
        FLY = allByDefault("fly");
        GAMEMODE = allByDefault("gamemode");
        TOP = allByDefault("top");

        INVSEE = opByDefault("invsee");

        TPX = opByDefault("tpx");
        TPFORCE = opByDefault("tpforce");

        event.addNodes(HOME, SETHOME, DELHOME, HOMES, SPAWN, SETSPAWN, WARP, SETWARP, DELWARP, WARPS, BACK, RTP,
                TPA, MSG, NICK, IGNORE, AFK, PLAYERINFO, SEEN, PLAYTIME, KIT, HEAL, FEED, INVSEE, GOD, FLY,
                GAMEMODE, TOP, TPX, TPFORCE);
    }

    private static PermissionNode<Boolean> allByDefault(String path) {
        return new PermissionNode<>(PhoenixEssentials.MOD_ID, path, PermissionTypes.BOOLEAN,
                (player, uuid, context) -> true);
    }

    private static PermissionNode<Boolean> opByDefault(String path) {
        return new PermissionNode<>(PhoenixEssentials.MOD_ID, path, PermissionTypes.BOOLEAN,
                (player, uuid, context) -> player != null && player.hasPermissions(2));
    }

    public static boolean check(CommandSourceStack source, PermissionNode<Boolean> node) {
        if (source.getEntity() instanceof ServerPlayer player) {
            return PermissionAPI.getPermission(player, node);
        }
        return true;
    }
}
