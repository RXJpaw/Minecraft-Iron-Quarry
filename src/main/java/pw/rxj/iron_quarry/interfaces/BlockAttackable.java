package pw.rxj.iron_quarry.interfaces;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public interface BlockAttackable {
    ActionResult attackOnBlock(PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction);
}
