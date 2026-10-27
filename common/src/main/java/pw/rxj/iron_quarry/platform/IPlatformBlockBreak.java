package pw.rxj.iron_quarry.platform;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface IPlatformBlockBreak {
    boolean canOwnerBreak(ServerLevel world, GameProfile owner, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity);
    boolean beforeBreak(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity);
    void onBreakCanceled(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity);
    void afterBreak(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity);
}
