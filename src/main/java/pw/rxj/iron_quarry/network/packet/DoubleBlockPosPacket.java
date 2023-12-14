package pw.rxj.iron_quarry.network.packet;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class DoubleBlockPosPacket {
        public final BlockPos blockPos1;
        public final BlockPos blockPos2;

        private DoubleBlockPosPacket(BlockPos blockPos1, BlockPos blockPos2) {
            this.blockPos1 = blockPos1;
            this.blockPos2 = blockPos2;
        }

        public static DoubleBlockPosPacket read(PacketByteBuf buf) {
            try {
                BlockPos blockPos1 = buf.readBlockPos();
                BlockPos blockPos2 = buf.readBlockPos();

                return new DoubleBlockPosPacket(blockPos1, blockPos2);
            } catch(Exception ignored) {
                return null;
            }
        }
        public static PacketByteBuf write(Identifier channel, BlockPos blockPos1, BlockPos blockPos2) {
            PacketByteBuf packet = new PacketByteBuf(Unpooled.buffer());
            packet.writeIdentifier(channel);
            packet.writeBlockPos(blockPos1);
            packet.writeBlockPos(blockPos2);

            return packet;
        }
    }