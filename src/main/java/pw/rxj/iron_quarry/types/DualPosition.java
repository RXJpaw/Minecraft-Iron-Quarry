package pw.rxj.iron_quarry.types;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;

import java.util.function.IntFunction;

public enum DualPosition implements StringIdentifiable {
    FIRST(0, "first_position"),
    SECOND(1, "second_position");

    public static final Codec<DualPosition> CODEC = StringIdentifiable.createBasicCodec(DualPosition::values);
    public static final IntFunction<DualPosition> ID_TO_VALUE = ValueLists.<DualPosition>createIndexToValueFunction((value) -> value.id, values(), ValueLists.OutOfBoundsHandling.ZERO);
    public static final PacketCodec<ByteBuf, DualPosition> PACKET_CODEC = PacketCodecs.indexed(ID_TO_VALUE, (value) -> value.id);

    private final int id;
    private final String name;

    private DualPosition(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
            return id;
        }

    public String getName() {
        return this.name;
    }

    @Override
    public String asString() {
        return this.getName();
    }
}
