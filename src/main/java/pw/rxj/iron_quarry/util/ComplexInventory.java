package pw.rxj.iron_quarry.util;

import com.mojang.serialization.Codec;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;

public class ComplexInventory extends SimpleInventory {
    public ComplexInventory(int size){
        super(size);
    }
    public static ComplexInventory ofSize(int size) {
        return new ComplexInventory(size);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof ComplexInventory other) || other.size() != this.size()) return false;

        for (int i = 0; i < this.size(); i++) {
            if(!ItemStack.areEqual(this.getStack(i), other.getStack(i))) return false;
        }

        return true;
    }
    @Override
    public int hashCode() {
        return ItemStack.listHashCode(this.getHeldStacks());
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

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return super.isValid(slot, stack);
    }

    protected ComplexInventory copy() {
        ComplexInventory clone = ComplexInventory.ofSize(this.size());

        for (int i = 0; i < clone.size(); i++) {
            clone.setStack(i, this.getStack(i).copy());
        }

        return clone;
    }

    public int occupiedSlots() {
        Iterator<ItemStack> stacks = this.heldStacks.iterator();
        int occupiedSlots = 0;

        for (int i = 0; stacks.hasNext(); i++) {
            ItemStack stack = stacks.next();

            if(!stack.isEmpty()) occupiedSlots++;
        }

        return occupiedSlots;
    }

    public NbtList write(RegistryWrapper.WrapperLookup registries){
        NbtList items = new NbtList();

        Iterator<ItemStack> stacks = this.heldStacks.iterator();

        for (int i = 0; stacks.hasNext(); i++) {
            ItemStack stack = stacks.next();

            if(stack.isEmpty()) continue;

            NbtCompound item = (NbtCompound) stack.toNbt(registries);
            item.putByte("Slot", (byte) i);

            items.add(item);
        }

        return items;
    }
    public void read(NbtList nbtList, RegistryWrapper.WrapperLookup registries){
        this.clear();

        for(int i = 0; i < nbtList.size(); ++i) {
            NbtCompound item = nbtList.getCompound(i);
            int slot = nbtList.getCompound(i).getByte("Slot") & 255;

            if (slot < this.heldStacks.size()) {
                ItemStack.fromNbt(registries, item).ifPresent(stack -> this.heldStacks.set(slot, stack));
            }
        }
    }

    //components

    public static final Codec<ComplexInventory> CODEC = ContainerComponent.Slot.CODEC.sizeLimitedListOf(256).xmap(ComplexInventory::fromSlots, ComplexInventory::collectSlots);
    public static final PacketCodec<RegistryByteBuf, ComplexInventory> PACKET_CODEC = ItemStack.OPTIONAL_PACKET_CODEC.collect(PacketCodecs.toList(256)).xmap(ComplexInventory::new, (component) -> component.heldStacks);

    private ComplexInventory(List<ItemStack> stacks) {
        this(stacks.size());

        for(int slot = 0; slot < stacks.size(); ++slot) {
            this.heldStacks.set(slot, stacks.get(slot));
        }
    }
    public void read(@Nullable ComplexInventory inventory){
        if(inventory == null) return;

        this.clear();

        for (int slot = 0; slot < inventory.size(); slot++) {
            this.heldStacks.set(slot, inventory.getStack(slot));
        }
    }
    private List<ContainerComponent.Slot> collectSlots() {
        List<ContainerComponent.Slot> list = new ArrayList<>();

        for(int i = 0; i < this.heldStacks.size(); ++i) {
            ItemStack itemStack = this.heldStacks.get(i);
            if (!itemStack.isEmpty()) {
                list.add(new ContainerComponent.Slot(i, itemStack));
            }
        }

        return list;
    }
    private static ComplexInventory fromSlots(List<ContainerComponent.Slot> slots) {
        OptionalInt optionalInt = slots.stream().mapToInt(ContainerComponent.Slot::index).max();
        if (optionalInt.isEmpty()) {
            return ComplexInventory.ofSize(0);
        } else {
            ComplexInventory inventory = ComplexInventory.ofSize(optionalInt.getAsInt() + 1);

            for(ContainerComponent.Slot slot : slots) {
                inventory.heldStacks.set(slot.index(), slot.item());
            }

            return inventory;
        }
    }
}
