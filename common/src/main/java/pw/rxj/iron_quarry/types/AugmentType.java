package pw.rxj.iron_quarry.types;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import pw.rxj.iron_quarry.augment.AbstractAugment;
import pw.rxj.iron_quarry.augment.ZAugments;

import java.util.function.IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

public enum AugmentType implements StringRepresentable {
    EMPTY(0, "empty", ZAugments.EMPTY),
    SPEED(1, "speed", ZAugments.SPEED),
    FORTUNE(2, "fortune", ZAugments.FORTUNE),
    SILK_TOUCH(3, "silk_touch", ZAugments.SILK_TOUCH),
    CHEST_LOOTING(4, "chest_looting", ZAugments.CHEST_LOOTING);

    public static final Codec<AugmentType> CODEC = StringRepresentable.fromValues(AugmentType::values);
    public static final IntFunction<AugmentType> ID_TO_VALUE = ByIdMap.<AugmentType>continuous((value) -> value.id, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, AugmentType> PACKET_CODEC = ByteBufCodecs.idMapper(ID_TO_VALUE, (value) -> value.id);

    private final int id;
    private final String name;
    private final AbstractAugment augment;

    private AugmentType(int id, String name, AbstractAugment augment) {
        this.id = id;
        this.name = name;
        this.augment = augment;
    }

    public int getId(){
        return this.id;
    }
    public String getName(){
        return this.name;
    }
    public AbstractAugment getAugment() {
        return this.augment;
    }

    public boolean isEmpty() { return this.equals(AugmentType.EMPTY); }
    public boolean isPresent() { return !this.isEmpty(); }

    public static @NotNull AugmentType from(String name){
        for (AugmentType augmentType : values()) {
            if(augmentType.getName().equalsIgnoreCase(name)){
                return augmentType;
            }
        }

        return AugmentType.EMPTY;
    }

    @Override
    public String getSerializedName() {
        return this.getName();
    }
}
