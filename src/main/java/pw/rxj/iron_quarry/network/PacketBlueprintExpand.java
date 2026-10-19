package pw.rxj.iron_quarry.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Direction;
import oshi.util.tuples.Pair;
import pw.rxj.iron_quarry.item.BlueprintItem;
import pw.rxj.iron_quarry.network.packet.DirectionBytePacket;
import pw.rxj.iron_quarry.util.ReadableString;
import pw.rxj.iron_quarry.util.ZUtil;

public class PacketBlueprintExpand extends DirectionBytePacket {
    public static final PacketCodec<RegistryByteBuf, PacketBlueprintExpand> PACKET_CODEC = PacketCodec.of(PacketBlueprintExpand::write, PacketBlueprintExpand::new);
    public static final CustomPayload.Id<PacketBlueprintExpand> PACKET_ID = register("packet_blueprint_expand", PACKET_CODEC);

    public static Pair<Integer, Integer> ALLOWED_DISTANCE = new Pair<>(-16, 16);

    private PacketBlueprintExpand(Direction direction, byte data) {
        super(direction, data);
    }
    private PacketBlueprintExpand(RegistryByteBuf buf) {
        super(buf);
    }

    @Override
    public Id<PacketBlueprintExpand> getId() {
        return PACKET_ID;
    }

    protected boolean checkPacket(ServerPlayerEntity player, DirectionBytePacket packet) {
        if(packet.data < ALLOWED_DISTANCE.getA() || packet.data > ALLOWED_DISTANCE.getB()) {
            player.networkHandler.disconnect(ReadableString.translatable("kick_reason.iron_quarry.packet_blueprint_position_set.disallowed_distance", ALLOWED_DISTANCE.getA(), ALLOWED_DISTANCE.getB()));
            return false;
        }

        return true;
    }

    @Override
    protected void receiveFromClient(DirectionBytePacket payload, MinecraftServer server, ServerPlayerEntity player, PacketSender responseSender) {
        if(!this.checkPacket(player, payload)) return;

        PlayerInventory playerInventory = player.getInventory();
        ItemStack holdingStack = playerInventory.getSelectedStack();

        if(ZUtil.getBlockOrItem(holdingStack) instanceof BlueprintItem blueprintItem) {
            if(blueprintItem.isSealed(holdingStack)) return;

            blueprintItem.expandInDirection(holdingStack, player, payload.direction, payload.data);
        }
    }

    public static PacketBlueprintExpand bake(Direction direction, byte distance) {
        return new PacketBlueprintExpand(direction, distance);
    }
}
