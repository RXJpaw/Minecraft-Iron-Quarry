package pw.rxj.iron_quarry.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import pw.rxj.iron_quarry.types.DualPosition;

public record BlueprintPositionComponent(BlockPos pos1, BlockPos pos2) {
    public static BlueprintPositionComponent of(BlockPos pos1, BlockPos pos2) {
        return new BlueprintPositionComponent(pos1.immutable(), pos2.immutable());
    }
    public static BlueprintPositionComponent twin(BlockPos pos) {
        return of(pos, pos);
    }

    public BlockPos getPosition(DualPosition position) {
        if(position == null) throw new AssertionError();

        return switch (position) {
            case FIRST -> this.pos1;
            case SECOND -> this.pos2;
        };
    }

    public BlueprintPositionComponent withPosition(DualPosition position, BlockPos blockPos) {
        if(position == null) throw new AssertionError();

        return switch (position) {
            case FIRST -> of(blockPos, this.pos2);
            case SECOND -> of(this.pos1, blockPos);
        };
    }

    private static BlueprintPositionComponent read(RegistryFriendlyByteBuf buf) {
        return of(
                buf.readBlockPos(),
                buf.readBlockPos()
        );
    }
    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos1);
        buf.writeBlockPos(this.pos2);
    }

    public static final Codec<BlueprintPositionComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos1").forGetter(BlueprintPositionComponent::pos1),
            BlockPos.CODEC.fieldOf("pos2").forGetter(BlueprintPositionComponent::pos2)
            ).apply(instance, BlueprintPositionComponent::of)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, BlueprintPositionComponent> PACKET_CODEC = StreamCodec.ofMember(BlueprintPositionComponent::write, BlueprintPositionComponent::read);
}
