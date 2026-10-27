package pw.rxj.iron_quarry.network;

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
import pw.rxj.iron_quarry.platform.Platform;

@SuppressWarnings({"unchecked cast"})
public abstract class ComplexPayloadHandler<T extends ComplexPayloadHandler<?>> implements CustomPacketPayload {
    protected final void scheduleToClient(Minecraft client, LocalPlayer player) {
        client.execute(() -> this.receiveFromServer((T) this, client, player));
    }
    protected void receiveFromServer(T payload, Minecraft client, LocalPlayer player) { }

    protected final void scheduleToServer(MinecraftServer server, ServerPlayer player) {
        server.execute(() -> this.receiveFromClient((T) this, server, player));
    }
    protected void receiveFromClient(T payload, MinecraftServer server, ServerPlayer player) { }

    protected static <E extends CustomPacketPayload> CustomPacketPayload.Type<E> register(String path, StreamCodec<? super RegistryFriendlyByteBuf, E> packetCodec) {
        CustomPacketPayload.Type<E> packetId = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Main.MOD_ID, path));

        Platform.NETWORK.registerPayload(packetId, packetCodec);

        return packetId;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        throw new NotImplementedException("getId in ComplexPayloadHandler");
    }
}
