package pw.rxj.iron_quarry.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.platform.Platform;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.List;
import java.util.function.Function;

@SuppressWarnings({"rawtypes"})
public class ZNetwork {
    private static final List<CustomPacketPayload.Type<? extends ComplexPayloadHandler>> packetList = List.of(
            PacketServerConfigApply.PACKET_ID,
            PacketQuarryBlockBreak.PACKET_ID,
            PacketQuarryMenuEnergy.PACKET_ID,
            PacketBlueprintExpand.PACKET_ID,
            PacketBlueprintSelect.PACKET_ID
    );

    public static void registerClient() {
        packetList.forEach(packetId -> {
            Platform.NETWORK.registerClientReceiver(packetId, (customPayload, client, player) -> {
                customPayload.scheduleToClient(client, player);
            });
        });
    }
    public static void registerServer() {
        packetList.forEach(packetId -> {
            Platform.NETWORK.registerServerReceiver(packetId, (customPayload, server, player) -> {
                customPayload.scheduleToServer(server, player);
            });
        });
    }

    public static void sendToAround(ServerLevel serverWorld, BlockPos pos, double maxDistance, Function<ServerPlayer, @Nullable CustomPacketPayload> bufFunc) {
        sendToAround(serverWorld, ZUtil.toVec3d(pos), maxDistance, bufFunc);
    }
    public static void sendToAround(ServerLevel serverWorld, Vec3 pos, double maxDistance, Function<ServerPlayer, @Nullable CustomPacketPayload> bufFunc) {
        sendToAllInLevel(serverWorld, player -> pos.distanceTo(player.getEyePosition()) > maxDistance ? null : bufFunc.apply(player));
    }
    public static void sendToAllInLevel(ServerLevel serverWorld, Function<ServerPlayer, @Nullable CustomPacketPayload> bufFunc) {
        for (ServerPlayer player : serverWorld.players()) {
            CustomPacketPayload payload = bufFunc.apply(player);
            if(payload == null) continue;

            sendToPlayer(player, payload);
        }
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        Platform.NETWORK.sendToPlayer(player, payload);
    }

    public static void sendToServer(@Nullable CustomPacketPayload payload) {
        if(payload == null) return;

        Minecraft minecraftClient = Minecraft.getInstance();
        if(minecraftClient.player == null) return;

        Platform.NETWORK.sendToServer(payload);
    }
}
