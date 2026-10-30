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

public final class EssentialsHcServerBridge {
    private EssentialsHcServerBridge() {}

    private static final Map<UUID, UUID> LAST_MESSAGED = new ConcurrentHashMap<>();

    public static String sendDirectMessage(ServerPlayer sender, ServerPlayer target, String message) {
        if (target == sender) return "You can't message yourself.";
        PlayerEssentialsData targetData = EssentialsCapabilityProvider.lookup(target).orElse(null);
        if (targetData != null && targetData.isIgnoring(sender.getUUID())) {
            return "That player isn't receiving messages right now.";
        }
        sender.sendSystemMessage(Component.literal("§7[me -> " + target.getName().getString() + "] §f" + message));
        target.sendSystemMessage(Component.literal("§7[" + sender.getName().getString() + " -> me] §f" + message));
        LAST_MESSAGED.put(sender.getUUID(), target.getUUID());
        LAST_MESSAGED.put(target.getUUID(), sender.getUUID());
        return null;
    }

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

    public static void setIgnored(PlayerEssentialsData data, ServerPlayer target, boolean ignore) {
        if (ignore) {
            data.getIgnored().add(target.getUUID());
        } else {
            data.getIgnored().remove(target.getUUID());
        }
    }

    public static NamedLocation topLocation(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return null;
        int x = player.getBlockX();
        int z = player.getBlockZ();
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        return new NamedLocation(level.dimension(), x + 0.5, y, z + 0.5, player.getYRot(), player.getXRot());
    }

    public static NamedLocation tpxLocation(ServerPlayer player, ServerLevel targetLevel) {
        if (player.level() == targetLevel) return null;
        int x = player.getBlockX();
        int z = player.getBlockZ();
        targetLevel.getChunk(x >> 4, z >> 4); 
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

    public static String playerInfo(ServerPlayer target) {
        PlayerEssentialsData data = EssentialsCapabilityProvider.lookup(target).orElse(null);
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

    public static void openTrash(ServerPlayer player) {
        net.minecraft.world.SimpleContainer voidContainer = new net.minecraft.world.SimpleContainer(36);
        player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                (id, playerInv, p) -> new net.minecraft.world.inventory.ChestMenu(
                        net.minecraft.world.inventory.MenuType.GENERIC_9x4, id, playerInv, voidContainer, 4),
                Component.literal("Trash")));
    }

    public static String autoTrashCsv(PlayerEssentialsData data) {
        List<String> ids = new ArrayList<>();
        for (net.minecraft.resources.ResourceLocation id : data.getAlwaysTrash()) ids.add(id.toString());
        ids.sort(String::compareTo);
        return String.join(",", ids);
    }

    public static Object unwrapSupplier(Object supplier) {
        return ((java.util.function.Supplier<?>) supplier).get();
    }

    public static String teleportPlayerTo(ServerPlayer player, NamedLocation target) {
        ServerLevel targetLevel = player.getServer().getLevel(target.dimension);
        if (targetLevel == null) return "That destination's dimension is no longer loaded.";
        EssentialsCapabilityProvider.lookup(player)
                .ifPresent(data -> data.setBack(NamedLocation.of(player)));
        player.teleportTo(targetLevel, target.x, target.y, target.z, java.util.Set.of(), target.yaw, target.pitch);
        return null;
    }

    public static String dimensionLabel(ServerLevel level) {
        return level.dimension().location().toString();
    }

    public static String seenMessage(MinecraftServer server, String name) {
        ServerPlayer online = server.getPlayerList().getPlayerByName(name);
        if (online != null) return online.getGameProfile().getName() + " is online now.";
        UUID uuid = null;
        try {
            uuid = server.getProfileCache() == null ? null :
                    server.getProfileCache().get(name).map(p -> p.getId()).orElse(null);
        } catch (Exception e) {

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

    private record TpaRequest(UUID fromUuid, String fromName, boolean here, long expiresAtMs) {}
    private static final Map<UUID, TpaRequest> TPA_PENDING = new ConcurrentHashMap<>();

    public static String tpaRequest(ServerPlayer requester, ServerPlayer target, boolean here) {
        if (target == requester) return "You can't send a teleport request to yourself.";
        PlayerEssentialsData targetData = EssentialsCapabilityProvider.lookup(target).orElse(null);
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
                + ". §a/" + EssentialsHcMode.literal("tpaccept", "hctpaccept") + " §7or §c/" + EssentialsHcMode.literal("tpdeny", "hctpdeny")
                + " §7(expires in " + timeoutSeconds + "s)"));
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

    private static String playtimeFormatted(PlayerEssentialsData data) {
        long totalSeconds = data.getPlaytimeTicks() / 20;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        StringBuilder sb = new StringBuilder();
        if (hours > 0) sb.append(hours).append("h ");
        if (hours > 0 || minutes > 0) sb.append(minutes).append("m ");
        sb.append(seconds).append("s");
        return sb.toString();
    }
}
