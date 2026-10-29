package pw.rxj.iron_quarry.data;

import io.netty.handler.codec.DecoderException;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import pw.rxj.iron_quarry.block.QuarryBlock;

@Getter
public class QuarryBlockScreenData {
    private final QuarryBlock quarryBlock;
    private final BlockPos blockPos;

    private QuarryBlockScreenData(QuarryBlock quarryBlock, BlockPos blockPos) {
        this.quarryBlock = quarryBlock;
        this.blockPos = blockPos;
    }
    public static QuarryBlockScreenData of(QuarryBlock quarryBlock, BlockPos blockPos) {
        return new QuarryBlockScreenData(quarryBlock, blockPos);
    }

    private static QuarryBlockScreenData read(RegistryFriendlyByteBuf buf) {
        Identifier blockId = buf.readIdentifier();
        if(!(BuiltInRegistries.BLOCK.getValue(blockId) instanceof QuarryBlock quarryBlock)) throw new DecoderException("Unknown quarry block: " + blockId);

        return of(quarryBlock, buf.readBlockPos());
    }
    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeIdentifier(BuiltInRegistries.BLOCK.getKey(this.quarryBlock));
        buf.writeBlockPos(this.blockPos);
    }

    public static StreamCodec<RegistryFriendlyByteBuf, QuarryBlockScreenData> PACKET_CODEC = StreamCodec.ofMember(QuarryBlockScreenData::write, QuarryBlockScreenData::read);
}
