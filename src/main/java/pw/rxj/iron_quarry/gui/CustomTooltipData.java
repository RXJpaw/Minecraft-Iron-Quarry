package pw.rxj.iron_quarry.gui;

import net.minecraft.client.item.TooltipData;
import net.minecraft.text.Text;

public interface CustomTooltipData extends TooltipData {
    Text MARKER = Text.literal("[Unimplemented Tooltip-Component]");

    default Boolean renderAtMarker() {
        return false;
    }
}
