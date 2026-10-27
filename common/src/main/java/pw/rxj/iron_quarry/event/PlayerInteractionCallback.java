package pw.rxj.iron_quarry.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public interface PlayerInteractionCallback {
    Event<AttackBlock> ATTACK_BLOCK = Event.createArrayBacked(AttackBlock.class, (listeners) -> (player, world, hand, pos, direction) -> {
        for (AttackBlock event : listeners) {
            InteractionResult result = event.onAttackBlock(player, world, hand, pos, direction);
            if(!result.equals(InteractionResult.PASS)) return result;
        }

        return InteractionResult.PASS;
    });
    Event<UseBlock> USE_BLOCK = Event.createArrayBacked(UseBlock.class, (listeners) -> (player, world, hand, hitResult) -> {
        for (UseBlock event : listeners) {
            InteractionResult result = event.onUseBlock(player, world, hand, hitResult);
            if(!result.equals(InteractionResult.PASS)) return result;
        }

        return InteractionResult.PASS;
    });

    interface AttackBlock {
        InteractionResult onAttackBlock(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction);
    }
    interface UseBlock {
        InteractionResult onUseBlock(Player player, Level world, InteractionHand hand, BlockHitResult hitResult);
    }
}
