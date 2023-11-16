package pw.rxj.iron_quarry.util;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import java.util.Iterator;

public class ComplexInventory extends SimpleInventory {
    public ComplexInventory(int size){
        super(size);
    }

    public NbtList write(){
        NbtList items = new NbtList();

        Iterator<ItemStack> stacks = this.stacks.iterator();

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
        for(int i = 0; i < items.size(); ++i) {
            NbtCompound item = items.getCompound(i);

            int slot = item.getByte("Slot") & 255;
            if (slot >= 0 && slot < this.stacks.size()) {
                this.stacks.set(slot, ItemStack.fromNbt(item));
            }
        }
    }
}
