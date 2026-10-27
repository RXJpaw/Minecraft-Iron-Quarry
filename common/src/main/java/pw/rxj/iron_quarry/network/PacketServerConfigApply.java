package pw.rxj.iron_quarry.network;

import pw.rxj.iron_quarry.resource.config.ConfigSide;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.network.packet.ByteArrayPacket;
import pw.rxj.iron_quarry.resource.config.ConfigHandler;

public class PacketServerConfigApply extends ByteArrayPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketServerConfigApply> PACKET_CODEC = StreamCodec.ofMember(PacketServerConfigApply::write, PacketServerConfigApply::new);
    public static final CustomPacketPayload.Type<PacketServerConfigApply> PACKET_ID = register("server_config_apply", PACKET_CODEC);

    private PacketServerConfigApply(byte[] bytes) {
        super(bytes);
    }
    public PacketServerConfigApply(RegistryFriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public Type<PacketServerConfigApply> type() {
        return PACKET_ID;
    }

    @Override
    protected void receiveFromServer(ByteArrayPacket payload, Minecraft client, LocalPlayer player) {
        String address = client.getCurrentServer() == null ? "unknown address/integrated server" : client.getCurrentServer().ip;

        Main.CONFIG.readByteArray(ConfigSide.SERVER, payload.bytes);
        Main.CONFIG.applyServerChanges();

        Main.LOGGER.info("Received server config from: {}", address);
    }

    public static PacketServerConfigApply bake(ConfigHandler config) {
        return new PacketServerConfigApply(config.asByteArray(ConfigSide.SERVER));
    }
}
