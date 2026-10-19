package pw.rxj.iron_quarry.data;

import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;

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

    private static QuarryBlockScreenData read(RegistryByteBuf buf) {
        return of(
                Registries.BLOCK.get(buf.readIdentifier()),
                buf.readBlockPos()
        );
    }
    private void write(RegistryByteBuf buf) {
        buf.writeIdentifier(Registries.BLOCK.getId(this.block));
        buf.writeBlockPos(this.blockPos);
    }

    public static PacketCodec<RegistryByteBuf, QuarryBlockScreenData> PACKET_CODEC = PacketCodec.of(QuarryBlockScreenData::write, QuarryBlockScreenData::read);
}
