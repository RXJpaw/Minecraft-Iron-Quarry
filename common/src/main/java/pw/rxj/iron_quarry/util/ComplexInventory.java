package pw.rxj.iron_quarry.util;

import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ComplexInventory extends SimpleContainer {
    public ComplexInventory(int size){
        super(size);
    }
    public static ComplexInventory ofSize(int size) {
        return new ComplexInventory(size);
    }

    public boolean canInsertAll(List<ItemStack> stacks) {
        return this.canInsertAll(stacks.toArray(new ItemStack[0]));
    }
    public boolean canInsertAll(ItemStack ...stacks) {
        if(stacks.length == 0) {
            return true;
        } else if(stacks.length == 1) {
            return this.canAddItem(stacks[0]);
        } else {
            SimpleContainer clone = this.copy();

            for (ItemStack stack : stacks) {
                if(clone.canAddItem(stack)) {
                    clone.addItem(stack);
                } else {
                    return false;
                }
            }

            return true;
        }
    }
    @Override
    public boolean canAddItem(ItemStack stack) {
        return this.canPlaceItem(0, stack) && super.canAddItem(stack);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return super.canPlaceItem(slot, stack);
    }

    protected ComplexInventory copy() {
        ComplexInventory clone = ComplexInventory.ofSize(this.getContainerSize());

        for (int i = 0; i < clone.getContainerSize(); i++) {
            clone.setItem(i, this.getItem(i).copy());
        }

        return clone;
    }

    public int occupiedSlots() {
        java.util.Iterator<ItemStack> stacks = this.items.iterator();
        int occupiedSlots = 0;

        for (int i = 0; stacks.hasNext(); i++) {
            ItemStack stack = stacks.next();

            if(!stack.isEmpty()) occupiedSlots++;
        }

        return occupiedSlots;
    }

    public void write(ValueOutput view){
        ContainerHelper.saveAllItems(view, this.items);
    }
    public void read(ValueInput view){
        this.clearContent();

        ContainerHelper.loadAllItems(view, this.items);
    }

    public void read(@Nullable ItemContainerContents contents){
        if(contents == null) return;

        contents.copyInto(this.items);
    }
}
