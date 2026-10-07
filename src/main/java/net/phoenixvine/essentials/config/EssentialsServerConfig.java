package net.phoenixvine.essentials.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.phoenixvine.essentials.hc.hcCompileMerged1;

import java.util.List;

/**
 * The server config spec. The spec itself (every key, default, range and comment) is built by the Hot
 * Chocolate port in {@code EssentialsServerConfig.hotc}; this class just exposes it and the typed value
 * fields every Java reader uses.
 */
public class EssentialsServerConfig {

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue TELEPORT_WARMUP_SECONDS;
    public static final ForgeConfigSpec.IntValue TELEPORT_COOLDOWN_SECONDS;
    public static final ForgeConfigSpec.BooleanValue CANCEL_ON_MOVE;
    public static final ForgeConfigSpec.BooleanValue CANCEL_ON_DAMAGE;
    public static final ForgeConfigSpec.IntValue WARP_COOLDOWN_SECONDS;
    public static final ForgeConfigSpec.IntValue TPX_COOLDOWN_SECONDS;
    public static final ForgeConfigSpec.IntValue TOP_COOLDOWN_SECONDS;
    public static final ForgeConfigSpec.IntValue HOMES_PER_PLAYER;
    public static final ForgeConfigSpec.BooleanValue BACK_AFTER_DEATH;
    public static final ForgeConfigSpec.IntValue RTP_MIN_RADIUS;
    public static final ForgeConfigSpec.IntValue RTP_MAX_RADIUS;
    public static final ForgeConfigSpec.IntValue RTP_MAX_ATTEMPTS;
    public static final ForgeConfigSpec.IntValue RTP_COOLDOWN_SECONDS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> RTP_ALLOWED_DIMENSIONS;
    public static final ForgeConfigSpec.IntValue TPA_TIMEOUT_SECONDS;
    public static final ForgeConfigSpec.IntValue TPA_COOLDOWN_SECONDS;
    public static final ForgeConfigSpec.BooleanValue TPA_TEAMMATE_BYPASS_DELAY;
    public static final ForgeConfigSpec.BooleanValue ENABLE_HOME;
    public static final ForgeConfigSpec.BooleanValue ENABLE_SPAWN;
    public static final ForgeConfigSpec.BooleanValue ENABLE_WARP;
    public static final ForgeConfigSpec.BooleanValue ENABLE_BACK;
    public static final ForgeConfigSpec.BooleanValue ENABLE_RTP;
    public static final ForgeConfigSpec.BooleanValue ENABLE_TPA;
    public static final ForgeConfigSpec.BooleanValue ENABLE_MSG;
    public static final ForgeConfigSpec.BooleanValue ENABLE_NICK;
    public static final ForgeConfigSpec.BooleanValue ENABLE_IGNORE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_AFK;
    public static final ForgeConfigSpec.BooleanValue ENABLE_PLAYERINFO;
    public static final ForgeConfigSpec.BooleanValue ENABLE_SEEN;
    public static final ForgeConfigSpec.BooleanValue ENABLE_PLAYTIME;
    public static final ForgeConfigSpec.BooleanValue ENABLE_KIT;
    public static final ForgeConfigSpec.BooleanValue ENABLE_HEAL_FEED;
    public static final ForgeConfigSpec.BooleanValue ENABLE_INVSEE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_GODFLY;
    public static final ForgeConfigSpec.BooleanValue ENABLE_GAMEMODE_SHORTCUTS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_TOP;
    public static final ForgeConfigSpec.BooleanValue ENABLE_TPX;
    public static final ForgeConfigSpec.BooleanValue ENABLE_TPFORCE;

    static {
        SPEC = hcCompileMerged1.cfg_spec();

        TELEPORT_WARMUP_SECONDS = value("teleportWarmupSeconds");
        TELEPORT_COOLDOWN_SECONDS = value("teleportCooldownSeconds");
        CANCEL_ON_MOVE = value("cancelOnMove");
        CANCEL_ON_DAMAGE = value("cancelOnDamage");
        WARP_COOLDOWN_SECONDS = value("warpCooldownSeconds");
        TPX_COOLDOWN_SECONDS = value("tpxCooldownSeconds");
        TOP_COOLDOWN_SECONDS = value("topCooldownSeconds");
        HOMES_PER_PLAYER = value("homesPerPlayer");
        BACK_AFTER_DEATH = value("backAfterDeath");
        RTP_MIN_RADIUS = value("rtpMinRadius");
        RTP_MAX_RADIUS = value("rtpMaxRadius");
        RTP_MAX_ATTEMPTS = value("rtpMaxAttempts");
        RTP_COOLDOWN_SECONDS = value("rtpCooldownSeconds");
        RTP_ALLOWED_DIMENSIONS = value("rtpAllowedDimensions");
        TPA_TIMEOUT_SECONDS = value("tpaTimeoutSeconds");
        TPA_COOLDOWN_SECONDS = value("tpaCooldownSeconds");
        TPA_TEAMMATE_BYPASS_DELAY = value("tpaTeammateBypassDelay");
        ENABLE_HOME = value("enableHome");
        ENABLE_SPAWN = value("enableSpawn");
        ENABLE_WARP = value("enableWarp");
        ENABLE_BACK = value("enableBack");
        ENABLE_RTP = value("enableRtp");
        ENABLE_TPA = value("enableTpa");
        ENABLE_MSG = value("enableMsg");
        ENABLE_NICK = value("enableNick");
        ENABLE_IGNORE = value("enableIgnore");
        ENABLE_AFK = value("enableAfk");
        ENABLE_PLAYERINFO = value("enablePlayerInfo");
        ENABLE_SEEN = value("enableSeen");
        ENABLE_PLAYTIME = value("enablePlaytime");
        ENABLE_KIT = value("enableKit");
        ENABLE_HEAL_FEED = value("enableHealFeed");
        ENABLE_INVSEE = value("enableInvsee");
        ENABLE_GODFLY = value("enableGodFly");
        ENABLE_GAMEMODE_SHORTCUTS = value("enableGamemodeShortcuts");
        ENABLE_TOP = value("enableTop");
        ENABLE_TPX = value("enableTpx");
        ENABLE_TPFORCE = value("enableTpforce");
    }

    @SuppressWarnings("unchecked")
    private static <T> T value(String key) {
        return (T) hcCompileMerged1.cfg_value(key);
    }

    private EssentialsServerConfig() {}
}
