package pw.rxj.iron_quarry.platform;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import org.jetbrains.annotations.Nullable;

public class NeoForgePlatformBlockBreak implements IPlatformBlockBreak {
    @Override
    public boolean canOwnerBreak(ServerLevel world, GameProfile owner, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {
        return this.beforeBreak(world, FakePlayerFactory.get(world, owner), pos, state, blockEntity);
    }

    @Override
    public boolean beforeBreak(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {
        return !NeoForge.EVENT_BUS.post(new BreakBlockEvent(world, pos, state, player)).isCanceled();
    }

    @Override
    public void onBreakCanceled(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {
    }

    @Override
    public void afterBreak(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {
    }
}
