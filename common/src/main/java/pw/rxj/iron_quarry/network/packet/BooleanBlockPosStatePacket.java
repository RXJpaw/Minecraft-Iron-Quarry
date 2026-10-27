package pw.rxj.iron_quarry.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import pw.rxj.iron_quarry.network.ComplexPayloadHandler;

public class BooleanBlockPosStatePacket extends ComplexPayloadHandler<BooleanBlockPosStatePacket> {
    public final Boolean bool;
    public final BlockPos blockPos;
    public final BlockState blockState;

    protected BooleanBlockPosStatePacket(Boolean bool, BlockPos blockPos, BlockState blockState) {
        this.bool = bool;
        this.blockPos = blockPos;
        this.blockState = blockState;
    }
    protected BooleanBlockPosStatePacket(RegistryFriendlyByteBuf buf) {
        this.bool = buf.readBoolean();
        this.blockPos = buf.readBlockPos();
        this.blockState = Block.stateById(buf.readInt());
    }
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(bool);
        buf.writeBlockPos(blockPos);
        buf.writeInt(Block.getId(blockState));
    }
}