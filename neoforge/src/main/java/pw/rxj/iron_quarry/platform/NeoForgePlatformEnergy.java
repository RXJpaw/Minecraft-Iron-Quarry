package pw.rxj.iron_quarry.platform;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import pw.rxj.iron_quarry.data.ZComponents;
import pw.rxj.iron_quarry.util.ComplexEnergyContainer;

import java.util.function.Function;
import java.util.function.Supplier;

public class NeoForgePlatformEnergy implements IPlatformEnergy {
    private static final DataComponentType<Long> ENERGY_COMPONENT = ZComponents.register("energy", (builder) -> builder
            .persistent(Codec.LONG)
            .networkSynchronized(ByteBufCodecs.VAR_LONG));

    @Override
    public <T extends BlockEntity> void registerBlockEntity(Supplier<BlockEntityType<T>> blockEntityType, Function<T, ComplexEnergyContainer> energyContainer) {
    }

    @Override
    public DataComponentType<Long> getEnergyComponent() {
        return ENERGY_COMPONENT;
    }

    @Override
    public boolean canExtractEnergy(ItemStack stack) {
        return false;
    }

    @Override
    public boolean chargeFromSlot(Container container, int slot, ComplexEnergyContainer target) {
        return false;
    }
}
