package pw.rxj.iron_quarry.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import pw.rxj.iron_quarry.network.packet.IntLongPacket;
import pw.rxj.iron_quarry.screen.QuarryBlockScreenHandler;

public class PacketQuarryMenuEnergy extends IntLongPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketQuarryMenuEnergy> PACKET_CODEC = StreamCodec.ofMember(PacketQuarryMenuEnergy::write, PacketQuarryMenuEnergy::new);
    public static final CustomPacketPayload.Type<PacketQuarryMenuEnergy> PACKET_ID = register("quarry_menu_energy", PACKET_CODEC);

    private PacketQuarryMenuEnergy(int containerId, long energy) {
        super(containerId, energy);
    }
    private PacketQuarryMenuEnergy(RegistryFriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public Type<PacketQuarryMenuEnergy> type() {
        return PACKET_ID;
    }

    @Override
    protected void receiveFromServer(IntLongPacket payload, Minecraft client, LocalPlayer player) {
        if(client.player == null) return;
        if(!(client.player.containerMenu instanceof QuarryBlockScreenHandler screenHandler)) return;
        if(screenHandler.containerId != payload.intValue) return;

        screenHandler.getEnergyContainer().read(payload.longValue);
    }

    public static PacketQuarryMenuEnergy bake(int containerId, long energy) {
        return new PacketQuarryMenuEnergy(containerId, energy);
    }
}
