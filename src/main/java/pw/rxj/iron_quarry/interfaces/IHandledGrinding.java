package pw.rxj.iron_quarry.interfaces;

import net.minecraft.item.ItemStack;

public interface IHandledGrinding {
    default boolean isGrindable(ItemStack stack) {
        return true;
    }

    ItemStack getGrindingOutput(ItemStack stack, ItemStack other);
}
