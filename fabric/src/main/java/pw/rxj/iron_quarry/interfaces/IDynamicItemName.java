package pw.rxj.iron_quarry.interfaces;

import net.minecraft.world.item.ItemStack;
import pw.rxj.iron_quarry.types.DynamicText;

public interface IDynamicItemName {
    DynamicText getDynamicItemName(ItemStack stack);
}
