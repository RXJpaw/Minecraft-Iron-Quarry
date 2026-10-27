package pw.rxj.iron_quarry.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BlockPosState {
    public final BlockState blockState;
    public final BlockPos blockPos;

    private BlockPosState(BlockState blockState, BlockPos blockPos) {
        this.blockState = blockState;
        this.blockPos = blockPos;
    }
    public static BlockPosState of(BlockState blockState, BlockPos blockPos) {
        return new BlockPosState(blockState, blockPos);
    }
}
