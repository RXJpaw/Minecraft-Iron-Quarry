package pw.rxj.iron_quarry.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import pw.rxj.iron_quarry.types.DualPosition;

import java.util.Objects;

public class BlueprintPositionComponent {
    private final BlockPos pos1;
    private final BlockPos pos2;

    private BlueprintPositionComponent(BlockPos pos1, BlockPos pos2) {
        this.pos1 = pos1;
        this.pos2 = pos2;
    }
    public static BlueprintPositionComponent of(BlockPos pos1, BlockPos pos2) {
        return new BlueprintPositionComponent(pos1.immutable(), pos2.immutable());
    }
    public static BlueprintPositionComponent twin(BlockPos pos) {
        return of(pos, pos);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        return o instanceof BlueprintPositionComponent other && this.pos1.equals(other.pos1) && this.pos2.equals(other.pos2);
    }
    @Override
    public int hashCode() {
        return Objects.hash(this.pos1, this.pos2);
    }

    protected BlockPos getPos1() {
        return this.pos1;
    }
    protected BlockPos getPos2() {
        return this.pos2;
    }
    public BlockPos getPosition(DualPosition position) {
        if(position == null) throw new AssertionError();

        return switch (position) {
            case FIRST -> this.getPos1();
            case SECOND -> this.getPos2();
        };
    }

    public BlueprintPositionComponent withPosition(DualPosition position, BlockPos blockPos) {
        if(position == null) throw new AssertionError();

        return switch (position) {
            case FIRST -> of(blockPos, this.getPos2());
            case SECOND -> of(this.getPos1(), blockPos);
        };
    }

    private static BlueprintPositionComponent read(RegistryFriendlyByteBuf buf) {
        return of(
                buf.readBlockPos(),
                buf.readBlockPos()
        );
    }
    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(this.getPos1());
        buf.writeBlockPos(this.getPos2());
    }

    public static final Codec<BlueprintPositionComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos1").forGetter(BlueprintPositionComponent::getPos1),
            BlockPos.CODEC.fieldOf("pos2").forGetter(BlueprintPositionComponent::getPos2)
            ).apply(instance, BlueprintPositionComponent::of)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, BlueprintPositionComponent> PACKET_CODEC = StreamCodec.ofMember(BlueprintPositionComponent::write, BlueprintPositionComponent::read);
}
