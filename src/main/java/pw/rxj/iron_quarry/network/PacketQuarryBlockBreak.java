package pw.rxj.iron_quarry.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.network.packet.BooleanBlockPosStatePacket;
import pw.rxj.iron_quarry.render.RenderUtil;
import pw.rxj.iron_quarry.resource.config.client.BlockBreakingConfig;

public class PacketQuarryBlockBreak extends BooleanBlockPosStatePacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketQuarryBlockBreak> PACKET_CODEC = StreamCodec.ofMember(PacketQuarryBlockBreak::write, PacketQuarryBlockBreak::new);
    public static final CustomPacketPayload.Type<PacketQuarryBlockBreak> PACKET_ID = register("quarry_block_break", PACKET_CODEC);

    private static final BlockBreakingConfig.Handler BLOCK_BREAKING_CONFIG = Main.CONFIG.getBlockBreakingConfig();

    private PacketQuarryBlockBreak(Boolean bool, BlockPos blockPos, BlockState blockState) {
        super(bool, blockPos, blockState);
    }
    private PacketQuarryBlockBreak(RegistryFriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public Type<PacketQuarryBlockBreak> type() {
        return PACKET_ID;
    }

    @Override
    protected void receiveFromServer(BooleanBlockPosStatePacket payload, Minecraft client, LocalPlayer player, PacketSender responseSender) {
        if(client.player == null || client.level == null) return;

        BlockPos blockPos = payload.blockPos;
        BlockState blockState = payload.blockState;

        Vec3 blockPosVec = RenderUtil.vec3dFrom(blockPos);
        if(client.player.position().distanceTo(blockPosVec) > BLOCK_BREAKING_CONFIG.getDistance()) return;
        
        if (!blockState.isAir() && blockState.getFluidState().isEmpty()) {
            SoundType blockSoundGroup = blockState.getSoundType();
            client.level.addDestroyBlockEffect(blockPos, blockState);

            if(payload.bool) {
                float volume = ((blockSoundGroup.getVolume() + 1.0F) / 2.0F) * BLOCK_BREAKING_CONFIG.getVolume();
                float pitch = blockSoundGroup.getPitch() * 0.8F;

                if(volume > 0) {
                    client.level.playLocalSound(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockSoundGroup.getBreakSound(), SoundSource.BLOCKS, volume, pitch, false);
                }
            }
        }
    }

    public static PacketQuarryBlockBreak bake(BlockPos blockPos, BlockState blockState, boolean sound) {
        return new PacketQuarryBlockBreak(sound, blockPos, blockState);
    }
}
