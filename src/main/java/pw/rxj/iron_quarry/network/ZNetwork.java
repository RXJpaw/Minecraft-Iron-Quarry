package pw.rxj.iron_quarry.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.List;
import java.util.function.Function;

public class ZNetwork {
    private static final List<ComplexPacketHandler<?>> packetList = List.of(
            PacketQuarryBlockBreak.INSTANCE
    );

    @Environment(EnvType.CLIENT)
    public static void registerClient() {
        packetList.forEach(packetHandler -> {
            ClientPlayNetworking.registerGlobalReceiver(packetHandler.getChannelId(), packetHandler::receiveFromServer);
        });
    }
    public static void registerServer() {
        packetList.forEach(packetHandler -> {
            ServerPlayNetworking.registerGlobalReceiver(packetHandler.getChannelId(), packetHandler::receiveFromClient);
        });
    }

    public static void sendToAround(ServerWorld serverWorld, BlockPos pos, double maxDistance, Function<ServerPlayerEntity, @Nullable PacketByteBuf> bufFunc) {
        sendToAround(serverWorld, ZUtil.toVec3d(pos), maxDistance, bufFunc);
    }
    public static void sendToAround(ServerWorld serverWorld, Vec3d pos, double maxDistance, Function<ServerPlayerEntity, @Nullable PacketByteBuf> bufFunc) {
        for (ServerPlayerEntity player : serverWorld.getPlayers()) {
            double blockPosToPlayerDistance = pos.distanceTo(player.getEyePos());
            if(blockPosToPlayerDistance > maxDistance) continue;

            PacketByteBuf buf = bufFunc.apply(player);
            if(buf == null) continue;

            PacketByteBuf bufCopy = new PacketByteBuf(buf.copy());
            CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(bufCopy);

            player.networkHandler.sendPacket(packet);
        }
    }
}