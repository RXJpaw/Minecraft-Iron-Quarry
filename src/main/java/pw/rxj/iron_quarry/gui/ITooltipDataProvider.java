package pw.rxj.iron_quarry.gui;

import net.minecraft.client.item.TooltipData;
import net.minecraft.item.ItemStack;

import java.util.Optional;

public interface ITooltipDataProvider {
    Optional<TooltipData> getTooltipData(ItemStack stack);
}
