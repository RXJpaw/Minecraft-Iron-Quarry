package pw.rxj.iron_quarry.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.render.RenderUtil;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class BlockActress {
    public static int DEFAULT_MAX_UPDATE_DEPTH = 512;
    public static int DEFAULT_FLAGS = 3;

    public final ServerLevel serverWorld;
    public final @NotNull LevelChunk worldChunk;
    public final BlockPos blockPos;
    private final boolean trackBlockEntity;
    private final int maxUpdateDepth;
    private final int flags;

    public BlockEntity blockEntity;
    public BlockState blockState;
    public Block block;

    private BlockActress(ServerLevel serverWorld, @NotNull LevelChunk worldChunk, BlockPos blockPos, boolean trackBlockEntity, int maxUpdateDepth, int flags) {
        this.serverWorld = serverWorld;
        this.worldChunk = worldChunk;
        this.blockPos = blockPos;
        this.trackBlockEntity = trackBlockEntity;
        this.maxUpdateDepth = maxUpdateDepth;
        this.flags = flags;

        //set BlockEntity (if enabled), BlockState and Block
        this.updateBlockState(worldChunk.getBlockState(blockPos));
    }
    public static BlockActress bake(ServerLevel serverWorld, @NotNull LevelChunk worldChunk, BlockPos blockPos, boolean trackBlockEntity, int maxUpdateDepth, int flags) {
        return new BlockActress(serverWorld, worldChunk, blockPos, trackBlockEntity, maxUpdateDepth, flags);
    }
    public static Optional<BlockActress> of(ServerLevel serverWorld, BlockPos blockPos) {
        @Nullable LevelChunk worldChunk = serverWorld.getChunkSource().getChunkNow(blockPos.getX() >> 4, blockPos.getZ() >> 4);
        if(worldChunk == null) return Optional.empty();

        return bake(serverWorld, worldChunk, blockPos, false, DEFAULT_MAX_UPDATE_DEPTH, DEFAULT_FLAGS).describeConstable();
    }
    public static @Nullable BlockActress ofOrNull(ServerLevel serverWorld, BlockPos blockPos){
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
        boolean success = this.serverWorld.setBlock(this.blockPos, blockState, this.flags, this.maxUpdateDepth);
        if(success) this.updateBlockState(blockState);

        return success;
    }
    public boolean setBlockState(Block block) {
        return this.setBlockState(block.defaultBlockState());
    }

    public boolean isFluidStateEmpty() {
        return this.blockState.getFluidState().isEmpty();
    }
    public boolean isFluidStatePresent() {
        return !this.isFluidStateEmpty();
    }
    public boolean isUnbreakable() {
        return this.block.defaultDestroyTime() < 0;
    }
    public boolean isAir() {
        return this.blockState.isAir();
    }

    public List<ItemStack> getDroppedStacks(BlockState originBlockState, BlockPos originBlockPos, ItemStack miningTool) {
        Vec3 originVec3d = RenderUtil.vec3dFrom(originBlockPos);

        return this.blockState.getDrops(
                new LootParams.Builder(this.serverWorld)
                        .withParameter(LootContextParams.BLOCK_STATE, originBlockState)
                        .withParameter(LootContextParams.ORIGIN, originVec3d)
                        .withParameter(LootContextParams.TOOL, miningTool)
        );
    }
    public boolean isCorrectForDrops(Tool toolComponent) {
        return !this.blockState.requiresCorrectToolForDrops() || toolComponent.isCorrectForDrops(this.blockState);
    }

    public BlockPosState toBlockPosState() {
        return BlockPosState.of(this.blockState, this.blockPos);
    }

    public Optional<BlockActress> describeConstable() {
        return Optional.of(this);
    }
}
