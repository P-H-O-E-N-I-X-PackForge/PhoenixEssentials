package net.phoenixvine.essentials;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.phoenixvine.essentials.command.TeleportExecutor;
import net.phoenixvine.essentials.config.EssentialsServerConfig;
import net.phoenixvine.essentials.data.NamedLocation;
import net.phoenixvine.essentials.data.OfflinePlayerIndex;
import net.phoenixvine.essentials.team.TeamCompat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.phoenixvine.essentials.capability.EssentialsCapabilityProvider;
import net.phoenixvine.essentials.capability.PlayerEssentialsData;
import net.phoenixvine.essentials.data.KitDefinition;
import net.phoenixvine.essentials.data.KitItemEntry;
import net.phoenixvine.essentials.data.KitRegistry;
import net.phoenixvine.essentials.data.WarpRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Tiny, disclosed shim for the hotc port of HomeCommand (src/main/hotc/EssentialsHomeCommands.hotc)
// -- hotc-mc has no java.util.Map/Set iteration binding, and this is the only server-side caller
// that needs one (listing PlayerEssentialsData's own home-name key set, sorted, for /hchomes).
// Top-level package (not .client/.command) so it loads fine on a dedicated server too.
public final class EssentialsHcServerBridge {
    private EssentialsHcServerBridge() {}

    // Real /msg + /reply round trip -- consolidates MessageCommand.java's own ignoring-check,
    // the two sendSystemMessage calls, and the UUID->UUID "last messaged" bookkeeping into one
    // real call each, rather than binding java.util.UUID/Map individually for this one real
    // caller. Logic is a direct copy of MessageCommand.java's own send()/reply(), not a
    // reimplementation.
    private static final Map<UUID, UUID> LAST_MESSAGED = new ConcurrentHashMap<>();

    // Returns null on success, or a real error message to show via sendFailure.
    public static String sendDirectMessage(ServerPlayer sender, ServerPlayer target, String message) {
        if (target == sender) return "You can't message yourself.";
        PlayerEssentialsData targetData = target.getCapability(EssentialsCapabilityProvider.PLAYER_ESSENTIALS).orElse(null);
        if (targetData != null && targetData.isIgnoring(sender.getUUID())) {
            return "That player isn't receiving messages right now.";
        }
        sender.sendSystemMessage(Component.literal("§7[me -> " + target.getName().getString() + "] §f" + message));
        target.sendSystemMessage(Component.literal("§7[" + sender.getName().getString() + " -> me] §f" + message));
        LAST_MESSAGED.put(sender.getUUID(), target.getUUID());
        LAST_MESSAGED.put(target.getUUID(), sender.getUUID());
        return null;
    }

    // Real, nullable -- the real login name of whoever `player` last messaged (either direction),
    // or null if there's no one (or they've gone offline since).
    public static String getLastMessagedName(MinecraftServer server, ServerPlayer player) {
        UUID targetUuid = LAST_MESSAGED.get(player.getUUID());
        if (targetUuid == null) return null;
        ServerPlayer target = server.getPlayerList().getPlayer(targetUuid);
        if (target == null) return null;
        return target.getGameProfile().getName();
    }

    public static String homeNamesCsv(PlayerEssentialsData data) {
        List<String> names = new ArrayList<>(data.getHomes().keySet());
        names.sort(String::compareTo);
        return String.join(",", names);
    }

    public static int homeCount(PlayerEssentialsData data) {
        return data.getHomes().size();
    }

    // Same real need as `homeNamesCsv` above, for the server-wide (not per-player) `WarpRegistry`.
    public static String warpNamesCsv() {
        List<String> names = new ArrayList<>(WarpRegistry.getAll().keySet());
        names.sort(String::compareTo);
        return String.join(",", names);
    }

    // Kit claiming -- consolidates KitCommand.java's own cooldown math + item-granting loop into
    // one real call each, rather than binding java.util.List<KitItemEntry> iteration,
    // KitDefinition's own fields, Inventory.add, and System.currentTimeMillis() individually for
    // a single real caller (same "tiny shim over a real gap" precedent as this class's other
    // methods). Logic is a direct copy of KitCommand.java's own claim()/cooldown check, not a
    // reimplementation.
    public static String kitNamesCsv() {
        List<String> names = new ArrayList<>(KitRegistry.getAll().keySet());
        names.sort(String::compareTo);
        return String.join(",", names);
    }

    public static boolean kitExists(String name) {
        return KitRegistry.get(name) != null;
    }

