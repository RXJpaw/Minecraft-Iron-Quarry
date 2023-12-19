package pw.rxj.iron_quarry.interfaces;

import net.minecraft.item.ItemStack;

public interface IAlwaysRenderItemName {
    default boolean renderItemName(ItemStack stack) {
        return true;
    }
}
