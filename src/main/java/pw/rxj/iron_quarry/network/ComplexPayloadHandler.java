package pw.rxj.iron_quarry.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.NotImplementedException;
import pw.rxj.iron_quarry.Main;

@SuppressWarnings({"unchecked cast"})
public abstract class ComplexPayloadHandler<T extends ComplexPayloadHandler<?>> implements CustomPayload {
    protected final void scheduleToClient(MinecraftClient client, ClientPlayNetworking.Context context) {
        client.execute(() -> this.receiveFromServer((T) this, client, context.player(), context.responseSender()));
    }
    protected void receiveFromServer(T payload, MinecraftClient client, ClientPlayerEntity player, PacketSender responseSender) { }

    protected final void scheduleToServer(MinecraftServer server, ServerPlayNetworking.Context context) {
        server.execute(() -> this.receiveFromClient((T) this, server, context.player(), context.responseSender()));
    }
    protected void receiveFromClient(T payload, MinecraftServer server, ServerPlayerEntity player, PacketSender responseSender) { }

    protected static <E extends CustomPayload> CustomPayload.Id<E> register(String path, PacketCodec<? super RegistryByteBuf, E> packetCodec) {
        CustomPayload.Id<E> packetId = new CustomPayload.Id<>(Identifier.of(Main.MOD_ID, path));

        PayloadTypeRegistry.playS2C().register(packetId, packetCodec);
        PayloadTypeRegistry.playC2S().register(packetId, packetCodec);

        return packetId;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        throw new NotImplementedException("getId in ComplexPayloadHandler");
    }
}
