package pw.rxj.iron_quarry.records;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public record BlockPosState(BlockPos blockPos, BlockState blockState) {
}
