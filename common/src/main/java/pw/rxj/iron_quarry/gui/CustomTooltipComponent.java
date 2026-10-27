package pw.rxj.iron_quarry.gui;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.List;

public interface CustomTooltipComponent extends ClientTooltipComponent {
    CustomTooltipData getCustomTooltipData();

    static void moveToMarker(List<ClientTooltipComponent> components) {
        int componentIndex = -1;
        for (int i = 1; i < components.size(); i++) {
            if(components.get(i) instanceof CustomTooltipComponent) {
                componentIndex = i; break;
            }
        }

        if(componentIndex > 0 && components.get(componentIndex) instanceof CustomTooltipComponent customTooltipComponent) {
            CustomTooltipData customTooltipData = customTooltipComponent.getCustomTooltipData();
            if(!customTooltipData.renderAtMarker()) return;

            int replaceIndex = 0;

            for (int i = 0; i < components.size(); i++) {
                if(components.get(i) instanceof ClientTextTooltip textTooltipComponent) {
                    if(ZUtil.toString(textTooltipComponent.text).equals(CustomTooltipData.MARKER.getString())) {
                        replaceIndex = i; break;
                    }
                }
            }

            if(replaceIndex <= 0) return;

            ClientTooltipComponent substituteComponent = components.remove(componentIndex);
            if(componentIndex < replaceIndex) replaceIndex--;

            components.set(replaceIndex, substituteComponent);
        }
    }
}
