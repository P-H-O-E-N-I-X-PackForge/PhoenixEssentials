package net.phoenixvine.essentials.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class EssentialsServerConfig {

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue TELEPORT_WARMUP_SECONDS;
    public static final ForgeConfigSpec.IntValue TELEPORT_COOLDOWN_SECONDS;
    public static final ForgeConfigSpec.BooleanValue CANCEL_ON_MOVE;
    public static final ForgeConfigSpec.BooleanValue CANCEL_ON_DAMAGE;

    public static final ForgeConfigSpec.IntValue HOMES_PER_PLAYER;
    public static final ForgeConfigSpec.BooleanValue BACK_AFTER_DEATH;

    public static final ForgeConfigSpec.IntValue RTP_MIN_RADIUS;
    public static final ForgeConfigSpec.IntValue RTP_MAX_RADIUS;
    public static final ForgeConfigSpec.IntValue RTP_MAX_ATTEMPTS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> RTP_ALLOWED_DIMENSIONS;

    public static final ForgeConfigSpec.IntValue TPA_TIMEOUT_SECONDS;

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
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("teleport");

        TELEPORT_WARMUP_SECONDS = builder
                .comment("Seconds a player must stand still before a home/spawn/warp/rtp teleport fires. 0 = instant.")
                .defineInRange("teleportWarmupSeconds", 3, 0, 300);

        TELEPORT_COOLDOWN_SECONDS = builder
                .comment("Seconds a player must wait between teleports of the same kind (home/spawn/warp/rtp each " +
                        "have their own independent cooldown timer). 0 = no cooldown.")
                .defineInRange("teleportCooldownSeconds", 5, 0, 3600);

        CANCEL_ON_MOVE = builder
                .comment("If true, moving during the warmup countdown cancels the teleport.")
                .define("cancelOnMove", true);

        CANCEL_ON_DAMAGE = builder
                .comment("If true, taking damage during the warmup countdown cancels the teleport.")
                .define("cancelOnDamage", true);

        builder.pop();
        builder.push("homes");

        HOMES_PER_PLAYER = builder
                .comment("Maximum number of named homes a single player may set.")
                .defineInRange("homesPerPlayer", 3, 1, 256);

        BACK_AFTER_DEATH = builder
                .comment("If true, /back after dying returns the player to their death location. If false, /back " +
                        "only ever returns to the point before the last teleport, ignoring deaths.")
                .define("backAfterDeath", true);

        builder.pop();
        builder.push("rtp");

        RTP_MIN_RADIUS = builder
                .comment("Minimum distance (blocks) from the origin a /rtp destination may land.")
                .defineInRange("rtpMinRadius", 100, 0, 100_000);

        RTP_MAX_RADIUS = builder
                .comment("Maximum distance (blocks) from the origin a /rtp destination may land.")
                .defineInRange("rtpMaxRadius", 5000, 1, 1_000_000);

        RTP_MAX_ATTEMPTS = builder
                .comment("How many random locations to try before giving up and telling the player to try again.")
                .defineInRange("rtpMaxAttempts", 20, 1, 200);

        RTP_ALLOWED_DIMENSIONS = builder
                .comment("Dimension ids /rtp is allowed in (e.g. \"minecraft:overworld\"). Empty list = allowed " +
                        "everywhere.")
                .defineList("rtpAllowedDimensions", new ArrayList<String>(), o -> o instanceof String);

        builder.pop();
        builder.push("social");

        TPA_TIMEOUT_SECONDS = builder
                .comment("Seconds a /tpa or /tpahere request stays valid before it expires.")
                .defineInRange("tpaTimeoutSeconds", 60, 5, 3600);

        builder.pop();
        builder.push("enabledCommands");

        ENABLE_HOME = builder.comment("If false, disables /home, /sethome, /delhome, /homes entirely.")
                .define("enableHome", true);
        ENABLE_SPAWN = builder.comment("If false, disables /spawn and /setspawn entirely.")
                .define("enableSpawn", true);
        ENABLE_WARP = builder.comment("If false, disables /warp, /setwarp, /delwarp, /warps entirely.")
                .define("enableWarp", true);
        ENABLE_BACK = builder.comment("If false, disables /back entirely.")
                .define("enableBack", true);
        ENABLE_RTP = builder.comment("If false, disables /rtp entirely.")
                .define("enableRtp", true);
        ENABLE_TPA = builder.comment("If false, disables /tpa, /tpahere, /tpaccept, /tpdeny, /tpcancel entirely.")
                .define("enableTpa", true);
        ENABLE_MSG = builder.comment("If false, disables /msg, /tell, /r, /reply entirely.")
                .define("enableMsg", true);
        ENABLE_NICK = builder.comment("If false, disables /nick entirely.")
                .define("enableNick", true);
        ENABLE_IGNORE = builder.comment("If false, disables /ignore and /unignore entirely.")
                .define("enableIgnore", true);
        ENABLE_AFK = builder.comment("If false, disables /afk entirely.")
                .define("enableAfk", true);
        ENABLE_PLAYERINFO = builder.comment("If false, disables /playerinfo entirely.")
                .define("enablePlayerInfo", true);
        ENABLE_SEEN = builder.comment("If false, disables /seen entirely.")
                .define("enableSeen", true);
        ENABLE_PLAYTIME = builder.comment("If false, disables /playtime entirely.")
                .define("enablePlaytime", true);
        ENABLE_KIT = builder.comment("If false, disables /kit and /kits entirely.")
                .define("enableKit", true);
        ENABLE_HEAL_FEED = builder.comment("If false, disables /heal and /feed entirely.")
                .define("enableHealFeed", true);
        ENABLE_INVSEE = builder.comment("If false, disables /invsee entirely.")
                .define("enableInvsee", true);
        ENABLE_GODFLY = builder.comment("If false, disables /god and /fly entirely.")
                .define("enableGodFly", true);
        ENABLE_GAMEMODE_SHORTCUTS = builder.comment("If false, disables /gmc, /gms, /gma, /gmsp entirely.")
                .define("enableGamemodeShortcuts", true);
        ENABLE_TOP = builder.comment("If false, disables /top entirely.")
                .define("enableTop", true);
        ENABLE_TPX = builder.comment("If false, disables /tpx entirely.")
                .define("enableTpx", true);
        ENABLE_TPFORCE = builder.comment("If false, disables /tpforce and /tphere entirely.")
                .define("enableTpforce", true);

        builder.pop();
        SPEC = builder.build();
    }

    private EssentialsServerConfig() {}
}
