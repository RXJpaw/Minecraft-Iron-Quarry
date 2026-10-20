package pw.rxj.iron_quarry.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import pw.rxj.iron_quarry.interfaces.IHandledKeyedAction;
import pw.rxj.iron_quarry.network.packet.StringByteByteBlockHitResultPacket;
import pw.rxj.iron_quarry.types.ActionGoal;
import pw.rxj.iron_quarry.util.ZUtil;

public class PacketKeyedAction extends StringByteByteBlockHitResultPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketKeyedAction> PACKET_CODEC = StreamCodec.ofMember(PacketKeyedAction::write, PacketKeyedAction::new);
    public static final CustomPacketPayload.Type<PacketKeyedAction> PACKET_ID = register("blueprint_expand", PACKET_CODEC);

    private PacketKeyedAction(String string, Byte byte0, Byte byte1, BlockHitResult hitResult) {
        super(string, byte0, byte1, hitResult);
    }
    private PacketKeyedAction(RegistryFriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public Type<PacketKeyedAction> type() {
        return PACKET_ID;
    }

    @Override
    protected void receiveFromClient(StringByteByteBlockHitResultPacket payload, MinecraftServer server, ServerPlayer player, PacketSender responseSender) {
        InteractionHand hand = ZUtil.safeEnumConstant(InteractionHand.class, payload.byte0);
        if(hand == null) return;
        ActionGoal actionGoal = ZUtil.safeEnumConstant(ActionGoal.class, payload.byte1);
        if(actionGoal == null) return;

        String keyName = payload.string;
        BlockHitResult hitResult = payload.hitResult;

        UseOnContext context = new UseOnContext(player, hand, hitResult);

        switch(actionGoal) {
            case USE -> {
                ItemStack stack = player.getItemInHand(hand);

                if(ZUtil.getBlockOrItem(stack) instanceof IHandledKeyedAction handledKeyedAction) {
                    handledKeyedAction.keyedUse(keyName, context);
                }
            }
            case USE_ON_BLOCK -> {
                if(hitResult.getType().equals(HitResult.Type.MISS)) break;

                BlockState blockState = context.getLevel().getBlockState(hitResult.getBlockPos());

                if(blockState.getBlock() instanceof IHandledKeyedAction handledKeyedAction) {
                    handledKeyedAction.keyedUseOnBlock(keyName, context, blockState);
                }
            }
            case ATTACK_ON_BLOCK -> {
                if(hitResult.getType().equals(HitResult.Type.MISS)) break;

                BlockState blockState = context.getLevel().getBlockState(hitResult.getBlockPos());

                if(blockState.getBlock() instanceof IHandledKeyedAction handledKeyedAction) {
                    handledKeyedAction.keyedAttackOnBlock(keyName, context, blockState);
                }
            }
        }
    }

    public static PacketKeyedAction bake(String keyName, InteractionHand hand, ActionGoal actionGoal, BlockHitResult hitResult) {
        return new PacketKeyedAction(keyName, (byte) hand.ordinal(), (byte) actionGoal.ordinal(), hitResult);
    }
}
