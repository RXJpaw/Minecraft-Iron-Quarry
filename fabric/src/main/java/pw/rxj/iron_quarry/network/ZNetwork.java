package pw.rxj.iron_quarry.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.List;
import java.util.function.Function;

@SuppressWarnings({"rawtypes"})
public class ZNetwork {
    private static final List<CustomPacketPayload.Type<? extends ComplexPayloadHandler>> packetList = List.of(
            PacketServerConfigApply.PACKET_ID,
            PacketQuarryBlockBreak.PACKET_ID,
            PacketBlueprintExpand.PACKET_ID,
            PacketBlueprintSelect.PACKET_ID
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

    public static void sendToAround(ServerLevel serverWorld, BlockPos pos, double maxDistance, Function<ServerPlayer, @Nullable CustomPacketPayload> bufFunc) {
        sendToAround(serverWorld, ZUtil.toVec3d(pos), maxDistance, bufFunc);
    }
    public static void sendToAround(ServerLevel serverWorld, Vec3 pos, double maxDistance, Function<ServerPlayer, @Nullable CustomPacketPayload> bufFunc) {
        for (ServerPlayer player : serverWorld.players()) {
            double blockPosToPlayerDistance = pos.distanceTo(player.getEyePosition());
            if(blockPosToPlayerDistance > maxDistance) continue;

            CustomPacketPayload payload = bufFunc.apply(player);
            if(payload == null) continue;

            sendToPlayer(player, payload);
        }
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    public static void sendToServer(@Nullable CustomPacketPayload payload) {
        if(payload == null) return;

        Minecraft minecraftClient = Minecraft.getInstance();
        if(minecraftClient.player == null) return;

        ClientPlayNetworking.send(payload);
    }
}
