package pw.rxj.iron_quarry.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public interface CustomTooltipData extends TooltipComponent {
    Component MARKER = Component.literal("[Unimplemented Tooltip-Component]");

    default Boolean renderAtMarker() {
        return false;
    }
}
