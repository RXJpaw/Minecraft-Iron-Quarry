package pw.rxj.iron_quarry.platform;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.util.ComplexEnergyContainer;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;
import team.reborn.energy.impl.EnergyImpl;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public class FabricPlatformEnergy implements IPlatformEnergy {
    private static final Map<ComplexEnergyContainer, SidedEnergyStorage> SIDED_STORAGES = Collections.synchronizedMap(new WeakHashMap<>());

    @Override
    public <T extends BlockEntity> void registerBlockEntity(Supplier<BlockEntityType<T>> blockEntityType, Function<T, ComplexEnergyContainer> energyContainer) {
        EnergyStorage.SIDED.registerForBlockEntity((blockEntity, direction) -> getSideStorage(energyContainer.apply(blockEntity), direction), blockEntityType.get());
    }

    @Override
    public DataComponentType<Long> getEnergyComponent() {
        return EnergyImpl.ENERGY_COMPONENT;
    }

    @Override
    public boolean canExtractEnergy(ItemStack stack) {
        EnergyStorage energyStorage = ContainerItemContext.withConstant(stack).find(EnergyStorage.ITEM);

        return energyStorage != null && energyStorage.supportsExtraction();
    }

    @Override
    public boolean chargeFromSlot(Container container, int slot, ComplexEnergyContainer target) {
        EnergyStorage energyStorage = ContainerItemContext.ofSingleSlot(ContainerStorage.of(container, null).getSlot(slot)).find(EnergyStorage.ITEM);
        if(energyStorage == null || energyStorage.getAmount() <= 0) return false;

        EnergyStorageUtil.move(energyStorage, getSideStorage(target, null), Long.MAX_VALUE, null);

        return true;
    }

    private static EnergyStorage getSideStorage(ComplexEnergyContainer container, @Nullable Direction side) {
        return SIDED_STORAGES.computeIfAbsent(container, SidedEnergyStorage::new).getSideStorage(side);
    }

    private static class SidedEnergyStorage extends SnapshotParticipant<Long> {
        private final ComplexEnergyContainer container;
        private final Map<Direction, SideStorage> sideStorages = new EnumMap<>(Direction.class);
        private final SideStorage nullSideStorage;

        private SidedEnergyStorage(ComplexEnergyContainer container) {
            this.container = container;
            this.nullSideStorage = new SideStorage(null);

            for (Direction direction : Direction.values()) {
                this.sideStorages.put(direction, new SideStorage(direction));
            }
        }

        private SideStorage getSideStorage(@Nullable Direction side) {
            return side == null ? this.nullSideStorage : this.sideStorages.get(side);
        }

        @Override
        protected Long createSnapshot() {
            return this.container.getStored();
        }

        @Override
        protected void readSnapshot(Long snapshot) {
            this.container.read(snapshot);
        }

        @Override
        protected void onFinalCommit() {
            this.container.onFinalCommit();
        }

        private class SideStorage implements EnergyStorage {
            private final @Nullable Direction side;

            private SideStorage(@Nullable Direction side) {
                this.side = side;
            }

            @Override
            public boolean supportsInsertion() {
                return container.getMaxInsert(this.side) > 0;
            }

            @Override
            public long insert(long maxAmount, TransactionContext transaction) {
                StoragePreconditions.notNegative(maxAmount);

                long inserted = Math.min(container.getMaxInsert(this.side), Math.min(maxAmount, container.getCapacity() - container.getStored()));
                if(inserted <= 0) return 0;

                updateSnapshots(transaction);
                container.read(container.getStored() + inserted);

                return inserted;
            }

            @Override
            public boolean supportsExtraction() {
                return container.getMaxExtract(this.side) > 0;
            }

            @Override
            public long extract(long maxAmount, TransactionContext transaction) {
                StoragePreconditions.notNegative(maxAmount);

                long extracted = Math.min(container.getMaxExtract(this.side), Math.min(maxAmount, container.getStored()));
                if(extracted <= 0) return 0;

                updateSnapshots(transaction);
                container.read(container.getStored() - extracted);

                return extracted;
            }

            @Override
            public long getAmount() {
                return container.getStored();
            }

            @Override
            public long getCapacity() {
                return container.getCapacity();
            }
        }
    }
}
