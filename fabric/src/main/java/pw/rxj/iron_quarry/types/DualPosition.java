package pw.rxj.iron_quarry.types;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

public enum DualPosition implements StringRepresentable {
    FIRST(0, "first_position"),
    SECOND(1, "second_position");

    public static final Codec<DualPosition> CODEC = StringRepresentable.fromValues(DualPosition::values);
    public static final IntFunction<DualPosition> ID_TO_VALUE = ByIdMap.<DualPosition>continuous((value) -> value.id, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, DualPosition> PACKET_CODEC = ByteBufCodecs.idMapper(ID_TO_VALUE, (value) -> value.id);

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
    public String getSerializedName() {
        return this.getName();
    }
}
