package pw.rxj.iron_quarry.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import pw.rxj.iron_quarry.interfaces.IHandledKeyedAction;
import pw.rxj.iron_quarry.network.packet.StringByteByteBlockHitResultPacket;
import pw.rxj.iron_quarry.types.ActionGoal;
import pw.rxj.iron_quarry.util.ZUtil;

public class PacketKeyedAction extends StringByteByteBlockHitResultPacket {
    public static final PacketCodec<RegistryByteBuf, PacketKeyedAction> PACKET_CODEC = PacketCodec.of(PacketKeyedAction::write, PacketKeyedAction::new);
    public static final CustomPayload.Id<PacketKeyedAction> PACKET_ID = register("blueprint_expand", PACKET_CODEC);

    private PacketKeyedAction(String string, Byte byte0, Byte byte1, BlockHitResult hitResult) {
        super(string, byte0, byte1, hitResult);
    }
    private PacketKeyedAction(RegistryByteBuf buf) {
        super(buf);
    }

    @Override
    public Id<PacketKeyedAction> getId() {
        return PACKET_ID;
    }

    @Override
    protected void receiveFromClient(StringByteByteBlockHitResultPacket payload, MinecraftServer server, ServerPlayerEntity player, PacketSender responseSender) {
        Hand hand = ZUtil.safeEnumConstant(Hand.class, payload.byte0);
        if(hand == null) return;
        ActionGoal actionGoal = ZUtil.safeEnumConstant(ActionGoal.class, payload.byte1);
        if(actionGoal == null) return;

        String keyName = payload.string;
        BlockHitResult hitResult = payload.hitResult;

        ItemUsageContext context = new ItemUsageContext(player, hand, hitResult);

        switch(actionGoal) {
            case USE -> {
                ItemStack stack = player.getStackInHand(hand);

                if(ZUtil.getBlockOrItem(stack) instanceof IHandledKeyedAction handledKeyedAction) {
                    handledKeyedAction.keyedUse(keyName, context);
                }
            }
            case USE_ON_BLOCK -> {
                if(hitResult.getType().equals(HitResult.Type.MISS)) break;

                BlockState blockState = context.getWorld().getBlockState(hitResult.getBlockPos());

                if(blockState.getBlock() instanceof IHandledKeyedAction handledKeyedAction) {
                    handledKeyedAction.keyedUseOnBlock(keyName, context, blockState);
                }
            }
            case ATTACK_ON_BLOCK -> {
                if(hitResult.getType().equals(HitResult.Type.MISS)) break;

                BlockState blockState = context.getWorld().getBlockState(hitResult.getBlockPos());

                if(blockState.getBlock() instanceof IHandledKeyedAction handledKeyedAction) {
                    handledKeyedAction.keyedAttackOnBlock(keyName, context, blockState);
                }
            }
        }
    }

    public static PacketKeyedAction bake(String keyName, Hand hand, ActionGoal actionGoal, BlockHitResult hitResult) {
        return new PacketKeyedAction(keyName, (byte) hand.ordinal(), (byte) actionGoal.ordinal(), hitResult);
    }
}
