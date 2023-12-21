package pw.rxj.iron_quarry.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.network.packet.ByteArrayPacket;
import pw.rxj.iron_quarry.resource.ConfigHandler;

public class PacketServerConfigApply extends ComplexPacketHandler<ByteArrayPacket> {
    protected static PacketServerConfigApply INSTANCE = new PacketServerConfigApply();

    private PacketServerConfigApply() { }

    @Override
    public Identifier getChannelId() {
        return Identifier.of(Main.MOD_ID, "server_config_apply");
    }
    @Override
    public @Nullable ByteArrayPacket read(PacketByteBuf buf) {
        return ByteArrayPacket.read(buf);
    }

    @Override
    protected void receiveFromServer(MinecraftClient client, ClientPlayNetworkHandler handler, @NotNull ByteArrayPacket packet, PacketSender response) {
        String address = client.getCurrentServerEntry() == null ? "unknown address/integrated server" : client.getCurrentServerEntry().address;

        Main.CONFIG.readByteArray(EnvType.SERVER, packet.bytes);
        Main.CONFIG.getQuarryStatsConfig().applyChanges();
        Main.CONFIG.getAugmentStatsConfig().applyChanges();

        Main.LOGGER.info("Received server config from: {}", address);
    }

    public static PacketByteBuf bake(ConfigHandler config) {
        return ByteArrayPacket.write(INSTANCE.getChannelId(), config.asByteArray(EnvType.SERVER));
    }
}
