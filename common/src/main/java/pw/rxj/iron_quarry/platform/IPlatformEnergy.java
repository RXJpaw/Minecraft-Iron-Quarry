package pw.rxj.iron_quarry.platform;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import pw.rxj.iron_quarry.util.ComplexEnergyContainer;

import java.util.function.Function;
import java.util.function.Supplier;

public interface IPlatformEnergy {
    <T extends BlockEntity> void registerBlockEntity(Supplier<BlockEntityType<T>> blockEntityType, Function<T, ComplexEnergyContainer> energyContainer);

    DataComponentType<Long> getEnergyComponent();
    boolean canExtractEnergy(ItemStack stack);
    boolean chargeFromSlot(Container container, int slot, ComplexEnergyContainer target);
}
