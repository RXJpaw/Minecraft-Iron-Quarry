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
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.network.packet.BooleanBlockPosStatePacket;
import pw.rxj.iron_quarry.render.RenderUtil;
import pw.rxj.iron_quarry.resource.ConfigHandler;

public class PacketQuarryBlockBreak extends ComplexPacketHandler<BooleanBlockPosStatePacket> {
    private static final ConfigHandler.BlockBreakingConfigHandler BlockBreakingConfig = Main.CONFIG.getBlockBreakingConfig();
    protected static PacketQuarryBlockBreak INSTANCE = new PacketQuarryBlockBreak();

    private PacketQuarryBlockBreak() { }

    @Override
    public Identifier getChannelId() {
        return Identifier.of(Main.MOD_ID, "quarry_block_break");
    }
    @Override
    public @Nullable BooleanBlockPosStatePacket read(PacketByteBuf buf) {
        return BooleanBlockPosStatePacket.read(buf);
    }

    @Override
    protected void receiveFromServer(MinecraftClient client, ClientPlayNetworkHandler handler, @NotNull BooleanBlockPosStatePacket packet, PacketSender response) {
        if(client.player == null || client.world == null) return;

        BlockPos blockPos = packet.blockPos;
        BlockState blockState = packet.blockState;

        Vec3d blockPosVec = RenderUtil.vec3dFrom(blockPos);
        if(client.player.getPos().distanceTo(blockPosVec) > BlockBreakingConfig.getDistance()) return;
        
        if (!blockState.isAir() && blockState.getFluidState().isEmpty()) {
            BlockSoundGroup blockSoundGroup = blockState.getSoundGroup();
            client.world.addBlockBreakParticles(blockPos, blockState);

            if(packet.bool) {
                float volume = ((blockSoundGroup.getVolume() + 1.0F) / 2.0F) * BlockBreakingConfig.getVolume();
                float pitch = blockSoundGroup.getPitch() * 0.8F;

                Main.LOGGER.info(BlockBreakingConfig.getVolume());

                if(volume > 0) {
                    client.world.playSound(blockPos, blockSoundGroup.getBreakSound(), SoundCategory.BLOCKS, volume, pitch, false);
                }
            }
        }
    }

    public static PacketByteBuf bake(BlockPos blockPos, BlockState blockState, boolean sound) {
        return BooleanBlockPosStatePacket.write(INSTANCE.getChannelId(), sound, blockPos, blockState);
    }
}
