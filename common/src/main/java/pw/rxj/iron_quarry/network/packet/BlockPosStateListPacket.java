package pw.rxj.iron_quarry.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import pw.rxj.iron_quarry.network.ComplexPayloadHandler;
import pw.rxj.iron_quarry.util.BlockPosState;

import java.util.ArrayList;
import java.util.List;

public class BlockPosStateListPacket extends ComplexPayloadHandler<BlockPosStateListPacket> {
    public final List<BlockPosState> blockPosStates;

    protected BlockPosStateListPacket(List<BlockPosState> blockPosStates) {
        this.blockPosStates = List.copyOf(blockPosStates);
    }
    protected BlockPosStateListPacket(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<BlockPosState> blockPosStates = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            blockPosStates.add(BlockPosState.of(Block.stateById(buf.readVarInt()), buf.readBlockPos()));
        }

        this.blockPosStates = List.copyOf(blockPosStates);
    }
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(blockPosStates.size());

        for (BlockPosState blockPosState : blockPosStates) {
            buf.writeVarInt(Block.getId(blockPosState.blockState));
            buf.writeBlockPos(blockPosState.blockPos);
        }
    }
}
