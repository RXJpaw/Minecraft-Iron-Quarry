package pw.rxj.iron_quarry.interfaces;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import pw.rxj.iron_quarry.util.ZUtil;

public interface IEnergyContainer {
    int getBaseConsumption();
    int getEnergyCapacity();

    default long getEnergyStored(ItemStack stack) {
        NbtCompound nbtCompound = stack.copy().getOrCreateNbt();;

        return nbtCompound.getCompound("BlockEntityTag").getCompound("rxj.pw/Energy").getLong("Stored");
    }

    static IEnergyContainer instanceOf(ItemStack stack) {
        Object blockOrItem = ZUtil.getBlockOrItem(stack);

        if(blockOrItem instanceof IEnergyContainer energyContainer) {
            return energyContainer;
        } else {
            return null;
        }
    }
}
