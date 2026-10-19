package pw.rxj.iron_quarry.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.List;
import java.util.function.Function;

@SuppressWarnings({"rawtypes"})
public class ZNetwork {
    private static final List<CustomPayload.Id<? extends ComplexPayloadHandler>> packetList = List.of(
            PacketServerConfigApply.PACKET_ID,
            PacketQuarryBlockBreak.PACKET_ID,
            PacketBlueprintExpand.PACKET_ID,
            PacketKeyedAction.PACKET_ID
    );

    public static void registerClient() {
        packetList.forEach(packetId -> {
            ClientPlayNetworking.registerGlobalReceiver(packetId, (customPayload, context) -> {
                customPayload.scheduleToClient(context.client(), context);
            });
        });
    }
    public static void registerServer() {
        packetList.forEach(packetId -> {
            ServerPlayNetworking.registerGlobalReceiver(packetId, (customPayload, context) -> {
                customPayload.scheduleToServer(context.server(), context);
            });
        });
    }

    public static void sendToAround(ServerWorld serverWorld, BlockPos pos, double maxDistance, Function<ServerPlayerEntity, @Nullable CustomPayload> bufFunc) {
        sendToAround(serverWorld, ZUtil.toVec3d(pos), maxDistance, bufFunc);
    }
    public static void sendToAround(ServerWorld serverWorld, Vec3d pos, double maxDistance, Function<ServerPlayerEntity, @Nullable CustomPayload> bufFunc) {
        for (ServerPlayerEntity player : serverWorld.getPlayers()) {
            double blockPosToPlayerDistance = pos.distanceTo(player.getEyePos());
            if(blockPosToPlayerDistance > maxDistance) continue;

            CustomPayload payload = bufFunc.apply(player);
            if(payload == null) continue;

            sendToPlayer(player, payload);
        }
    }

    public static void sendToPlayer(ServerPlayerEntity player, CustomPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    public static void sendToServer(@Nullable CustomPayload payload) {
        if(payload == null) return;

        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        if(minecraftClient.player == null) return;

        ClientPlayNetworking.send(payload);
    }
}