    public static int kitCooldownRemainingSeconds(PlayerEssentialsData data, String name) {
        KitDefinition kit = KitRegistry.get(name);
        if (kit == null) return 0;
        long lastClaimedMs = data.getKitLastClaimedMs(name);
        if (lastClaimedMs <= 0) return 0;
        long remainingMs = kit.cooldownSeconds * 1000L - (System.currentTimeMillis() - lastClaimedMs);
        if (remainingMs <= 0) return 0;
        return (int) ((remainingMs + 999) / 1000);
    }

    // Direct copy of PlaytimeCommand.java's own format() -- avoids needing a java.lang.Long
    // binding in HC for this one real caller (/hcplaytime).
    public static String playtimeFormatted(PlayerEssentialsData data) {
        long ticks = data.getPlaytimeTicks();
        long totalSeconds = ticks / 20;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();
        if (hours > 0) sb.append(hours).append("h ");
        if (hours > 0 || minutes > 0) sb.append(minutes).append("m ");
        sb.append(seconds).append("s");
        return sb.toString();
    }

    // Real /ignore + /unignore -- consolidates IgnoreCommand.java's own Set<UUID> add/remove into
    // one real call, rather than binding java.util.UUID/Set individually for this one real caller.
    public static void setIgnored(PlayerEssentialsData data, ServerPlayer target, boolean ignore) {
        if (ignore) {
            data.getIgnored().add(target.getUUID());
        } else {
            data.getIgnored().remove(target.getUUID());
        }
    }

    // Direct copy of TopCommand.java's own target computation (surface Y via the MOTION_BLOCKING_NO_LEAVES
    // heightmap) -- avoids binding Heightmap/ResourceKey/NamedLocation's 6-arg ctor for one real
    // caller. Returns null if the player isn't in a ServerLevel.
    public static NamedLocation topLocation(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return null;
        int x = player.getBlockX();
        int z = player.getBlockZ();
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        return new NamedLocation(level.dimension(), x + 0.5, y, z + 0.5, player.getYRot(), player.getXRot());
    }

    // Direct copy of TpxCommand.java's own target computation (same X/Z, surface Y in the target
    // dimension). Returns null if the player is already in that dimension.
    // Deliberate IMPROVEMENT over the original, not a pure copy: the original calls getHeight on a
    // chunk that may not be loaded in the target dimension, which returns the world floor and drops
    // the player into the void (observed in the hc port). This forces the chunk to load first, and
    // for ceilinged dimensions (the Nether) scans down from the roof for a solid block with two air
    // blocks above instead of landing on the bedrock ceiling.
    public static NamedLocation tpxLocation(ServerPlayer player, ServerLevel targetLevel) {
        if (player.level() == targetLevel) return null;
        int x = player.getBlockX();
        int z = player.getBlockZ();
        targetLevel.getChunk(x >> 4, z >> 4); // force load/generate
        int y;
        if (targetLevel.dimensionType().hasCeiling()) {
            y = targetLevel.getMinBuildHeight() + 1;
            net.minecraft.core.BlockPos.MutableBlockPos pos = new net.minecraft.core.BlockPos.MutableBlockPos();
            for (int cy = targetLevel.getMinBuildHeight() + targetLevel.dimensionType().logicalHeight() - 2;
                    cy > targetLevel.getMinBuildHeight() + 1; cy--) {
                pos.set(x, cy, z);
                if (targetLevel.getBlockState(pos).isAir()) continue;
                if (targetLevel.getBlockState(pos.above()).isAir() && targetLevel.getBlockState(pos.above(2)).isAir()) {
                    y = cy + 1;
                    break;
                }
            }
        } else {
            y = targetLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        }
        return new NamedLocation(targetLevel.dimension(), x + 0.5, y, z + 0.5, player.getYRot(), player.getXRot());
    }

    // Direct copy of RtpCommand.java's own whole flow (allowed-dimension check, RtpFinder search,
    // distance detail, the real 7-arg TeleportExecutor with RTP_COOLDOWN_SECONDS). Returns null on
    // success or a real error message to show via sendFailure.
    public static String rtp(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return "Not in a server level.";
        var allowedDims = EssentialsServerConfig.RTP_ALLOWED_DIMENSIONS.get();
        if (!allowedDims.isEmpty() && !allowedDims.contains(level.dimension().location().toString())) {
            return "/rtp isn't allowed in this dimension.";
        }
        net.minecraft.core.BlockPos origin = player.blockPosition();
        int minRadius = EssentialsServerConfig.RTP_MIN_RADIUS.get();
        int maxRadius = EssentialsServerConfig.RTP_MAX_RADIUS.get();
        int maxAttempts = EssentialsServerConfig.RTP_MAX_ATTEMPTS.get();
        net.phoenixvine.essentials.rtp.RtpFinder.Result result =
                net.phoenixvine.essentials.rtp.RtpFinder.find(level, origin, player, minRadius, maxRadius, maxAttempts);
        if (result == null) return "Couldn't find a safe spot to land after " + maxAttempts + " tries - try again.";
        net.minecraft.core.BlockPos landing = result.pos();
        double dx = landing.getX() - origin.getX();
        double dz = landing.getZ() - origin.getZ();
        int distance = (int) Math.round(Math.sqrt(dx * dx + dz * dz));
        NamedLocation target = new NamedLocation(level.dimension(), landing.getX() + 0.5, landing.getY(),
                landing.getZ() + 0.5, player.getYRot(), player.getXRot());
        String detail = distance + " blocks away, " + result.attempts() + " "
                + (result.attempts() == 1 ? "try" : "tries");
        int cooldown = EssentialsServerConfig.RTP_COOLDOWN_SECONDS.get();
        TeleportExecutor.request(player, target, "rtp", "a random location", detail, false, cooldown);
        return null;
    }

