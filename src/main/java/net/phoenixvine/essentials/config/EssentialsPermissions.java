package net.phoenixvine.essentials.config;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.phoenixvine.essentials.PhoenixEssentials;
import net.phoenixvine.essentials.hc.hcCompileMerged1;

@EventBusSubscriber(modid = PhoenixEssentials.MOD_ID)
public final class EssentialsPermissions {

    public static PermissionNode<Boolean> HOME;
    public static PermissionNode<Boolean> SETHOME;
    public static PermissionNode<Boolean> DELHOME;
    public static PermissionNode<Boolean> HOMES;
    public static PermissionNode<Boolean> SETHOMELIMIT;
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
    public static PermissionNode<Boolean> WEATHER;

    public static PermissionNode<Boolean> BYPASS_COOLDOWN;
    public static PermissionNode<Boolean> BYPASS_WARMUP;

    private EssentialsPermissions() {}

    @SubscribeEvent
    public static void gather(PermissionGatherEvent.Nodes event) {
        hcCompileMerged1.perm_gather(event);
        HOME = node("HOME");
        SETHOME = node("SETHOME");
        DELHOME = node("DELHOME");
        HOMES = node("HOMES");
        SETHOMELIMIT = node("SETHOMELIMIT");
        SPAWN = node("SPAWN");
        SETSPAWN = node("SETSPAWN");
        WARP = node("WARP");
        SETWARP = node("SETWARP");
        DELWARP = node("DELWARP");
        WARPS = node("WARPS");
        BACK = node("BACK");
        RTP = node("RTP");
        TPA = node("TPA");
        MSG = node("MSG");
        NICK = node("NICK");
        IGNORE = node("IGNORE");
        AFK = node("AFK");
        PLAYERINFO = node("PLAYERINFO");
        SEEN = node("SEEN");
        PLAYTIME = node("PLAYTIME");
        KIT = node("KIT");
        HEAL = node("HEAL");
        FEED = node("FEED");
        INVSEE = node("INVSEE");
        GOD = node("GOD");
        FLY = node("FLY");
        GAMEMODE = node("GAMEMODE");
        TOP = node("TOP");
        TPX = node("TPX");
        TPFORCE = node("TPFORCE");
        WEATHER = node("WEATHER");
        BYPASS_COOLDOWN = node("BYPASS_COOLDOWN");
        BYPASS_WARMUP = node("BYPASS_WARMUP");
    }

    @SuppressWarnings("unchecked")
    private static PermissionNode<Boolean> node(String fieldName) {
        return (PermissionNode<Boolean>) hcCompileMerged1.perm_node(fieldName);
    }

    public static boolean check(CommandSourceStack source, PermissionNode<Boolean> node) {
        if (source.getEntity() instanceof ServerPlayer player) {
            return PermissionAPI.getPermission(player, node);
        }
        return true;
    }
}