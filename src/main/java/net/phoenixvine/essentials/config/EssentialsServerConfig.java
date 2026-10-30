package net.phoenixvine.essentials.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.phoenixvine.essentials.hc.hcCompileMerged1;

import java.util.List;

public class EssentialsServerConfig {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue TELEPORT_WARMUP_SECONDS;
    public static final ModConfigSpec.IntValue TELEPORT_COOLDOWN_SECONDS;
    public static final ModConfigSpec.BooleanValue CANCEL_ON_MOVE;
    public static final ModConfigSpec.BooleanValue CANCEL_ON_DAMAGE;
    public static final ModConfigSpec.IntValue WARP_COOLDOWN_SECONDS;
    public static final ModConfigSpec.IntValue TPX_COOLDOWN_SECONDS;
    public static final ModConfigSpec.IntValue TOP_COOLDOWN_SECONDS;
    public static final ModConfigSpec.IntValue HOMES_PER_PLAYER;
    public static final ModConfigSpec.BooleanValue BACK_AFTER_DEATH;
    public static final ModConfigSpec.IntValue RTP_MIN_RADIUS;
    public static final ModConfigSpec.IntValue RTP_MAX_RADIUS;
    public static final ModConfigSpec.IntValue RTP_MAX_ATTEMPTS;
    public static final ModConfigSpec.IntValue RTP_COOLDOWN_SECONDS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> RTP_ALLOWED_DIMENSIONS;
    public static final ModConfigSpec.IntValue TPA_TIMEOUT_SECONDS;
    public static final ModConfigSpec.IntValue TPA_COOLDOWN_SECONDS;
    public static final ModConfigSpec.BooleanValue TPA_TEAMMATE_BYPASS_DELAY;
    public static final ModConfigSpec.BooleanValue ENABLE_HOME;
    public static final ModConfigSpec.BooleanValue ENABLE_SPAWN;
    public static final ModConfigSpec.BooleanValue ENABLE_WARP;
    public static final ModConfigSpec.BooleanValue ENABLE_BACK;
    public static final ModConfigSpec.BooleanValue ENABLE_RTP;
    public static final ModConfigSpec.BooleanValue ENABLE_TPA;
    public static final ModConfigSpec.BooleanValue ENABLE_MSG;
    public static final ModConfigSpec.BooleanValue ENABLE_NICK;
    public static final ModConfigSpec.BooleanValue ENABLE_IGNORE;
    public static final ModConfigSpec.BooleanValue ENABLE_AFK;
    public static final ModConfigSpec.BooleanValue ENABLE_PLAYERINFO;
    public static final ModConfigSpec.BooleanValue ENABLE_SEEN;
    public static final ModConfigSpec.BooleanValue ENABLE_PLAYTIME;
    public static final ModConfigSpec.BooleanValue ENABLE_KIT;
    public static final ModConfigSpec.BooleanValue ENABLE_HEAL_FEED;
    public static final ModConfigSpec.BooleanValue ENABLE_INVSEE;
    public static final ModConfigSpec.BooleanValue ENABLE_GODFLY;
    public static final ModConfigSpec.BooleanValue ENABLE_GAMEMODE_SHORTCUTS;
    public static final ModConfigSpec.BooleanValue ENABLE_TOP;
    public static final ModConfigSpec.BooleanValue ENABLE_TPX;
    public static final ModConfigSpec.BooleanValue ENABLE_TPFORCE;

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
