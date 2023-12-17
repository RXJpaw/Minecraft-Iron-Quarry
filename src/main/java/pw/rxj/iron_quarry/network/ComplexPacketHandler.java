package pw.rxj.iron_quarry.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public abstract class ComplexPacketHandler<T> implements ComplexPacketProvider<T> {
    protected final void receiveFromServer(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender response) {
        T packet = this.read(buf);
        if(packet != null) client.execute(() -> this.receiveFromServer(client, handler, packet, response));
    }
    protected void receiveFromServer(MinecraftClient client, ClientPlayNetworkHandler handler, @NotNull T packet, PacketSender response) { }

    protected final void receiveFromClient(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender response) {
        T packet = this.read(buf);
        if(packet != null) server.execute(() -> this.receiveFromClient(server, player, handler, packet, response));
    }
    protected void receiveFromClient(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, @NotNull T packet, PacketSender response) { }
}
