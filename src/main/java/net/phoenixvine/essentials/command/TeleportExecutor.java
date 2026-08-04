package net.phoenixvine.essentials.command;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.phoenixvine.essentials.PhoenixEssentials;
import net.phoenixvine.essentials.api.EssentialsAPI;
import net.phoenixvine.essentials.api.event.TeleportEvent;
import net.phoenixvine.essentials.capability.EssentialsCapabilityProvider;
import net.phoenixvine.essentials.config.EssentialsPermissions;
import net.phoenixvine.essentials.config.EssentialsServerConfig;
import net.phoenixvine.essentials.data.NamedLocation;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = PhoenixEssentials.MOD_ID)
public final class TeleportExecutor {

    private record Pending(NamedLocation target, long readyAtMs, double startX, double startY, double startZ,
                           net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> startDim,
                           String category, String destinationLabel, String successDetail) {}

    private static final Map<UUID, Pending> PENDING = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<String, Long>> LAST_USE_MS = new ConcurrentHashMap<>();

    private TeleportExecutor() {}

    public static void request(ServerPlayer player, NamedLocation target, String category, String destinationLabel) {
        request(player, target, category, destinationLabel, null);
    }

    public static void request(ServerPlayer player, NamedLocation target, String category, String destinationLabel,
                               String successDetail) {
        request(player, target, category, destinationLabel, successDetail, false);
    }

    public static void request(ServerPlayer player, NamedLocation target, String category, String destinationLabel,
                               String successDetail, boolean bypassWarmupAndCooldown) {
        
        int defaultCooldown = EssentialsServerConfig.TELEPORT_COOLDOWN_SECONDS.get();
        request(player, target, category, destinationLabel, successDetail, bypassWarmupAndCooldown, defaultCooldown);
    }

    public static void request(ServerPlayer player, NamedLocation target, String category, String destinationLabel,
                               String successDetail, boolean bypassWarmupAndCooldown, int cooldownSeconds) {

        if (!EssentialsAPI.isFeatureEnabled(EssentialsAPI.FEATURE_TELEPORT, player.level().dimension().location())) {
            return;
        }

        UUID uuid = player.getUUID();

        if (PENDING.containsKey(uuid)) {
            player.sendSystemMessage(Component.literal("§cYou already have a teleport pending - moving will cancel it."));
            return;
        }

        boolean canBypassCooldown = bypassWarmupAndCooldown ||
                (EssentialsPermissions.BYPASS_COOLDOWN != null && EssentialsPermissions.check(player.createCommandSourceStack(), EssentialsPermissions.BYPASS_COOLDOWN));

        if (!canBypassCooldown) {
            long cooldownRemainingMs = cooldownRemainingMs(uuid, category, cooldownSeconds);
            if (cooldownRemainingMs > 0) {
                long seconds = (cooldownRemainingMs + 999) / 1000;
                player.sendSystemMessage(Component.literal("§cYou must wait " + seconds + "s before doing that again."));
                return;
            }
        }

        TeleportEvent.Pre pre = new TeleportEvent.Pre(player, category, destinationLabel, target);
        MinecraftForge.EVENT_BUS.post(pre);
        if (pre.isCanceled()) return;

        int warmupSeconds = bypassWarmupAndCooldown ? 0 : EssentialsServerConfig.TELEPORT_WARMUP_SECONDS.get();
        if (warmupSeconds <= 0) {
            executeNow(player, target, category, destinationLabel, successDetail);
            return;
        }

        PENDING.put(uuid, new Pending(target, System.currentTimeMillis() + warmupSeconds * 1000L,
                player.getX(), player.getY(), player.getZ(), player.level().dimension(), category, destinationLabel,
                successDetail));
        player.sendSystemMessage(Component.literal("§7Teleporting to §f" + destinationLabel + " §7in " +
                warmupSeconds + "s - don't move!"));
    }

    public static void cancel(ServerPlayer player) {
        PENDING.remove(player.getUUID());
    }

    public static void forceTeleport(ServerPlayer player, NamedLocation target, String category, String destinationLabel) {
        if (!EssentialsAPI.isFeatureEnabled(EssentialsAPI.FEATURE_TELEPORT, player.level().dimension().location())) {
            return;
        }
        executeNow(player, target, category, destinationLabel, null);
    }

    private static void executeNow(ServerPlayer player, NamedLocation target, String category, String destinationLabel,
                                   String successDetail) {
        ServerLevel targetLevel = player.getServer().getLevel(target.dimension);
        if (targetLevel == null) {
            player.sendSystemMessage(Component.literal("§cThat destination's dimension is no longer loaded."));
            return;
        }

        player.getCapability(EssentialsCapabilityProvider.PLAYER_ESSENTIALS)
                .ifPresent(data -> data.setBack(NamedLocation.of(player)));

        player.teleportTo(targetLevel, target.x, target.y, target.z, Set.of(), target.yaw, target.pitch);

        LAST_USE_MS.computeIfAbsent(player.getUUID(), k -> new ConcurrentHashMap<>())
                .put(category, System.currentTimeMillis());
        String suffix = successDetail == null || successDetail.isBlank() ? "" : " §7(" + successDetail + ")";
        player.sendSystemMessage(Component.literal("§aTeleported to §f" + destinationLabel + "§a." + suffix));

        MinecraftForge.EVENT_BUS.post(new TeleportEvent.Post(player, category, destinationLabel));
    }

    private static long cooldownRemainingMs(UUID uuid, String category, int cooldownSeconds) {
        if (cooldownSeconds <= 0) return 0;
        long last = LAST_USE_MS.getOrDefault(uuid, Map.of()).getOrDefault(category, 0L);
        long remaining = cooldownSeconds * 1000L - (System.currentTimeMillis() - last);
        return Math.max(0, remaining);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (PENDING.isEmpty()) return;

        long now = System.currentTimeMillis();
        Iterator<Map.Entry<UUID, Pending>> it = PENDING.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Pending> entry = it.next();
            ServerPlayer player = ServerLifecycleHooks.getCurrentServer() == null ? null :
                    ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(entry.getKey());
            if (player == null) {
                it.remove();
                continue;
            }

            Pending pending = entry.getValue();
            if (EssentialsServerConfig.CANCEL_ON_MOVE.get() && hasMoved(player, pending)) {
                player.sendSystemMessage(Component.literal("§cTeleport to §f" + pending.destinationLabel() +
                        " §ccancelled - you moved."));
                it.remove();
                continue;
            }

            if (now >= pending.readyAtMs()) {
                it.remove();
                executeNow(player, pending.target(), pending.category(), pending.destinationLabel(),
                        pending.successDetail());
            }
        }
    }

    @SubscribeEvent
    public static void onDamage(LivingHurtEvent event) {
        if (!EssentialsServerConfig.CANCEL_ON_DAMAGE.get()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Pending removed = PENDING.remove(player.getUUID());
        if (removed != null) {
            player.sendSystemMessage(Component.literal("§cTeleport to §f" + removed.destinationLabel() +
                    " §ccancelled - you took damage."));
        }
    }

    private static boolean hasMoved(ServerPlayer player, Pending pending) {
        if (!player.level().dimension().equals(pending.startDim())) return true;
        double dx = player.getX() - pending.startX();
        double dy = player.getY() - pending.startY();
        double dz = player.getZ() - pending.startZ();
        return (dx * dx + dy * dy + dz * dz) > 0.01;
    }
}