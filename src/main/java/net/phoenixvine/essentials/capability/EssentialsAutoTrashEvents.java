package net.phoenixvine.essentials.capability;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.phoenixvine.essentials.PhoenixEssentials;

@EventBusSubscriber(modid = PhoenixEssentials.MOD_ID)
public final class EssentialsAutoTrashEvents {

    private EssentialsAutoTrashEvents() {}

    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Pre event) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) return;
        Player player = event.getPlayer();
        PlayerEssentialsData data = EssentialsCapabilityProvider.get(player);
        if (data.getAlwaysTrash().isEmpty()) return;

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(event.getItemEntity().getItem().getItem());
        if (!data.isAlwaysTrash(id)) return;

        event.setCanPickup(TriState.FALSE);
        event.getItemEntity().discard();
    }

    private static final int SWEEP_INTERVAL_TICKS = 10;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % SWEEP_INTERVAL_TICKS != 0) return;

        PlayerEssentialsData data = EssentialsCapabilityProvider.get(player);
        if (data.getAlwaysTrash().isEmpty()) return;

        Inventory inv = player.getInventory();
        boolean changed = sweepList(inv.items, data) | sweepList(inv.offhand, data);
        if (changed) inv.setChanged();
    }

    private static boolean sweepList(java.util.List<ItemStack> slots, PlayerEssentialsData data) {
        boolean changed = false;
        for (int i = 0; i < slots.size(); i++) {
            ItemStack stack = slots.get(i);
            if (stack.isEmpty()) continue;
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (data.isAlwaysTrash(id)) {
                slots.set(i, ItemStack.EMPTY);
                changed = true;
            }
        }
        return changed;
    }
}
