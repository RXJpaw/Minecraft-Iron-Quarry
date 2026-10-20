package pw.rxj.iron_quarry.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.screen.sync.ItemStackHash;
import net.minecraft.util.math.Direction;
import pw.rxj.iron_quarry.network.ComplexPayloadHandler;

public class DirectionByteHashPacket extends ComplexPayloadHandler<DirectionByteHashPacket> {
    public final Direction direction;
    public final byte data;
    public final ItemStackHash hash;

    protected DirectionByteHashPacket(Direction direction, byte data, ItemStackHash hash) {
        this.direction = direction;
        this.data = data;
        this.hash = hash;
    }
    protected DirectionByteHashPacket(RegistryByteBuf buf) {
        this.direction = Direction.byIndex(buf.readByte());
        this.data = buf.readByte();
        this.hash = ItemStackHash.PACKET_CODEC.decode(buf);
    }
    public void write(RegistryByteBuf buf) {
        buf.writeByte(direction.getIndex());
        buf.writeByte(data);
        ItemStackHash.PACKET_CODEC.encode(buf, hash);
    }
}
