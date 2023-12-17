package pw.rxj.iron_quarry.network.packet;

import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class BooleanBlockPosStatePacket {
    public final Boolean bool;
    public final BlockPos blockPos;
    public final BlockState blockState;

    private BooleanBlockPosStatePacket(Boolean bool, BlockPos blockPos, BlockState blockState) {
        this.bool = bool;
        this.blockPos = blockPos;
        this.blockState = blockState;
    }

    public static BooleanBlockPosStatePacket read(PacketByteBuf buf) {
        try {
            Boolean bool = buf.readBoolean();
            BlockPos blockPos = buf.readBlockPos();
            BlockState blockState = Block.getStateFromRawId(buf.readInt());

            return new BooleanBlockPosStatePacket(bool, blockPos, blockState);
        } catch(Exception ignored) {
            return null;
        }
    }
    public static PacketByteBuf write(Identifier channel, Boolean bool, BlockPos blockPos, BlockState blockState) {
        PacketByteBuf packet = new PacketByteBuf(Unpooled.buffer());
        packet.writeIdentifier(channel);
        packet.writeBoolean(bool);
        packet.writeBlockPos(blockPos);
        packet.writeInt(Block.getRawIdFromState(blockState));

       return packet;
    }
}