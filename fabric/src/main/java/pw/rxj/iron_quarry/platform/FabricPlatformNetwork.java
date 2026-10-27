package pw.rxj.iron_quarry.platform;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public class FabricPlatformNetwork implements IPlatformNetwork {
    @Override
    public <T extends CustomPacketPayload> void registerPayload(CustomPacketPayload.Type<T> packetId, StreamCodec<? super RegistryFriendlyByteBuf, T> packetCodec) {
        PayloadTypeRegistry.clientboundPlay().register(packetId, packetCodec);
        PayloadTypeRegistry.serverboundPlay().register(packetId, packetCodec);
    }

    @Override
    public <T extends CustomPacketPayload> void registerServerReceiver(CustomPacketPayload.Type<T> packetId, ServerReceiver<T> receiver) {
        ServerPlayNetworking.registerGlobalReceiver(packetId, (customPayload, context) -> receiver.receive(customPayload, context.server(), context.player()));
    }

    @Override
    public <T extends CustomPacketPayload> void registerClientReceiver(CustomPacketPayload.Type<T> packetId, ClientReceiver<T> receiver) {
        ClientPlayNetworking.registerGlobalReceiver(packetId, (customPayload, context) -> receiver.receive(customPayload, context.client(), context.player()));
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }
}
