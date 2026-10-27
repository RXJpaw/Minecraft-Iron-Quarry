package pw.rxj.iron_quarry.gui;

import net.minecraft.world.item.ItemStack;
import pw.rxj.iron_quarry.util.ComplexInventory;

public class TooltipQuarryInventoryData implements CustomTooltipData {
    private final ItemStack blueprintStack;
    private final ItemStack drillStack;
    private final ComplexInventory MachineUpgradesInventory;
    private final int augmentLimit;

    private TooltipQuarryInventoryData(ItemStack blueprintStack, ItemStack drillStack, ComplexInventory MachineUpgradesInventory, int augmentLimit) {
        this.blueprintStack = blueprintStack;
        this.drillStack = drillStack;
        this.MachineUpgradesInventory = MachineUpgradesInventory;
        this.augmentLimit = augmentLimit;
    }

    public static TooltipQuarryInventoryData from(ItemStack blueprintStack, ItemStack drillStack, ComplexInventory MachineUpgradesInventory, int augmentLimit) {
        return new TooltipQuarryInventoryData(blueprintStack, drillStack, MachineUpgradesInventory, augmentLimit);
    }

    public ItemStack getBlueprintStack() {
        return this.blueprintStack;
    }
    public ItemStack getDrillStack() {
        return this.drillStack;
    }
    public ComplexInventory getMachineUpgradesInventory() {
        return this.MachineUpgradesInventory;
    }
    public int getAugmentLimit() {
        return this.augmentLimit;
    }

    @Override
    public Boolean renderAtMarker() {
        return true;
    }
}
