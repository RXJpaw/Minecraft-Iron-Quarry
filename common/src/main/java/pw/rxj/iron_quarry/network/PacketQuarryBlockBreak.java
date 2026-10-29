package pw.rxj.iron_quarry.network;

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
import pw.rxj.iron_quarry.network.packet.BlockPosStateListPacket;
import pw.rxj.iron_quarry.render.RenderUtil;
import pw.rxj.iron_quarry.resource.config.client.BlockBreakingConfig;
import pw.rxj.iron_quarry.util.BlockPosState;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class PacketQuarryBlockBreak extends BlockPosStateListPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketQuarryBlockBreak> PACKET_CODEC = StreamCodec.ofMember(PacketQuarryBlockBreak::write, PacketQuarryBlockBreak::new);
    public static final CustomPacketPayload.Type<PacketQuarryBlockBreak> PACKET_ID = register("quarry_block_break", PACKET_CODEC);

    private static final BlockBreakingConfig.Handler BLOCK_BREAKING_CONFIG = Main.CONFIG.getBlockBreakingConfig();

    private PacketQuarryBlockBreak(List<BlockPosState> blockPosStates) {
        super(blockPosStates);
    }
    private PacketQuarryBlockBreak(RegistryFriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public Type<PacketQuarryBlockBreak> type() {
        return PACKET_ID;
    }

    @Override
    protected void receiveFromServer(BlockPosStateListPacket payload, Minecraft client, LocalPlayer player) {
        if(client.player == null || client.level == null) return;

        List<BlockPosState> visibleBlockPosStates = new ArrayList<>();
        for (BlockPosState blockPosState : payload.blockPosStates) {
            Vec3 blockPosVec = RenderUtil.vec3dFrom(blockPosState.blockPos);
            if(client.player.position().distanceTo(blockPosVec) > BLOCK_BREAKING_CONFIG.getDistance()) continue;
            if(blockPosState.blockState.isAir() || !blockPosState.blockState.getFluidState().isEmpty()) continue;

            visibleBlockPosStates.add(blockPosState);
        }

        HashSet<Integer> shouldPlaySound = ZUtil.getNSpacedIndexes(visibleBlockPosStates.size(), 10);

        for (int i = 0; i < visibleBlockPosStates.size(); i++) {
            BlockPosState blockPosState = visibleBlockPosStates.get(i);

            BlockPos blockPos = blockPosState.blockPos;
            BlockState blockState = blockPosState.blockState;

            SoundType blockSoundGroup = blockState.getSoundType();
            client.level.addDestroyBlockEffect(blockPos, blockState);

            if(shouldPlaySound.contains(i)) {
                float volume = ((blockSoundGroup.getVolume() + 1.0F) / 2.0F) * BLOCK_BREAKING_CONFIG.getVolume();
                float pitch = blockSoundGroup.getPitch() * 0.8F;

                if(volume > 0) {
                    client.level.playLocalSound(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockSoundGroup.getBreakSound(), SoundSource.BLOCKS, volume, pitch, false);
                }
            }
        }
    }

    public static PacketQuarryBlockBreak bake(List<BlockPosState> blockPosStates) {
        return new PacketQuarryBlockBreak(blockPosStates);
    }
}
