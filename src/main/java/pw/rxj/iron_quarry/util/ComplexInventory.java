package pw.rxj.iron_quarry.util;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import java.util.Iterator;
import java.util.List;

public class ComplexInventory extends SimpleInventory {
    public ComplexInventory(int size){
        super(size);
    }

    public boolean canInsertAll(List<ItemStack> stacks) {
        return this.canInsertAll(stacks.toArray(new ItemStack[0]));
    }
    public boolean canInsertAll(ItemStack ...stacks) {
        if(stacks.length == 0) {
            return true;
        } else if(stacks.length == 1) {
            return this.canInsert(stacks[0]);
        } else {
            SimpleInventory clone = this.copy();

            for (ItemStack stack : stacks) {
                if(clone.canInsert(stack)) {
                    clone.addStack(stack);
                } else {
                    return false;
                }
            }

            return true;
        }
    }
    @Override
    public boolean canInsert(ItemStack stack) {
        return this.isValid(0, stack) && super.canInsert(stack);
    }

    public NbtList write(){
        NbtList items = new NbtList();

        Iterator<ItemStack> stacks = this.heldStacks.iterator();

        for (int i = 0; stacks.hasNext(); i++) {
            ItemStack stack = stacks.next();

            if(stack.isEmpty()) continue;

            NbtCompound item = new NbtCompound();
            item.putByte("Slot", (byte) i);
            stack.writeNbt(item);

            items.add(item);
        }

        return items;
    }

    public void read(NbtList items){
        this.heldStacks.clear();

        for(int i = 0; i < items.size(); ++i) {
            NbtCompound item = items.getCompound(i);

            int slot = item.getByte("Slot") & 255;
            if (slot >= 0 && slot < this.heldStacks.size()) {
                this.heldStacks.set(slot, ItemStack.fromNbt(item));
            }
        }
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return super.isValid(slot, stack);
    }

    protected ComplexInventory copy() {
        ComplexInventory clone = new ComplexInventory(this.size());

        for (int i = 0; i < clone.size(); i++) {
            clone.setStack(i, this.getStack(i).copy());
        }

        return clone;
    }
}
