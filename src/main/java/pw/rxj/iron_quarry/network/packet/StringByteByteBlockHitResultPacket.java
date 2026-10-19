package pw.rxj.iron_quarry.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.util.hit.BlockHitResult;
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
    protected StringByteByteBlockHitResultPacket(RegistryByteBuf buf) {
        this.string = buf.readString();
        this.byte0 = buf.readByte();
        this.byte1 = buf.readByte();
        this.hitResult = buf.readBlockHitResult();
    }
    public void write(RegistryByteBuf buf) {
        buf.writeString(string);
        buf.writeByte(byte0);
        buf.writeByte(byte1);
        buf.writeBlockHitResult(hitResult);
    }
}
