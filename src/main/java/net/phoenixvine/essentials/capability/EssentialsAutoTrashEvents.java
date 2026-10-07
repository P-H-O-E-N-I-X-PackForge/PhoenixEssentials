package net.phoenixvine.essentials.capability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.phoenixvine.essentials.PhoenixEssentials;

@Mod.EventBusSubscriber(modid = PhoenixEssentials.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class EssentialsAutoTrashEvents {

    private EssentialsAutoTrashEvents() {}

    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        Player player = event.getEntity();
        PlayerEssentialsData data = player.getCapability(EssentialsCapabilityProvider.PLAYER_ESSENTIALS)
                .orElse(null);
        if (data == null || data.getAlwaysTrash().isEmpty()) return;

        ResourceLocation id = ForgeRegistries.ITEMS.getKey(event.getItem().getItem().getItem());
        if (id == null || !data.isAlwaysTrash(id)) return;

        event.setCanceled(true);
        event.getItem().discard();
    }

    private static final int SWEEP_INTERVAL_TICKS = 10;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % SWEEP_INTERVAL_TICKS != 0) return;

        PlayerEssentialsData data = player.getCapability(EssentialsCapabilityProvider.PLAYER_ESSENTIALS)
                .orElse(null);
        if (data == null || data.getAlwaysTrash().isEmpty()) return;

        Inventory inv = player.getInventory();
        boolean changed = sweepList(inv.items, data) | sweepList(inv.offhand, data);
        if (changed) inv.setChanged();
    }

    private static boolean sweepList(java.util.List<ItemStack> slots, PlayerEssentialsData data) {
        boolean changed = false;
        for (int i = 0; i < slots.size(); i++) {
            ItemStack stack = slots.get(i);
            if (stack.isEmpty()) continue;
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
            if (id != null && data.isAlwaysTrash(id)) {
                slots.set(i, ItemStack.EMPTY);
                changed = true;
            }
        }
        return changed;
    }
}
