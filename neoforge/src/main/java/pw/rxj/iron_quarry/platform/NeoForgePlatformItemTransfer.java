package pw.rxj.iron_quarry.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class NeoForgePlatformItemTransfer implements IPlatformItemTransfer {
    @Override
    public <T extends BlockEntity> void registerBlockEntity(Supplier<BlockEntityType<T>> blockEntityType, BiFunction<T, Direction, @Nullable Container> container) {
    }

    @Override
    public void pushToNeighbor(Level world, BlockPos pos, Direction direction, Container inventory) {
    }
}
