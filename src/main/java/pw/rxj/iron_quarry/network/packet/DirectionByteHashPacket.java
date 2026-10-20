package pw.rxj.iron_quarry.network.packet;

import net.minecraft.core.Direction;
import net.minecraft.network.HashedStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import pw.rxj.iron_quarry.network.ComplexPayloadHandler;

public class DirectionByteHashPacket extends ComplexPayloadHandler<DirectionByteHashPacket> {
    public final Direction direction;
    public final byte data;
    public final HashedStack hash;

    protected DirectionByteHashPacket(Direction direction, byte data, HashedStack hash) {
        this.direction = direction;
        this.data = data;
        this.hash = hash;
    }
    protected DirectionByteHashPacket(RegistryFriendlyByteBuf buf) {
        this.direction = Direction.from3DDataValue(buf.readByte());
        this.data = buf.readByte();
        this.hash = HashedStack.STREAM_CODEC.decode(buf);
    }
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeByte(direction.get3DDataValue());
        buf.writeByte(data);
        HashedStack.STREAM_CODEC.encode(buf, hash);
    }
}
