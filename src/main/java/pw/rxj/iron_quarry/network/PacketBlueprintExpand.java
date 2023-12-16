package pw.rxj.iron_quarry.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.item.BlueprintItem;
import pw.rxj.iron_quarry.network.packet.DirectionBytePacket;
import pw.rxj.iron_quarry.util.ReadableString;
import pw.rxj.iron_quarry.util.ZUtil;

public class PacketBlueprintExpand extends ComplexPacketHandler<DirectionBytePacket> {
    public static Pair<Integer, Integer> ALLOWED_DISTANCE = new Pair<>(-16, 16);

    protected static PacketBlueprintExpand INSTANCE = new PacketBlueprintExpand();

    private PacketBlueprintExpand() { }

    @Override
    public Identifier getChannelId() {
        return Identifier.of(Main.MOD_ID, "blueprint_position_set");
    }
    @Override
    public @Nullable DirectionBytePacket read(PacketByteBuf buf) {
        return DirectionBytePacket.read(buf);
    }

    protected boolean checkPacket(ServerPlayerEntity player, DirectionBytePacket packet) {
        if(packet.data < ALLOWED_DISTANCE.getA() || packet.data > ALLOWED_DISTANCE.getB()) {
            player.networkHandler.disconnect(ReadableString.translatable("kick_reason.iron_quarry.packet_blueprint_position_set.disallowed_distance", ALLOWED_DISTANCE.getA(), ALLOWED_DISTANCE.getB()));
            return false;
        }

        return true;
    }

    @Override
    protected void receiveFromClient(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, @NotNull DirectionBytePacket packet, PacketSender response) {
        if(!this.checkPacket(player, packet)) return;

        PlayerInventory playerInventory = player.getInventory();
        ItemStack holdingStack = playerInventory.getMainHandStack();

        if(ZUtil.getBlockOrItem(holdingStack) instanceof BlueprintItem blueprintItem) {
            if(blueprintItem.isSealed(holdingStack)) return;

            blueprintItem.expandInDirection(holdingStack, player, packet.direction, packet.data);
        }
    }

    public static PacketByteBuf bake(Direction direction, byte distance) {
        return DirectionBytePacket.write(INSTANCE.getChannelId(), direction, distance);
    }
}
