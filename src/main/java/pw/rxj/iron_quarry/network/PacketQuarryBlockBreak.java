package pw.rxj.iron_quarry.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.network.packet.BlockPosStatePacket;

public class PacketQuarryBlockBreak extends ComplexPacketHandler<BlockPosStatePacket> {
    protected static PacketQuarryBlockBreak INSTANCE = new PacketQuarryBlockBreak();

    private PacketQuarryBlockBreak() { }

    @Override
    public Identifier getChannelId() {
        return Identifier.of(Main.MOD_ID, "quarry_block_break");
    }
    @Override
    public @Nullable BlockPosStatePacket read(PacketByteBuf buf) {
        return BlockPosStatePacket.read(buf);
    }

    @Override
    protected void receiveFromServer(MinecraftClient client, ClientPlayNetworkHandler handler, @NotNull BlockPosStatePacket packet, PacketSender response) {
        if(client.world == null) return;

        BlockPos blockPos = packet.blockPos;
        BlockState blockState = packet.blockState;
        
        if (!blockState.isAir() && blockState.getFluidState().isEmpty()) {
            BlockSoundGroup blockSoundGroup = blockState.getSoundGroup();
            client.world.playSound(blockPos, blockSoundGroup.getBreakSound(), SoundCategory.BLOCKS, (blockSoundGroup.getVolume() + 1.0F) / 2.0F, blockSoundGroup.getPitch() * 0.8F, false);
            client.world.addBlockBreakParticles(blockPos, blockState);
        }
    }

    public static PacketByteBuf bake(BlockPos blockPos, BlockState blockState) {
        return BlockPosStatePacket.write(INSTANCE.getChannelId(), blockPos, blockState);
    }
}
