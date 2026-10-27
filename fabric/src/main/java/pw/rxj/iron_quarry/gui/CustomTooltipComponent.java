package pw.rxj.iron_quarry.gui;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public interface CustomTooltipComponent extends ClientTooltipComponent {
    CustomTooltipData getCustomTooltipData();
}
