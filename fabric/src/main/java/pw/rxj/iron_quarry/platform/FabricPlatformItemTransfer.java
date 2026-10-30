package pw.rxj.iron_quarry.platform;

import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class FabricPlatformItemTransfer implements IPlatformItemTransfer {
    @Override
    public <T extends BlockEntity> void registerBlockEntity(Supplier<BlockEntityType<T>> blockEntityType, BiFunction<T, Direction, @Nullable Container> container) {
        ItemStorage.SIDED.registerForBlockEntity((blockEntity, direction) -> {
            Container inventory = container.apply(blockEntity, direction);

            return inventory == null ? null : ContainerStorage.of(inventory, null);
        }, blockEntityType.get());
    }

    @Override
    public void pushToNeighbor(Level world, BlockPos pos, Direction direction, Container inventory) {
        Transaction transaction = Transaction.openOuter();

        Storage<ItemVariant> itemStorage = ItemStorage.SIDED.find(world, pos.offset(direction.getUnitVec3i()), direction.getOpposite());
        if(itemStorage != null) {
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack itemStack = inventory.getItem(i);
                if(itemStack.isEmpty()) continue;

                long insertedCount = itemStorage.insert(ItemVariant.of(itemStack), itemStack.getCount(), transaction);
                inventory.removeItem(i, (int) insertedCount);
            }
        }

        transaction.commit();
    }

    @Override
    public void notifyLinkedContainersChanged(BlockEntity blockEntity) {
    }
}
