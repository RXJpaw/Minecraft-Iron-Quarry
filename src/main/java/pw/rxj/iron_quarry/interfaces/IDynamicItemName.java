package pw.rxj.iron_quarry.interfaces;

import net.minecraft.item.ItemStack;
import pw.rxj.iron_quarry.types.DynamicItemName;

public interface IDynamicItemName {
    DynamicItemName getDynamicItemName(ItemStack stack);
}
