package pw.rxj.iron_quarry.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.CreativeModeTabs;
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

        //The integrated server shares the config and items with this client, it already applied everything.
        if(!client.hasSingleplayerServer()) {
            Main.CONFIG.readServerByteArray(payload.bytes);
            Main.CONFIG.validateServerConfig();

            //Item components are built from the config before it is received, rebuild them so e.g. enchantability matches the server.
            BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(player.registryAccess()).stream()
                    .filter(pendingComponents -> pendingComponents.key().equals(Registries.ITEM))
                    .forEach(DataComponentInitializers.PendingComponents::apply);
        }

        //Creative tabs only rebuild when feature flags or permissions change, force it so e.g. disabled augments update.
        CreativeModeTabs.CACHED_PARAMETERS = null;

        Main.LOGGER.info("Received server config from: {}", address);
    }

    public static PacketServerConfigApply bake(ConfigHandler config) {
        return new PacketServerConfigApply(config.asServerByteArray());
    }
}
