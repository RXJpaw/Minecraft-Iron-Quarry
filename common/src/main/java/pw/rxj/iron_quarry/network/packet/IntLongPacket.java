package pw.rxj.iron_quarry.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import pw.rxj.iron_quarry.network.ComplexPayloadHandler;

public class IntLongPacket extends ComplexPayloadHandler<IntLongPacket> {
    public final int intValue;
    public final long longValue;

    protected IntLongPacket(int intValue, long longValue) {
        this.intValue = intValue;
        this.longValue = longValue;
    }
    protected IntLongPacket(RegistryFriendlyByteBuf buf) {
        this.intValue = buf.readVarInt();
        this.longValue = buf.readVarLong();
    }
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(intValue);
        buf.writeVarLong(longValue);
    }
}
