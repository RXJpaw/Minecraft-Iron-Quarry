package pw.rxj.iron_quarry.platform;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings({"rawtypes", "unchecked"})
public class NeoForgePlatformNetwork implements IPlatformNetwork {
    private static final Map<CustomPacketPayload.Type, StreamCodec> PAYLOADS = new LinkedHashMap<>();
    private static final Map<CustomPacketPayload.Type, ServerReceiver> SERVER_RECEIVERS = new HashMap<>();
    private static final Map<CustomPacketPayload.Type, ClientReceiver> CLIENT_RECEIVERS = new HashMap<>();

    @Override
    public <T extends CustomPacketPayload> void registerPayload(CustomPacketPayload.Type<T> packetId, StreamCodec<? super RegistryFriendlyByteBuf, T> packetCodec) {
        PAYLOADS.put(packetId, packetCodec);
    }

    @Override
    public <T extends CustomPacketPayload> void registerServerReceiver(CustomPacketPayload.Type<T> packetId, ServerReceiver<T> receiver) {
        SERVER_RECEIVERS.put(packetId, receiver);
    }

    @Override
    public <T extends CustomPacketPayload> void registerClientReceiver(CustomPacketPayload.Type<T> packetId, ClientReceiver<T> receiver) {
        CLIENT_RECEIVERS.put(packetId, receiver);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPacketDistributor.sendToServer(payload);
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        PAYLOADS.forEach((packetId, packetCodec) -> registrar.playBidirectional(packetId, packetCodec, (customPayload, context) -> {
            ServerReceiver receiver = SERVER_RECEIVERS.get(packetId);
            if(receiver == null) return;

            ServerPlayer player = (ServerPlayer) context.player();
            receiver.receive((CustomPacketPayload) customPayload, player.level().getServer(), player);
        }));
    }
    public static void registerClient(RegisterClientPayloadHandlersEvent event) {
        PAYLOADS.keySet().forEach(packetId -> event.register(packetId, (customPayload, context) -> {
            ClientReceiver receiver = CLIENT_RECEIVERS.get(packetId);
            if(receiver == null) return;

            receiver.receive((CustomPacketPayload) customPayload, Minecraft.getInstance(), (LocalPlayer) context.player());
        }));
    }
}
