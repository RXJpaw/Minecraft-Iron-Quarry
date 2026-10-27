package pw.rxj.iron_quarry.platform;

import com.mojang.serialization.Codec;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.data.ZComponents;
import pw.rxj.iron_quarry.util.ComplexEnergyContainer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NeoForgePlatformEnergy implements IPlatformEnergy {
    private static final DataComponentType<Long> ENERGY_COMPONENT = ZComponents.register("energy", (builder) -> builder
            .persistent(Codec.LONG)
            .networkSynchronized(ByteBufCodecs.VAR_LONG));
    private static final List<Consumer<RegisterCapabilitiesEvent>> BLOCK_ENTITY_CAPABILITIES = new ArrayList<>();

    @Override
    public <T extends BlockEntity> void registerBlockEntity(Supplier<BlockEntityType<T>> blockEntityType, Function<T, ComplexEnergyContainer> energyContainer) {
        BLOCK_ENTITY_CAPABILITIES.add(event -> event.registerBlockEntity(Capabilities.Energy.BLOCK, blockEntityType.get(), (blockEntity, side) -> getSideHandler(energyContainer.apply(blockEntity), side)));
    }

    @Override
    public DataComponentType<Long> getEnergyComponent() {
        return ENERGY_COMPONENT;
    }

    @Override
    public boolean canExtractEnergy(ItemStack stack) {
        return ItemAccess.forStack(stack).getCapability(Capabilities.Energy.ITEM) != null;
    }

    @Override
    public boolean chargeFromSlot(Container container, int slot, ComplexEnergyContainer target) {
        EnergyHandler energyHandler = ItemAccess.forHandlerIndex(VanillaContainerWrapper.of(container), slot).getCapability(Capabilities.Energy.ITEM);
        if(energyHandler == null || energyHandler.getAmountAsLong() <= 0) return false;

        EnergyHandlerUtil.move(energyHandler, getSideHandler(target, null), Integer.MAX_VALUE, null);

        return true;
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        BLOCK_ENTITY_CAPABILITIES.forEach(capability -> capability.accept(event));
    }

    private static EnergyHandler getSideHandler(ComplexEnergyContainer container, @Nullable Direction side) {
        return container.getPlatformStorage(SidedEnergyHandler::new).getSideHandler(side);
    }

    private static class SidedEnergyHandler extends SnapshotJournal<Long> {
        private final ComplexEnergyContainer container;
        private final Map<Direction, SideHandler> sideHandlers = new EnumMap<>(Direction.class);
        private final SideHandler nullSideHandler;

        private SidedEnergyHandler(ComplexEnergyContainer container) {
            this.container = container;
            this.nullSideHandler = new SideHandler(null);

            for (Direction direction : Direction.values()) {
                this.sideHandlers.put(direction, new SideHandler(direction));
            }
        }

        private SideHandler getSideHandler(@Nullable Direction side) {
            return side == null ? this.nullSideHandler : this.sideHandlers.get(side);
        }

        @Override
        protected Long createSnapshot() {
            return this.container.getStored();
        }

        @Override
        protected void revertToSnapshot(Long snapshot) {
            this.container.read(snapshot);
        }

        @Override
        protected void onRootCommit(Long originalState) {
            if(this.container.getStored() != originalState) this.container.onFinalCommit();
        }

        private class SideHandler implements EnergyHandler {
            private final @Nullable Direction side;

            private SideHandler(@Nullable Direction side) {
                this.side = side;
            }

            @Override
            public long getAmountAsLong() {
                return container.getStored();
            }

            @Override
            public long getCapacityAsLong() {
                return container.getCapacity();
            }

            @Override
            public int insert(int amount, TransactionContext transaction) {
                TransferPreconditions.checkNonNegative(amount);

                long inserted = Math.min(container.getMaxInsert(this.side), Math.min(amount, container.getCapacity() - container.getStored()));
                if(inserted <= 0) return 0;

                updateSnapshots(transaction);
                container.read(container.getStored() + inserted);

                return (int) inserted;
            }

            @Override
            public int extract(int amount, TransactionContext transaction) {
                TransferPreconditions.checkNonNegative(amount);

                long extracted = Math.min(container.getMaxExtract(this.side), Math.min(amount, container.getStored()));
                if(extracted <= 0) return 0;

                updateSnapshots(transaction);
                container.read(container.getStored() - extracted);

                return (int) extracted;
            }
        }
    }
}
