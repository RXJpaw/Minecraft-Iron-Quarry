package pw.rxj.iron_quarry.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.network.packet.ByteArrayPacket;
import pw.rxj.iron_quarry.resource.config.ConfigHandler;

public class PacketServerConfigApply extends ByteArrayPacket {
    public static final PacketCodec<RegistryByteBuf, PacketServerConfigApply> PACKET_CODEC = PacketCodec.of(PacketServerConfigApply::write, PacketServerConfigApply::new);
    public static final CustomPayload.Id<PacketServerConfigApply> PACKET_ID = register("server_config_apply", PACKET_CODEC);

    private PacketServerConfigApply(byte[] bytes) {
        super(bytes);
    }
    public PacketServerConfigApply(RegistryByteBuf buf) {
        super(buf);
    }

    @Override
    public Id<PacketServerConfigApply> getId() {
        return PACKET_ID;
    }

    @Override
    protected void receiveFromServer(ByteArrayPacket payload, MinecraftClient client, ClientPlayerEntity player, PacketSender responseSender) {
        String address = client.getCurrentServerEntry() == null ? "unknown address/integrated server" : client.getCurrentServerEntry().address;

        Main.CONFIG.readByteArray(EnvType.SERVER, payload.bytes);
        Main.CONFIG.applyServerChanges();

        Main.LOGGER.info("Received server config from: {}", address);
    }

    public static PacketServerConfigApply bake(ConfigHandler config) {
        return new PacketServerConfigApply(config.asByteArray(EnvType.SERVER));
    }
}
