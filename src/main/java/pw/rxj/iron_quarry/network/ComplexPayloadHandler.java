package pw.rxj.iron_quarry.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.NotImplementedException;
import pw.rxj.iron_quarry.Main;

@SuppressWarnings({"unchecked cast"})
public abstract class ComplexPayloadHandler<T extends ComplexPayloadHandler<?>> implements CustomPacketPayload {
    protected final void scheduleToClient(Minecraft client, ClientPlayNetworking.Context context) {
        client.execute(() -> this.receiveFromServer((T) this, client, context.player(), context.responseSender()));
    }
    protected void receiveFromServer(T payload, Minecraft client, LocalPlayer player, PacketSender responseSender) { }

    protected final void scheduleToServer(MinecraftServer server, ServerPlayNetworking.Context context) {
        server.execute(() -> this.receiveFromClient((T) this, server, context.player(), context.responseSender()));
    }
    protected void receiveFromClient(T payload, MinecraftServer server, ServerPlayer player, PacketSender responseSender) { }

    protected static <E extends CustomPacketPayload> CustomPacketPayload.Type<E> register(String path, StreamCodec<? super RegistryFriendlyByteBuf, E> packetCodec) {
        CustomPacketPayload.Type<E> packetId = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Main.MOD_ID, path));

        PayloadTypeRegistry.clientboundPlay().register(packetId, packetCodec);
        PayloadTypeRegistry.serverboundPlay().register(packetId, packetCodec);

        return packetId;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        throw new NotImplementedException("getId in ComplexPayloadHandler");
    }
}
