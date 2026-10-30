package net.phoenixvine.essentials.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

public class KitItemEntry {

    public String item = "";
    public int count = 1;
    public String nbt = "";

    public KitItemEntry() {}

    public KitItemEntry(String item, int count) {
        this.item = item;
        this.count = count;
    }

    public ItemStack toStack() {
        ResourceLocation rl = ResourceLocation.tryParse(item);
        if (rl == null || !BuiltInRegistries.ITEM.containsKey(rl)) return ItemStack.EMPTY;
        Item i = BuiltInRegistries.ITEM.get(rl);
        if (i == null) return ItemStack.EMPTY;
        ItemStack stack = new ItemStack(i, Math.max(1, count));
        if (nbt != null && !nbt.isBlank()) {
            try {
                CompoundTag tag = TagParser.parseTag(nbt);
                stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
            } catch (Exception ignored) {}
        }
        return stack;
    }
}
