package pw.rxj.iron_quarry.network.packet;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.util.math.BlockPos;
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
    protected BooleanBlockPosStatePacket(RegistryByteBuf buf) {
        this.bool = buf.readBoolean();
        this.blockPos = buf.readBlockPos();
        this.blockState = Block.getStateFromRawId(buf.readInt());
    }
    public void write(RegistryByteBuf buf) {
        buf.writeBoolean(bool);
        buf.writeBlockPos(blockPos);
        buf.writeInt(Block.getRawIdFromState(blockState));
    }
}