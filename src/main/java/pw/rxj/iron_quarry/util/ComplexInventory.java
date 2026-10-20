package pw.rxj.iron_quarry.util;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ComplexInventory extends SimpleContainer {
    public ComplexInventory(int size){
        super(size);
    }
    public static ComplexInventory ofSize(int size) {
        return new ComplexInventory(size);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof ComplexInventory other) || other.getContainerSize() != this.getContainerSize()) return false;

        for (int i = 0; i < this.getContainerSize(); i++) {
            if(!ItemStack.matches(this.getItem(i), other.getItem(i))) return false;
        }

        return true;
    }
    @Override
    public int hashCode() {
        return ItemStack.hashStackList(this.getItems());
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

    //components

    public static final Codec<ComplexInventory> CODEC = ItemContainerContents.Slot.CODEC.sizeLimitedListOf(256).xmap(ComplexInventory::fromSlots, ComplexInventory::collectSlots);
    public static final StreamCodec<RegistryFriendlyByteBuf, ComplexInventory> PACKET_CODEC = ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list(256)).map(ComplexInventory::new, (component) -> component.items);

    private ComplexInventory(List<ItemStack> stacks) {
        this(stacks.size());

        for(int slot = 0; slot < stacks.size(); ++slot) {
            this.items.set(slot, stacks.get(slot));
        }
    }
    public void read(@Nullable ComplexInventory inventory){
        if(inventory == null) return;

        this.clearContent();

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            this.items.set(slot, inventory.getItem(slot));
        }
    }
    private List<ItemContainerContents.Slot> collectSlots() {
        List<ItemContainerContents.Slot> list = new ArrayList<>();

        for(int i = 0; i < this.items.size(); ++i) {
            ItemStack itemStack = this.items.get(i);
            if (!itemStack.isEmpty()) {
                list.add(new ItemContainerContents.Slot(i, itemStack));
            }
        }

        return list;
    }
    private static ComplexInventory fromSlots(List<ItemContainerContents.Slot> slots) {
        OptionalInt optionalInt = slots.stream().mapToInt(ItemContainerContents.Slot::index).max();
        if (optionalInt.isEmpty()) {
            return ComplexInventory.ofSize(0);
        } else {
            ComplexInventory inventory = ComplexInventory.ofSize(optionalInt.getAsInt() + 1);

            for(ItemContainerContents.Slot slot : slots) {
                inventory.items.set(slot.index(), slot.item());
            }

            return inventory;
        }
    }
}
