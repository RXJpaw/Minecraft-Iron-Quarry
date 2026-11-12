package pw.rxj.iron_quarry.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.network.packet.BlockPosStateListPacket;
import pw.rxj.iron_quarry.render.RenderUtil;
import pw.rxj.iron_quarry.resource.config.client.BlockBreakingConfig;
import pw.rxj.iron_quarry.util.BlockActress;
import pw.rxj.iron_quarry.util.BlockPosState;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.ArrayList;
import java.util.List;

public class PacketQuarryBlockBreak extends BlockPosStateListPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketQuarryBlockBreak> PACKET_CODEC = StreamCodec.ofMember(PacketQuarryBlockBreak::write, PacketQuarryBlockBreak::new);
    public static final CustomPacketPayload.Type<PacketQuarryBlockBreak> PACKET_ID = register("quarry_block_break", PACKET_CODEC);

    private static final BlockBreakingConfig.Handler BLOCK_BREAKING_CONFIG = Main.CONFIG.getBlockBreakingConfig();
    private static final int MAX_SOUNDS_PER_PACKET = 3;
    private static final int MAX_SOUNDS_PER_TICK = 6;

    private static long currentTick = -1;
    private static int soundsInCurrentTick = 0;
    private static int packetsInCurrentTick = 0;
    private static int packetsInPreviousTick = 0;

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
        List<BlockPosState> audibleBlockPosStates = new ArrayList<>();
        for (BlockPosState blockPosState : payload.blockPosStates) {
            Vec3 blockPosVec = RenderUtil.vec3dFrom(blockPosState.blockPos);
            double distance = client.player.position().distanceTo(blockPosVec);
            if(distance > BLOCK_BREAKING_CONFIG.getDistance()) continue;
            if(blockPosState.blockState.isAir()) continue;

            if(!BlockActress.isLiquid(blockPosState.blockState)) visibleBlockPosStates.add(blockPosState);
            if(isAudible(blockPosState, distance)) audibleBlockPosStates.add(blockPosState);
        }

        for (BlockPosState blockPosState : visibleBlockPosStates) {
            client.level.addDestroyBlockEffect(blockPosState.blockPos, blockPosState.blockState);
        }
        if(audibleBlockPosStates.isEmpty()) return;

        int soundCount = takeSounds(client.level.getGameTime(), client.level.getRandom(), audibleBlockPosStates.size());
        for (int index : ZUtil.getNSpacedIndexes(audibleBlockPosStates.size(), soundCount)) {
            playBreakSound(client.level, audibleBlockPosStates.get(index));
        }
    }

    private static int takeSounds(long gameTime, RandomSource random, int blockCount) {
        if(gameTime != currentTick) {
            packetsInPreviousTick = packetsInCurrentTick;
            currentTick = gameTime;
            soundsInCurrentTick = 0;
            packetsInCurrentTick = 0;
        }
        packetsInCurrentTick++;

        int remainingSounds = MAX_SOUNDS_PER_TICK - soundsInCurrentTick;
        int soundCount = Math.min(rollFairShare(random), Math.min(blockCount, remainingSounds));
        soundsInCurrentTick += soundCount;

        return soundCount;
    }

    private static int rollFairShare(RandomSource random) {
        if(packetsInPreviousTick == 0) return 1;

        int packets = Math.max(packetsInPreviousTick, packetsInCurrentTick);
        float share = Math.min(MAX_SOUNDS_PER_PACKET, (float) MAX_SOUNDS_PER_TICK / packets);
        int wholeSounds = (int) share;

        return random.nextFloat() < share - wholeSounds ? wholeSounds + 1 : wholeSounds;
    }

    private static boolean isAudible(BlockPosState blockPosState, double distance) {
        float volume = getBreakVolume(blockPosState.blockState);

        return volume > 0 && distance <= getBreakSound(blockPosState.blockState).getRange(volume);
    }

    private static SoundEvent getBreakSound(BlockState blockState) {
        if(BlockActress.isLiquid(blockState)) return blockState.getFluidState().getType().getPickupSound().orElse(SoundEvents.BUCKET_FILL);

        return blockState.getSoundType().getBreakSound();
    }

    private static float getBreakVolume(BlockState blockState) {
        if(BlockActress.isLiquid(blockState)) return BLOCK_BREAKING_CONFIG.getVolume();

        SoundType soundType = blockState.getSoundType();
        return ((soundType.getVolume() + 1.0F) / 2.0F) * BLOCK_BREAKING_CONFIG.getVolume();
    }

    private static float getBreakPitch(BlockState blockState) {
        if(BlockActress.isLiquid(blockState)) return 1.0F;

        return blockState.getSoundType().getPitch() * 0.8F;
    }

    private static void playBreakSound(ClientLevel level, BlockPosState blockPosState) {
        BlockState blockState = blockPosState.blockState;
        BlockPos blockPos = blockPosState.blockPos;

        level.playLocalSound(blockPos.getX(), blockPos.getY(), blockPos.getZ(), getBreakSound(blockState), SoundSource.BLOCKS, getBreakVolume(blockState), getBreakPitch(blockState), false);
    }

    public static PacketQuarryBlockBreak bake(List<BlockPosState> blockPosStates) {
        return new PacketQuarryBlockBreak(blockPosStates);
    }
}
