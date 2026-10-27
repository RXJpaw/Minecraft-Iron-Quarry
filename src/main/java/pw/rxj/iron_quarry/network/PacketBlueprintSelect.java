package pw.rxj.iron_quarry.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.core.BlockPos;
import net.minecraft.network.HashedStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import pw.rxj.iron_quarry.item.BlueprintItem;
import pw.rxj.iron_quarry.network.packet.ByteBlockPosHashPacket;
import pw.rxj.iron_quarry.types.DualPosition;
import pw.rxj.iron_quarry.util.ZUtil;

public class PacketBlueprintSelect extends ByteBlockPosHashPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketBlueprintSelect> PACKET_CODEC = StreamCodec.ofMember(PacketBlueprintSelect::write, PacketBlueprintSelect::new);
    public static final CustomPacketPayload.Type<PacketBlueprintSelect> PACKET_ID = register("packet_blueprint_select", PACKET_CODEC);

    public static final double MAX_DISTANCE = 256.0;
    private static final double MAX_DISTANCE_TOLERANCE = 16.0;

    private PacketBlueprintSelect(byte data, BlockPos blockPos, HashedStack hash) {
        super(data, blockPos, hash);
    }
    private PacketBlueprintSelect(RegistryFriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public Type<PacketBlueprintSelect> type() {
        return PACKET_ID;
    }

    @Override
    protected void receiveFromClient(ByteBlockPosHashPacket payload, MinecraftServer server, ServerPlayer player, PacketSender responseSender) {
        DualPosition position = DualPosition.ID_TO_VALUE.apply(payload.data);

        Inventory playerInventory = player.getInventory();
        ItemStack holdingStack = playerInventory.getSelectedItem();

        if(ZUtil.getBlockOrItem(holdingStack) instanceof BlueprintItem blueprintItem) {
            if(!blueprintItem.isSealed(holdingStack) && isWithinDistance(player, payload.blockPos)) {
                blueprintItem.setPosition(position, holdingStack, player.level(), payload.blockPos);
            }

            AbstractContainerMenu screenHandler = player.containerMenu;
            screenHandler.findSlot(playerInventory, playerInventory.getSelectedSlot()).ifPresent(slot -> screenHandler.setRemoteSlotUnsafe(slot, payload.hash));
        }
    }

    private static boolean isWithinDistance(ServerPlayer player, BlockPos blockPos) {
        double maxDistance = MAX_DISTANCE + MAX_DISTANCE_TOLERANCE;

        return player.getEyePosition().distanceToSqr(Vec3.atCenterOf(blockPos)) <= maxDistance * maxDistance;
    }

    public static PacketBlueprintSelect bake(DualPosition position, BlockPos blockPos, HashedStack hash) {
        return new PacketBlueprintSelect((byte) position.getId(), blockPos, hash);
    }
}
