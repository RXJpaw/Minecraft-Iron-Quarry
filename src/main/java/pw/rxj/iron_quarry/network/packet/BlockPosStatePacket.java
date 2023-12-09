package pw.rxj.iron_quarry.network.packet;

import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class BlockPosStatePacket {
        public final BlockPos blockPos;
        public final BlockState blockState;

        private BlockPosStatePacket(BlockPos blockPos, BlockState blockState) {
            this.blockPos = blockPos;
            this.blockState = blockState;
        }

        public static BlockPosStatePacket read(PacketByteBuf buf) {
            try {
                BlockPos blockPos = buf.readBlockPos();
                BlockState blockState = Block.getStateFromRawId(buf.readInt());

                return new BlockPosStatePacket(blockPos, blockState);
            } catch(Exception ignored) {
                return null;
            }
        }
        public static PacketByteBuf write(Identifier channel, BlockPos blockPos, BlockState blockState) {
            PacketByteBuf packet = new PacketByteBuf(Unpooled.buffer());
            packet.writeIdentifier(channel);
            packet.writeBlockPos(blockPos);
            packet.writeInt(Block.getRawIdFromState(blockState));

            return packet;
        }
    }