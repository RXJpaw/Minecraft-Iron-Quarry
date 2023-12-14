package pw.rxj.iron_quarry.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.item.BlueprintItem;
import pw.rxj.iron_quarry.network.packet.DoubleBlockPosPacket;
import pw.rxj.iron_quarry.util.ZUtil;

public class PacketBlueprintPositionSet extends ComplexPacketHandler<DoubleBlockPosPacket> {
    protected static PacketBlueprintPositionSet INSTANCE = new PacketBlueprintPositionSet();

    private PacketBlueprintPositionSet() { }

    @Override
    public Identifier getChannelId() {
        return Identifier.of(Main.MOD_ID, "blueprint_position_set");
    }
    @Override
    public @Nullable DoubleBlockPosPacket read(PacketByteBuf buf) {
        return DoubleBlockPosPacket.read(buf);
    }

    @Override
    protected void receiveFromClient(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, @NotNull DoubleBlockPosPacket packet, PacketSender response) {
        PlayerInventory playerInventory = player.getInventory();
        ItemStack holdingStack = playerInventory.getMainHandStack();
        if(ZUtil.getBlockOrItem(holdingStack) instanceof BlueprintItem blueprintItem) {
            blueprintItem.setWorld(holdingStack, player.getWorld());
            blueprintItem.setFirstPos(holdingStack, packet.blockPos1);
            blueprintItem.setSecondPos(holdingStack, packet.blockPos2);
        }
    }

    public static @Nullable PacketByteBuf bake(@Nullable BlockPos firstPos, @Nullable BlockPos secondPos) {
        if(firstPos == null || secondPos == null) return null;

        return DoubleBlockPosPacket.write(INSTANCE.getChannelId(), firstPos, secondPos);
    }
}
