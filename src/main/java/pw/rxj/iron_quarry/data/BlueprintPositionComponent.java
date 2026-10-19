package pw.rxj.iron_quarry.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.math.BlockPos;
import pw.rxj.iron_quarry.types.DualPosition;

public class BlueprintPositionComponent {
    private BlockPos pos1;
    private BlockPos pos2;

    private BlueprintPositionComponent(BlockPos pos1, BlockPos pos2) {
        this.pos1 = pos1;
        this.pos2 = pos2;
    }
    public static BlueprintPositionComponent of(BlockPos pos1, BlockPos pos2) {
        return new BlueprintPositionComponent(pos1, pos2);
    }
    public static BlueprintPositionComponent twin(BlockPos pos) {
        return of(pos, pos);
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

    protected void setPos1(BlockPos blockPos) {
        this.pos1 = blockPos;
    }
    protected void setPos2(BlockPos blockPos) {
        this.pos2 = blockPos;
    }
    public void setPosition(DualPosition position, BlockPos blockPos) {
        if(position == null) throw new AssertionError();

        switch (position) {
            case FIRST -> this.setPos1(blockPos);
            case SECOND -> this.setPos2(blockPos);
        }
    }

    private static BlueprintPositionComponent read(RegistryByteBuf buf) {
        return of(
                buf.readBlockPos(),
                buf.readBlockPos()
        );
    }
    private void write(RegistryByteBuf buf) {
        buf.writeBlockPos(this.getPos1());
        buf.writeBlockPos(this.getPos2());
    }

    public static Codec<BlueprintPositionComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos1").forGetter(BlueprintPositionComponent::getPos1),
            BlockPos.CODEC.fieldOf("pos2").forGetter(BlueprintPositionComponent::getPos2)
            ).apply(instance, BlueprintPositionComponent::of)
    );
    public static PacketCodec<RegistryByteBuf, BlueprintPositionComponent> PACKET_CODEC = PacketCodec.of(BlueprintPositionComponent::write, BlueprintPositionComponent::read);
}
