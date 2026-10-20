package pw.rxj.iron_quarry.data;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;

@Getter
public class QuarryBlockScreenData {
    private final Block block;
    private final BlockPos blockPos;

    private QuarryBlockScreenData(Block block, BlockPos blockPos) {
        this.block = block;
        this.blockPos = blockPos;
    }
    public static QuarryBlockScreenData of(Block block, BlockPos blockPos) {
        return new QuarryBlockScreenData(block, blockPos);
    }

    private static QuarryBlockScreenData read(RegistryFriendlyByteBuf buf) {
        return of(
                BuiltInRegistries.BLOCK.getValue(buf.readIdentifier()),
                buf.readBlockPos()
        );
    }
    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeIdentifier(BuiltInRegistries.BLOCK.getKey(this.block));
        buf.writeBlockPos(this.blockPos);
    }

    public static StreamCodec<RegistryFriendlyByteBuf, QuarryBlockScreenData> PACKET_CODEC = StreamCodec.ofMember(QuarryBlockScreenData::write, QuarryBlockScreenData::read);
}
