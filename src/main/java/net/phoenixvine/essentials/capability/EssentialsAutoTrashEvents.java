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
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) return; 
        Player player = event.getEntity();
        PlayerEssentialsData data = player.getCapability(EssentialsCapabilityProvider.PLAYER_ESSENTIALS)
                .orElse(null);
        if (data == null || data.getAlwaysTrash().isEmpty()) return;

        ItemStack picked = event.getItem().getItem();
        if (!isTrashable(picked)) return;
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(picked.getItem());
        if (id == null || !data.isAlwaysTrash(id)) return;

        event.setCanceled(true);
        event.getItem().discard();
    }

    private static final int SWEEP_INTERVAL_TICKS = 10;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (net.phoenixvine.essentials.EssentialsHcMode.takeover()) return; 
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % SWEEP_INTERVAL_TICKS != 0) return;

        PlayerEssentialsData data = player.getCapability(EssentialsCapabilityProvider.PLAYER_ESSENTIALS)
                .orElse(null);
        if (data == null || data.getAlwaysTrash().isEmpty()) return;

        Inventory inv = player.getInventory();
        int removed = sweepList(inv.items, data) + sweepList(inv.offhand, data);
        if (removed > 0) {
            inv.setChanged();
            player.sendSystemMessage(net.minecraft.network.chat.Component
                    .literal("§7Auto-trashed " + removed + " item" + (removed == 1 ? "" : "s") + "."));
        }
    }

    /**
     * Only plain stacks are ever trashed. A stack with data on it (an enchanted book, a configured circuit, a named
     * or filled item) is left alone, because the item id covers far more than the player meant.
     */
    public static boolean isTrashable(ItemStack stack) {
        return !stack.isEmpty() && !stack.hasTag();
    }

    /** How many items of this kind the player has that auto-trash would delete. */
    public static int countTrashable(Player player, ResourceLocation id) {
        Inventory inv = player.getInventory();
        int total = 0;
        for (var list : java.util.List.of(inv.items, inv.offhand)) {
            for (ItemStack stack : list) {
                if (isTrashable(stack) && id.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()))) {
                    total += stack.getCount();
                }
            }
        }
        return total;
    }

    private static int sweepList(java.util.List<ItemStack> slots, PlayerEssentialsData data) {
        int removed = 0;
        for (int i = 0; i < slots.size(); i++) {
            ItemStack stack = slots.get(i);
            if (!isTrashable(stack)) continue;
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
            if (id != null && data.isAlwaysTrash(id)) {
                removed += stack.getCount();
                slots.set(i, ItemStack.EMPTY);
            }
        }
        return removed;
    }
}
