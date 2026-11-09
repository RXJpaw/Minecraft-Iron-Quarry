package pw.rxj.iron_quarry.gui;

import net.minecraft.world.item.ItemStack;
import pw.rxj.iron_quarry.block.QuarryBlock;
import pw.rxj.iron_quarry.util.ComplexInventory;

public class TooltipQuarryInventoryData implements CustomTooltipData {
    private final ItemStack blueprintStack;
    private final ItemStack drillStack;
    private final ComplexInventory augmentInventory;
    private final int augmentLimit;
    private final ComplexInventory expansionInventory;
    private final int expansionLimit;

    private TooltipQuarryInventoryData(QuarryBlock quarryBlock, ItemStack stack) {
        this.blueprintStack = quarryBlock.getBlueprintStack(stack);
        this.drillStack = quarryBlock.getDrillStack(stack);
        this.augmentInventory = quarryBlock.getAugmentInventory(stack);
        this.augmentLimit = quarryBlock.getAugmentLimit();
        this.expansionInventory = quarryBlock.getExpansionInventory(stack);
        this.expansionLimit = quarryBlock.getExpansionLimit();
    }

    public static TooltipQuarryInventoryData from(QuarryBlock quarryBlock, ItemStack stack) {
        return new TooltipQuarryInventoryData(quarryBlock, stack);
    }

    public ItemStack getBlueprintStack() {
        return this.blueprintStack;
    }
    public ItemStack getDrillStack() {
        return this.drillStack;
    }
    public ComplexInventory getAugmentInventory() {
        return this.augmentInventory;
    }
    public int getAugmentLimit() {
        return this.augmentLimit;
    }
    public ComplexInventory getExpansionInventory() {
        return this.expansionInventory;
    }
    public int getExpansionLimit() {
        return this.expansionLimit;
    }

    @Override
    public Boolean renderAtMarker() {
        return true;
    }
}
