package pw.rxj.iron_quarry.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.render.RenderUtil;

import java.util.List;
import java.util.Optional;

public class BlockActress {
    public static int DEFAULT_MAX_UPDATE_DEPTH = 512;
    public static int DEFAULT_FLAGS = 3;

    public final ServerWorld serverWorld;
    public final @NotNull WorldChunk worldChunk;
    public final BlockPos blockPos;
    private final boolean trackBlockEntity;
    private final int maxUpdateDepth;
    private final int flags;

    public BlockEntity blockEntity;
    public BlockState blockState;
    public Block block;

    private BlockActress(ServerWorld serverWorld, @NotNull WorldChunk worldChunk, BlockPos blockPos, boolean trackBlockEntity, int maxUpdateDepth, int flags) {
        this.serverWorld = serverWorld;
        this.worldChunk = worldChunk;
        this.blockPos = blockPos;
        this.trackBlockEntity = trackBlockEntity;
        this.maxUpdateDepth = maxUpdateDepth;
        this.flags = flags;

        //set BlockEntity (if enabled), BlockState and Block
        this.updateBlockState(worldChunk.getBlockState(blockPos));
    }
    public static BlockActress bake(ServerWorld serverWorld, @NotNull WorldChunk worldChunk, BlockPos blockPos, boolean trackBlockEntity, int maxUpdateDepth, int flags) {
        return new BlockActress(serverWorld, worldChunk, blockPos, trackBlockEntity, maxUpdateDepth, flags);
    }
    public static Optional<BlockActress> of(ServerWorld serverWorld, BlockPos blockPos) {
        @Nullable WorldChunk worldChunk = serverWorld.getChunkManager().getWorldChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4);
        if(worldChunk == null) return Optional.empty();

        return bake(serverWorld, worldChunk, blockPos, false, DEFAULT_MAX_UPDATE_DEPTH, DEFAULT_FLAGS).describeConstable();
    }
    public static @Nullable BlockActress ofOrNull(ServerWorld serverWorld, BlockPos blockPos){
        return of(serverWorld, blockPos).orElse(null);
    }

    public BlockActress with(boolean trackBlockEntity, int maxUpdateDepth, int flags) {
        return bake(this.serverWorld, this.worldChunk, this.blockPos, trackBlockEntity, maxUpdateDepth, flags);
    }
    public BlockActress copy() {
        return bake(this.serverWorld, this.worldChunk, this.blockPos, this.trackBlockEntity, this.maxUpdateDepth, this.flags);
    }

    private void updateBlockState(BlockState blockState) {
        if(this.trackBlockEntity && blockState.hasBlockEntity()) {
            this.blockEntity = this.worldChunk.getBlockEntity(this.blockPos);
        } else {
            this.blockEntity = null;
        }

        this.blockState = blockState;
        this.block = blockState.getBlock();
    }
    public boolean setBlockState(BlockState blockState) {
        boolean success = this.serverWorld.setBlockState(this.blockPos, blockState, this.flags, this.maxUpdateDepth);
        if(success) this.updateBlockState(blockState);

        return success;
    }
    public boolean setBlockState(Block block) {
        return this.setBlockState(block.getDefaultState());
    }

    public boolean isFluidStateEmpty() {
        return this.blockState.getFluidState().isEmpty();
    }
    public boolean isFluidStatePresent() {
        return !this.isFluidStateEmpty();
    }
    public boolean isUnbreakable() {
        return this.block.getHardness() < 0;
    }
    public boolean isAir() {
        return this.blockState.isAir();
    }

    public List<ItemStack> getDroppedStacks(BlockState originBlockState, BlockPos originBlockPos, ItemStack miningTool) {
        Vec3d originVec3d = RenderUtil.vec3dFrom(originBlockPos);

        return this.blockState.getDroppedStacks(
                new LootContext.Builder(this.serverWorld)
                        .parameter(LootContextParameters.BLOCK_STATE, originBlockState)
                        .parameter(LootContextParameters.ORIGIN, originVec3d)
                        .parameter(LootContextParameters.TOOL, miningTool)
        );
    }

    public BlockPosState toBlockPosState() {
        return BlockPosState.of(this.blockState, this.blockPos);
    }

    public Optional<BlockActress> describeConstable() {
        return Optional.of(this);
    }
}
