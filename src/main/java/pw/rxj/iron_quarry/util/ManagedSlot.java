package pw.rxj.iron_quarry.util;

import net.minecraft.inventory.Inventory;
import net.minecraft.screen.slot.Slot;

public class ManagedSlot extends Slot {
    private boolean isEnabled = true;

    public ManagedSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean isEnabled() {
        return this.isEnabled;
    }

    public void setEnabled(boolean state) {
        this.isEnabled = state;
    }
}
