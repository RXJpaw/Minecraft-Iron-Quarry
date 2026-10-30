package pw.rxj.iron_quarry.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class NeoForgePlatformItemTransfer implements IPlatformItemTransfer {
    private static final List<Consumer<RegisterCapabilitiesEvent>> BLOCK_ENTITY_CAPABILITIES = new ArrayList<>();

    @Override
    public <T extends BlockEntity> void registerBlockEntity(Supplier<BlockEntityType<T>> blockEntityType, BiFunction<T, Direction, @Nullable Container> container) {
        BLOCK_ENTITY_CAPABILITIES.add(event -> event.registerBlockEntity(Capabilities.Item.BLOCK, blockEntityType.get(), (blockEntity, side) -> {
            Container inventory = container.apply(blockEntity, side);

            return inventory == null ? null : VanillaContainerWrapper.of(inventory);
        }));
    }

    @Override
    public void pushToNeighbor(Level world, BlockPos pos, Direction direction, Container inventory) {
        ResourceHandler<ItemResource> itemHandler = world.getCapability(Capabilities.Item.BLOCK, pos.relative(direction), direction.getOpposite());
        if(itemHandler == null) return;

        try (Transaction transaction = Transaction.openRoot()) {
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack itemStack = inventory.getItem(i);
                if(itemStack.isEmpty()) continue;

                int insertedCount = itemHandler.insert(ItemResource.of(itemStack), itemStack.getCount(), transaction);
                inventory.removeItem(i, insertedCount);
            }

            transaction.commit();
        }
    }

    @Override
    public void notifyLinkedContainersChanged(BlockEntity blockEntity) {
        blockEntity.invalidateCapabilities();
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        BLOCK_ENTITY_CAPABILITIES.forEach(capability -> capability.accept(event));
    }
}
