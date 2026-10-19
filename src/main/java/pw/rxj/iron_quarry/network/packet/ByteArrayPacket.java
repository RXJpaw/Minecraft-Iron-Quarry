package pw.rxj.iron_quarry.network.packet;

import net.minecraft.network.RegistryByteBuf;
import pw.rxj.iron_quarry.network.ComplexPayloadHandler;

public class ByteArrayPacket extends ComplexPayloadHandler<ByteArrayPacket> {
    public final byte[] bytes;

    protected ByteArrayPacket(byte[] bytes) {
        this.bytes = bytes;
    }
    protected ByteArrayPacket(RegistryByteBuf buf) {
        this.bytes = buf.readByteArray();
    }
    public void write(RegistryByteBuf buf) {
        buf.writeByteArray(bytes);
    }
}
