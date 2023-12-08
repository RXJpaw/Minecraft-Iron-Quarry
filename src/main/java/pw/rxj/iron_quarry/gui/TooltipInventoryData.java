package pw.rxj.iron_quarry.gui;

import net.minecraft.item.ItemStack;
import oshi.util.tuples.Pair;

import java.util.List;

public class TooltipInventoryData implements CustomTooltipData {
    private final List<Pair<ItemStack, Boolean>> disableableInventory;

    private TooltipInventoryData(List<Pair<ItemStack, Boolean>> disableableInventory) {
        this.disableableInventory = disableableInventory;
    }

    public static TooltipInventoryData from(List<Pair<ItemStack, Boolean>> disableableInventory) {
        return new TooltipInventoryData(disableableInventory);
    }

    public List<Pair<ItemStack, Boolean>> getDisableableInventory() {
        return this.disableableInventory;
    }

    @Override
    public Boolean renderAtMarker() {
        return true;
    }
}
