package pw.rxj.iron_quarry.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.HashedStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import pw.rxj.iron_quarry.network.ComplexPayloadHandler;

public class ByteBlockPosHashPacket extends ComplexPayloadHandler<ByteBlockPosHashPacket> {
    public final byte data;
    public final BlockPos blockPos;
    public final HashedStack hash;

    protected ByteBlockPosHashPacket(byte data, BlockPos blockPos, HashedStack hash) {
        this.data = data;
        this.blockPos = blockPos;
        this.hash = hash;
    }
    protected ByteBlockPosHashPacket(RegistryFriendlyByteBuf buf) {
        this.data = buf.readByte();
        this.blockPos = buf.readBlockPos();
        this.hash = HashedStack.STREAM_CODEC.decode(buf);
    }
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeByte(data);
        buf.writeBlockPos(blockPos);
        HashedStack.STREAM_CODEC.encode(buf, hash);
    }
}
