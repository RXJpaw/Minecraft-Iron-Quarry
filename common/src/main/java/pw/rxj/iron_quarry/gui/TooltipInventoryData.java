package pw.rxj.iron_quarry.gui;

import com.mojang.datafixers.util.Pair;

import java.util.List;
import net.minecraft.world.item.ItemStack;

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
