package pw.rxj.iron_quarry.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.network.packet.BooleanBlockPosStatePacket;
import pw.rxj.iron_quarry.render.RenderUtil;
import pw.rxj.iron_quarry.resource.config.client.BlockBreakingConfig;

public class PacketQuarryBlockBreak extends BooleanBlockPosStatePacket {
    public static final PacketCodec<RegistryByteBuf, PacketQuarryBlockBreak> PACKET_CODEC = PacketCodec.of(PacketQuarryBlockBreak::write, PacketQuarryBlockBreak::new);
    public static final CustomPayload.Id<PacketQuarryBlockBreak> PACKET_ID = register("quarry_block_break", PACKET_CODEC);

    private static final BlockBreakingConfig.Handler BLOCK_BREAKING_CONFIG = Main.CONFIG.getBlockBreakingConfig();

    private PacketQuarryBlockBreak(Boolean bool, BlockPos blockPos, BlockState blockState) {
        super(bool, blockPos, blockState);
    }
    private PacketQuarryBlockBreak(RegistryByteBuf buf) {
        super(buf);
    }

    @Override
    public Id<PacketQuarryBlockBreak> getId() {
        return PACKET_ID;
    }

    @Override
    protected void receiveFromServer(BooleanBlockPosStatePacket payload, MinecraftClient client, ClientPlayerEntity player, PacketSender responseSender) {
        if(client.player == null || client.world == null) return;

        BlockPos blockPos = payload.blockPos;
        BlockState blockState = payload.blockState;

        Vec3d blockPosVec = RenderUtil.vec3dFrom(blockPos);
        if(client.player.getPos().distanceTo(blockPosVec) > BLOCK_BREAKING_CONFIG.getDistance()) return;
        
        if (!blockState.isAir() && blockState.getFluidState().isEmpty()) {
            BlockSoundGroup blockSoundGroup = blockState.getSoundGroup();
            client.world.addBlockBreakParticles(blockPos, blockState);

            if(payload.bool) {
                float volume = ((blockSoundGroup.getVolume() + 1.0F) / 2.0F) * BLOCK_BREAKING_CONFIG.getVolume();
                float pitch = blockSoundGroup.getPitch() * 0.8F;

                if(volume > 0) {
                    client.world.playSound(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockSoundGroup.getBreakSound(), SoundCategory.BLOCKS, volume, pitch, false);
                }
            }
        }
    }

    public static PacketQuarryBlockBreak bake(BlockPos blockPos, BlockState blockState, boolean sound) {
        return new PacketQuarryBlockBreak(sound, blockPos, blockState);
    }
}
