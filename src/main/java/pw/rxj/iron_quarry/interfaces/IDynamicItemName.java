package pw.rxj.iron_quarry.interfaces;

import net.minecraft.item.ItemStack;
import pw.rxj.iron_quarry.types.DynamicText;

public interface IDynamicItemName {
    DynamicText getDynamicItemName(ItemStack stack);
}