    // Direct copy of PlayerInfoCommand.java's own report -- one multi-line string (newline-joined),
    // or null if the target has no essentials data. Plain text (no section-sign colors) so the HC
    // side stays simple; formatting is the only difference from the original.
    public static String playerInfo(ServerPlayer target) {
        PlayerEssentialsData data = target.getCapability(EssentialsCapabilityProvider.PLAYER_ESSENTIALS).orElse(null);
        if (data == null) return null;
        String nickname = data.getNickname();
        String login = target.getGameProfile().getName();
        String displayName = nickname.isEmpty() ? login : nickname + " (" + login + ")";
        return "--- " + displayName + " ---\n"
                + "Gamemode: " + target.gameMode.getGameModeForPlayer().getName() + "\n"
                + "Health: " + Math.round(target.getHealth()) + "/" + Math.round(target.getMaxHealth())
                + " Food: " + target.getFoodData().getFoodLevel() + "/20\n"
                + "Playtime: " + playtimeFormatted(data) + "\n"
                + "Homes: " + data.getHomes().size() + "/" + EssentialsServerConfig.HOMES_PER_PLAYER.get() + "\n"
                + "AFK: " + (data.isAfk() ? "yes" : "no");
    }

    // Direct copy of EssentialsTrashCommand.java's own virtual 9x4 void container menu -- avoids
    // binding SimpleContainer/SimpleMenuProvider/ChestMenu/MenuType (and the menu-factory lambda)
    // for one real caller. Anything put in is discarded when the menu closes, same as the original.
    public static void openTrash(ServerPlayer player) {
        net.minecraft.world.SimpleContainer voidContainer = new net.minecraft.world.SimpleContainer(36);
        player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                (id, playerInv, p) -> new net.minecraft.world.inventory.ChestMenu(
                        net.minecraft.world.inventory.MenuType.GENERIC_9x4, id, playerInv, voidContainer, 4),
                Component.literal("Trash")));
    }

    // The player's own auto-trash ids, comma-joined and sorted -- feeds tab-complete for
    // `/hcautotrash remove <item>` without binding Set<ResourceLocation> iteration.
    public static String autoTrashCsv(PlayerEssentialsData data) {
        List<String> ids = new ArrayList<>();
        for (net.minecraft.resources.ResourceLocation id : data.getAlwaysTrash()) ids.add(id.toString());
        ids.sort(String::compareTo);
        return String.join(",", ids);
    }

    // `SimpleChannel.registerMessage`'s handler receives its Context wrapped in a Supplier<Context>;
    // calling .get() on a value typed as a plain extern interface has no HC mechanism yet, so this
    // is the same tiny unwrap hotc-mc's Ganache shim (GanacheNet.unwrapSupplier) provides there.
    public static Object unwrapSupplier(Object supplier) {
        return ((java.util.function.Supplier<?>) supplier).get();
    }

    public static String dimensionLabel(ServerLevel level) {
        return level.dimension().location().toString();
    }

    // Direct copy of SeenCommand.java's own online/offline lookup + elapsed formatting -- one real
    // call instead of binding GameProfileCache/Optional/UUID/OfflinePlayerIndex.Entry's public
    // fields individually. Returns the plain-text result, or null if there's no record at all.
    public static String seenMessage(MinecraftServer server, String name) {
        ServerPlayer online = server.getPlayerList().getPlayerByName(name);
        if (online != null) return online.getGameProfile().getName() + " is online now.";
        UUID uuid = null;
        try {
            uuid = server.getProfileCache() == null ? null :
                    server.getProfileCache().get(name).map(p -> p.getId()).orElse(null);
        } catch (Exception e) {
            // Real, observed: an unknown name's profile-cache lookup can throw (e.g. the online
            // profile fetch failing in an offline/dev session) -- treat as "no record" instead of
            // surfacing Brigadier's generic "unexpected error". Logged so it isn't silent.
            PhoenixEssentials.LOGGER.warn("[hc] /hcseen profile cache lookup failed for '{}'", name, e);
        }
        OfflinePlayerIndex.Entry entry = uuid == null ? null : OfflinePlayerIndex.get(uuid);
        if (entry == null) return null;
        long elapsedMs = System.currentTimeMillis() - entry.lastSeenMs;
        long seconds = elapsedMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        String elapsed;
        if (days > 0) elapsed = days + "d " + (hours % 24) + "h";
        else if (hours > 0) elapsed = hours + "h " + (minutes % 60) + "m";
        else if (minutes > 0) elapsed = minutes + "m";
        else elapsed = seconds + "s";
        return entry.lastKnownName + " was last seen " + elapsed + " ago.";
    }

    // Real /tpa family -- a direct copy of TpaCommand.java's own request/respond/cancel logic with
    // this class's OWN pending-request map (so /hctpa* requests and the real /tpa* requests don't
    // cross-wire while both are registered). Each method returns null on success, or a real error
    // message for the caller to show via sendFailure; success messaging goes out via
    // sendSystemMessage exactly like the original.
    private record TpaRequest(UUID fromUuid, String fromName, boolean here, long expiresAtMs) {}
    private static final Map<UUID, TpaRequest> TPA_PENDING = new ConcurrentHashMap<>();

    public static String tpaRequest(ServerPlayer requester, ServerPlayer target, boolean here) {
        if (target == requester) return "You can't send a teleport request to yourself.";
        PlayerEssentialsData targetData = target.getCapability(EssentialsCapabilityProvider.PLAYER_ESSENTIALS).orElse(null);
        if (targetData != null && targetData.isIgnoring(requester.getUUID())) {
            return "That player isn't accepting teleport requests right now.";
        }
        int timeoutSeconds = EssentialsServerConfig.TPA_TIMEOUT_SECONDS.get();
        TPA_PENDING.put(target.getUUID(), new TpaRequest(requester.getUUID(), requester.getName().getString(), here,
                System.currentTimeMillis() + timeoutSeconds * 1000L));
        requester.sendSystemMessage(Component.literal("§7Teleport request sent to §f" + target.getName().getString()
                + "§7. Expires in " + timeoutSeconds + "s."));
        String action = here ? "wants you to teleport to them" : "wants to teleport to you";
        target.sendSystemMessage(Component.literal("§f" + requester.getName().getString() + " §7" + action
                + ". §a/hctpaccept §7or §c/hctpdeny §7(expires in " + timeoutSeconds + "s)"));
        return null;
    }

    public static String tpaRespond(MinecraftServer server, ServerPlayer target, boolean accept) {
        TpaRequest req = TPA_PENDING.remove(target.getUUID());
        if (req == null || System.currentTimeMillis() > req.expiresAtMs()) {
            return "You have no pending teleport request.";
        }
        ServerPlayer requester = server.getPlayerList().getPlayer(req.fromUuid());
        if (requester == null) return req.fromName() + " is no longer online.";
        if (!accept) {
            target.sendSystemMessage(Component.literal("§7Teleport request denied."));
            requester.sendSystemMessage(Component.literal("§f" + target.getName().getString()
                    + " §7denied your teleport request."));
            return null;
        }
        boolean bypassDelay = EssentialsServerConfig.TPA_TEAMMATE_BYPASS_DELAY.get()
                && TeamCompat.areTeammates(requester, target);
        int cooldown = EssentialsServerConfig.TPA_COOLDOWN_SECONDS.get();
        if (req.here()) {
            TeleportExecutor.request(target, NamedLocation.of(requester), "tpa", requester.getName().getString(),
                    null, bypassDelay, cooldown);
        } else {
            TeleportExecutor.request(requester, NamedLocation.of(target), "tpa", target.getName().getString(),
                    null, bypassDelay, cooldown);
        }
        return null;
    }

    public static String tpaCancel(ServerPlayer requester) {
        boolean removedAny = TPA_PENDING.entrySet().removeIf(e -> e.getValue().fromUuid().equals(requester.getUUID()));
        if (!removedAny) return "You have no pending outgoing teleport request.";
        requester.sendSystemMessage(Component.literal("§7Teleport request cancelled."));
        return null;
    }

    public static void claimKit(ServerPlayer player, PlayerEssentialsData data, String name) {
        KitDefinition kit = KitRegistry.get(name);
        if (kit == null) return;
        for (KitItemEntry entry : kit.items) {
            ItemStack stack = entry.toStack();
            if (stack.isEmpty()) continue;
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }
        data.recordKitClaimed(name);
    }
}
