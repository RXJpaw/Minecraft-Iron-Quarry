package pw.rxj.iron_quarry.util;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ManagedSlot extends Slot {
    private boolean isEnabled = true;
    private boolean isLocked = false;

    public ManagedSlot(Container inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        boolean isValid = !(container instanceof SimpleContainer simpleInventory) || simpleInventory.canPlaceItem(0, stack);

        return super.mayPlace(stack) && !this.isLocked() && isValid;
    }
    @Override
    public boolean isActive() {
        return this.isEnabled && !this.isLocked();
    }
    public boolean isLocked() {
        return this.isLocked;
    }

    public void setEnabled(boolean state) {
        this.isEnabled = state;
    }
    public void setLocked(boolean state) {
        this.isLocked = state;
    }
}
