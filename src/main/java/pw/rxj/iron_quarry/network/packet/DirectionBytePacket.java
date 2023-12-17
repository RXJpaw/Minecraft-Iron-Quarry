package pw.rxj.iron_quarry.network.packet;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public class DirectionBytePacket {
    public final Direction direction;
    public final byte data;

    private DirectionBytePacket(Direction direction, byte data) {
        this.direction = direction;
        this.data = data;
    }

    public static DirectionBytePacket read(PacketByteBuf buf) {
        try {
            Direction direction = Direction.byId(buf.readByte());
            byte data = buf.readByte();

            return new DirectionBytePacket(direction, data);
        } catch(Exception ignored) {
            return null;
        }
    }
    public static PacketByteBuf write(Identifier channel, Direction direction, byte data) {
        PacketByteBuf packet = new PacketByteBuf(Unpooled.buffer());
        packet.writeIdentifier(channel);
        packet.writeByte(direction.getId());
        packet.writeByte(data);

        return packet;
    }
}