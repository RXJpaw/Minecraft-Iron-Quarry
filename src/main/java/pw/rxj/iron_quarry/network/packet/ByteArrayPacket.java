package pw.rxj.iron_quarry.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import pw.rxj.iron_quarry.network.ComplexPayloadHandler;

public class ByteArrayPacket extends ComplexPayloadHandler<ByteArrayPacket> {
    public final byte[] bytes;

    protected ByteArrayPacket(byte[] bytes) {
        this.bytes = bytes;
    }
    protected ByteArrayPacket(RegistryFriendlyByteBuf buf) {
        this.bytes = buf.readByteArray();
    }
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeByteArray(bytes);
    }
}
