package pw.rxj.iron_quarry.gui;

import net.minecraft.client.gui.tooltip.TooltipComponent;

public interface CustomTooltipComponent extends TooltipComponent {
    CustomTooltipData getCustomTooltipData();
}
