package pw.rxj.iron_quarry.gui;

import pw.rxj.iron_quarry.util.ComplexInventory;

public class TooltipAugmentInventoryData implements CustomTooltipData {
    private final ComplexInventory MachineUpgradesInventory;
    private final int augmentLimit;

    private TooltipAugmentInventoryData(ComplexInventory MachineUpgradesInventory, int augmentLimit) {
        this.MachineUpgradesInventory = MachineUpgradesInventory;
        this.augmentLimit = augmentLimit;
    }

    public static TooltipAugmentInventoryData from(ComplexInventory MachineUpgradesInventory, int augmentLimit) {
        return new TooltipAugmentInventoryData(MachineUpgradesInventory, augmentLimit);
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
