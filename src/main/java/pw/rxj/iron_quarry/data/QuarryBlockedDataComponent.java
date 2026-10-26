package pw.rxj.iron_quarry.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.Block;
import pw.rxj.iron_quarry.util.ReadableString;

import java.util.Objects;

public class QuarryBlockedDataComponent {
    private static final StreamCodec<RegistryFriendlyByteBuf, Block> BLOCK_PACKET_CODEC = ByteBufCodecs.registry(Registries.BLOCK);

    private final Block block;
    private final BlockPos blockPos;

    private QuarryBlockedDataComponent(Block block, BlockPos blockPos) {
        this.block = block;
        this.blockPos = blockPos;
    }
    public static QuarryBlockedDataComponent of(Block block, BlockPos blockPos) {
        return new QuarryBlockedDataComponent(block, blockPos.immutable());
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        return o instanceof QuarryBlockedDataComponent other && this.block == other.block && this.blockPos.equals(other.blockPos);
    }
    @Override
    public int hashCode() {
        return Objects.hash(this.block, this.blockPos);
    }

    public Block getBlock() {
        return this.block;
    }
    public BlockPos getBlockPos() {
        return this.blockPos;
    }
    public MutableComponent getText() {
        return ReadableString.translatable("item.iron_quarry.warning.quarry_mining_denied", this.block.getName(), this.blockPos.getX(), this.blockPos.getY(), this.blockPos.getZ());
    }

    private static QuarryBlockedDataComponent read(RegistryFriendlyByteBuf buf) {
        return of(
                BLOCK_PACKET_CODEC.decode(buf),
                buf.readBlockPos()
        );
    }
    private void write(RegistryFriendlyByteBuf buf) {
        BLOCK_PACKET_CODEC.encode(buf, this.getBlock());
        buf.writeBlockPos(this.getBlockPos());
    }

    public static final Codec<QuarryBlockedDataComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(QuarryBlockedDataComponent::getBlock),
            BlockPos.CODEC.fieldOf("block_position").forGetter(QuarryBlockedDataComponent::getBlockPos)
            ).apply(instance, QuarryBlockedDataComponent::of)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, QuarryBlockedDataComponent> PACKET_CODEC = StreamCodec.ofMember(QuarryBlockedDataComponent::write, QuarryBlockedDataComponent::read);
}
