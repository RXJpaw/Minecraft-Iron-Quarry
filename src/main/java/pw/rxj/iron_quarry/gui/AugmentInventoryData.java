package pw.rxj.iron_quarry.gui;

import pw.rxj.iron_quarry.util.ComplexInventory;

public class AugmentInventoryData implements CustomTooltipData {
    private final ComplexInventory MachineUpgradesInventory;
    private final int augmentLimit;

    private AugmentInventoryData(ComplexInventory MachineUpgradesInventory, int augmentLimit) {
        this.MachineUpgradesInventory = MachineUpgradesInventory;
        this.augmentLimit = augmentLimit;
    }

    public static AugmentInventoryData from(ComplexInventory MachineUpgradesInventory, int augmentLimit) {
        return new AugmentInventoryData(MachineUpgradesInventory, augmentLimit);
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
