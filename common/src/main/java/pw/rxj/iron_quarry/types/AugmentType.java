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
    EMPTY(0, "empty"),
    SPEED(1, "speed"),
    FORTUNE(2, "fortune"),
    SILK_TOUCH(3, "silk_touch"),
    CHEST_LOOTING(4, "chest_looting");

    public static final Codec<AugmentType> CODEC = StringRepresentable.fromValues(AugmentType::values);
    public static final IntFunction<AugmentType> ID_TO_VALUE = ByIdMap.<AugmentType>continuous((value) -> value.id, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, AugmentType> PACKET_CODEC = ByteBufCodecs.idMapper(ID_TO_VALUE, (value) -> value.id);

    private final int id;
    private final String name;

    private AugmentType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId(){
        return this.id;
    }
    public String getName(){
        return this.name;
    }
    public AbstractAugment getAugment() {
        return switch (this) {
            case EMPTY -> ZAugments.EMPTY;
            case SPEED -> ZAugments.SPEED;
            case FORTUNE -> ZAugments.FORTUNE;
            case SILK_TOUCH -> ZAugments.SILK_TOUCH;
            case CHEST_LOOTING -> ZAugments.CHEST_LOOTING;
        };
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
