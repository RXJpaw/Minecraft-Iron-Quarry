package pw.rxj.iron_quarry.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.util.math.Direction;
import pw.rxj.iron_quarry.network.ComplexPayloadHandler;

public class DirectionBytePacket extends ComplexPayloadHandler<DirectionBytePacket> {
    public final Direction direction;
    public final byte data;

    protected DirectionBytePacket(Direction direction, byte data) {
        this.direction = direction;
        this.data = data;
    }
    protected DirectionBytePacket(RegistryByteBuf buf) {
        this.direction = Direction.byIndex(buf.readByte());
        this.data = buf.readByte();
    }
    public void write(RegistryByteBuf buf) {
        buf.writeByte(direction.getIndex());
        buf.writeByte(data);
    }
}