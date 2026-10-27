package pw.rxj.iron_quarry.platform;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public interface IPlatformNetwork {
    <T extends CustomPacketPayload> void registerPayload(CustomPacketPayload.Type<T> packetId, StreamCodec<? super RegistryFriendlyByteBuf, T> packetCodec);
    <T extends CustomPacketPayload> void registerServerReceiver(CustomPacketPayload.Type<T> packetId, ServerReceiver<T> receiver);
    <T extends CustomPacketPayload> void registerClientReceiver(CustomPacketPayload.Type<T> packetId, ClientReceiver<T> receiver);

    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);
    void sendToServer(CustomPacketPayload payload);

    interface ServerReceiver<T extends CustomPacketPayload> {
        void receive(T payload, MinecraftServer server, ServerPlayer player);
    }
    interface ClientReceiver<T extends CustomPacketPayload> {
        void receive(T payload, Minecraft client, LocalPlayer player);
    }
}
