package pw.rxj.iron_quarry.util;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

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
