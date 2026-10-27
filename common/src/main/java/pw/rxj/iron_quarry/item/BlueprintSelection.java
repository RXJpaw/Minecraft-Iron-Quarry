package pw.rxj.iron_quarry.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.HashedStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import pw.rxj.iron_quarry.network.PacketBlueprintExpand;
import pw.rxj.iron_quarry.network.PacketBlueprintSelect;
import pw.rxj.iron_quarry.network.ZNetwork;
import pw.rxj.iron_quarry.types.DualPosition;
import pw.rxj.iron_quarry.types.ScrollDirection;
import pw.rxj.iron_quarry.util.ReadableString;
import pw.rxj.iron_quarry.util.ZUtil;

@Environment(EnvType.CLIENT)
public class BlueprintSelection {
    private static final int REPEAT_TICKS = 10;

    private static int attackDelay = 0;

    public static boolean scroll(BlueprintItem blueprintItem, LocalPlayer player, ItemStack stack, ScrollDirection scrollDirection) {
        if(!Minecraft.getInstance().options.keyShift.isDown() || blueprintItem.isSealed(stack)) return false;

        Direction direction = ZUtil.getFacingDirection(player.getYRot(), player.getXRot());
        int multiplier = Minecraft.getInstance().options.keySprint.isDown() ? 16 : 1;

        int amount = switch(scrollDirection) {
            case FORWARDS -> multiplier;
            case BACKWARDS -> -multiplier;
            default -> 0;
        };

        if(blueprintItem.expandInDirection(stack, player, direction, amount)) {
            ZNetwork.sendToServer(PacketBlueprintExpand.bake(direction, (byte) amount, hash(player, stack)));

            return true;
        }

        return false;
    }

    public static boolean attack(BlueprintItem blueprintItem, LocalPlayer player, ItemStack stack, int clickCount) {
        if(blueprintItem.isSealed(stack)) return false;

        if(clickCount > 0 || --attackDelay <= 0) {
            select(blueprintItem, player, DualPosition.FIRST);
            player.swing(InteractionHand.MAIN_HAND);
            attackDelay = REPEAT_TICKS;
        }

        return true;
    }

    public static void use(BlueprintItem blueprintItem, Player player) {
        select(blueprintItem, player, DualPosition.SECOND);
        player.swing(InteractionHand.MAIN_HAND);

        Minecraft.getInstance().rightClickDelay = REPEAT_TICKS;
    }

    private static void select(BlueprintItem blueprintItem, Player player, DualPosition position) {
        if(!(player instanceof LocalPlayer localPlayer)) return;
        if(!(localPlayer.pick(PacketBlueprintSelect.MAX_DISTANCE, 1.0F, false) instanceof BlockHitResult hitResult)) return;
        if(hitResult.getType() != HitResult.Type.BLOCK) return;

        BlockPos targetedPos = hitResult.getBlockPos();
        ItemStack stack = localPlayer.getMainHandItem();
        int previousHash = ItemStack.hashItemAndComponents(stack);

        blueprintItem.setPosition(position, stack, localPlayer.level(), targetedPos);

        if(previousHash != ItemStack.hashItemAndComponents(stack)) {
            ZNetwork.sendToServer(PacketBlueprintSelect.bake(position, targetedPos, hash(localPlayer, stack)));
        }

        showOverlay(localPlayer, position, targetedPos);
    }

    private static void showOverlay(LocalPlayer player, DualPosition position, BlockPos blockPos) {
        Component positionText = ReadableString.textFrom(blockPos).orElse(ReadableString.ERROR);
        String translationKey = position.equals(DualPosition.FIRST) ? "item.iron_quarry.blueprint.overlay.first_pos_set" : "item.iron_quarry.blueprint.overlay.second_pos_set";

        player.sendOverlayMessage(ReadableString.translatable(translationKey, positionText));
    }
    private static HashedStack hash(LocalPlayer player, ItemStack stack) {
        return HashedStack.create(stack, player.connection.decoratedHashOpsGenenerator());
    }
}
