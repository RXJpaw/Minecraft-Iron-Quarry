package pw.rxj.iron_quarry.interfaces;

import net.minecraft.item.ItemStack;
import pw.rxj.iron_quarry.util.ZUtil;
import team.reborn.energy.impl.EnergyImpl;

public interface IEnergyContainer {
    int getBaseConsumption();
    int getEnergyCapacity();

    default long getEnergyStored(ItemStack stack) {
        return stack.getOrDefault(EnergyImpl.ENERGY_COMPONENT, 0L);
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
