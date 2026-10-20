package pw.rxj.iron_quarry.interfaces;

import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

public interface IHandledKeyedAction {
    default void keyedAttackOnBlock(String keyName, UseOnContext context, BlockState blockState) {}
    default void keyedUseOnBlock(String keyName, UseOnContext context, BlockState blockState) {}
    default void keyedUse(String keyName, UseOnContext context) {}
}
