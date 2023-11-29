package pw.rxj.iron_quarry.util;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class ManagedSlot extends Slot {
    private boolean isEnabled = true;
    private boolean isLocked = false;

    public ManagedSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return super.canInsert(stack) && this.isEnabled();
    }
    @Override
    public boolean isEnabled() {
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
