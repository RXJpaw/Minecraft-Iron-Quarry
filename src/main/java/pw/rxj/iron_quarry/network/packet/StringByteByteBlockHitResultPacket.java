package pw.rxj.iron_quarry.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.phys.BlockHitResult;
import pw.rxj.iron_quarry.network.ComplexPayloadHandler;

public class StringByteByteBlockHitResultPacket extends ComplexPayloadHandler<StringByteByteBlockHitResultPacket> {

    public final String string;
    public final byte byte0;
    public final byte byte1;
    public final BlockHitResult hitResult;

    protected StringByteByteBlockHitResultPacket(String string, Byte byte0, Byte byte1, BlockHitResult hitResult) {
        this.string = string;
        this.byte0 = byte0;
        this.byte1 = byte1;
        this.hitResult = hitResult;
    }
    protected StringByteByteBlockHitResultPacket(RegistryFriendlyByteBuf buf) {
        this.string = buf.readUtf();
        this.byte0 = buf.readByte();
        this.byte1 = buf.readByte();
        this.hitResult = BlockHitResult.STREAM_CODEC.decode(buf);
    }
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(string);
        buf.writeByte(byte0);
        buf.writeByte(byte1);
        BlockHitResult.STREAM_CODEC.encode(buf, hitResult);
    }
}
