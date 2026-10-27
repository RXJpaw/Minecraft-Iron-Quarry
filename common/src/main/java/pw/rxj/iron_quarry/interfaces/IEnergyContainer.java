package pw.rxj.iron_quarry.interfaces;

import net.minecraft.world.item.ItemStack;
import pw.rxj.iron_quarry.platform.Platform;
import pw.rxj.iron_quarry.util.ZUtil;

public interface IEnergyContainer {
    int getBaseConsumption();
    int getEnergyCapacity();

    default long getEnergyStored(ItemStack stack) {
        return stack.getOrDefault(Platform.ENERGY.getEnergyComponent(), 0L);
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
