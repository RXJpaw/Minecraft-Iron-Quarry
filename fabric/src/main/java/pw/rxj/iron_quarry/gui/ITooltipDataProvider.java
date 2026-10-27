package pw.rxj.iron_quarry.gui;

import java.util.Optional;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public interface ITooltipDataProvider {
    Optional<TooltipComponent> getTooltipImage(ItemStack stack);
}
