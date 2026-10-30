package pw.rxj.iron_quarry.network;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Direction;
import net.minecraft.network.HashedStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import pw.rxj.iron_quarry.item.BlueprintItem;
import pw.rxj.iron_quarry.network.packet.DirectionByteHashPacket;
import pw.rxj.iron_quarry.util.ReadableString;
import pw.rxj.iron_quarry.util.ZUtil;

public class PacketBlueprintExpand extends DirectionByteHashPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketBlueprintExpand> PACKET_CODEC = StreamCodec.ofMember(PacketBlueprintExpand::write, PacketBlueprintExpand::new);
    public static final CustomPacketPayload.Type<PacketBlueprintExpand> PACKET_ID = register("packet_blueprint_expand", PACKET_CODEC);

    public static Pair<Integer, Integer> ALLOWED_DISTANCE = new Pair<>(-16, 16);

    private PacketBlueprintExpand(Direction direction, byte data, HashedStack hash) {
        super(direction, data, hash);
    }
    private PacketBlueprintExpand(RegistryFriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public Type<PacketBlueprintExpand> type() {
        return PACKET_ID;
    }

    protected boolean checkPacket(ServerPlayer player, DirectionByteHashPacket packet) {
        if(packet.data < ALLOWED_DISTANCE.getFirst() || packet.data > ALLOWED_DISTANCE.getSecond()) {
            player.connection.disconnect(ReadableString.translatable("kick_reason.iron_quarry.packet_blueprint_position_set.disallowed_distance", ALLOWED_DISTANCE.getFirst(), ALLOWED_DISTANCE.getSecond()));
            return false;
        }

        return true;
    }

    @Override
    protected void receiveFromClient(DirectionByteHashPacket payload, MinecraftServer server, ServerPlayer player) {
        if(!this.checkPacket(player, payload)) return;

        Inventory playerInventory = player.getInventory();
        ItemStack holdingStack = playerInventory.getSelectedItem();

        if(ZUtil.getBlockOrItem(holdingStack) instanceof BlueprintItem blueprintItem) {
            if(blueprintItem.isSealed(holdingStack)) return;

            blueprintItem.expandInDirection(holdingStack, player, payload.direction, payload.data);

            AbstractContainerMenu screenHandler = player.containerMenu;
            screenHandler.findSlot(playerInventory, playerInventory.getSelectedSlot()).ifPresent(slot -> screenHandler.setRemoteSlotUnsafe(slot, payload.hash));
        }
    }

    public static PacketBlueprintExpand bake(Direction direction, byte distance, HashedStack hash) {
        return new PacketBlueprintExpand(direction, distance, hash);
    }
}
