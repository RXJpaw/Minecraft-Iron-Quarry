package pw.rxj.iron_quarry.network.packet;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class ByteArrayPacket {
    public final byte[] bytes;

    private ByteArrayPacket(byte[] bytes) {
        this.bytes = bytes;
    }

    public static ByteArrayPacket read(PacketByteBuf buf) {
        try {
            byte[] bytes = buf.readByteArray();

            return new ByteArrayPacket(bytes);
        } catch(Exception ignored) {
            return null;
        }
    }
    public static PacketByteBuf write(Identifier channel, byte[] bytes) {
        PacketByteBuf packet = new PacketByteBuf(Unpooled.buffer());
        packet.writeIdentifier(channel);
        packet.writeByteArray(bytes);

        return packet;
    }
}
