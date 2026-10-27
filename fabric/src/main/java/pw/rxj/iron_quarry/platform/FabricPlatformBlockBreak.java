package pw.rxj.iron_quarry.platform;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FabricPlatformBlockBreak implements IPlatformBlockBreak {
    @Override
    public boolean canOwnerBreak(ServerLevel world, GameProfile owner, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {
        FakePlayer fakePlayer = FakePlayer.get(world, owner);

        return PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(world, fakePlayer, pos, state, blockEntity);
    }

    @Override
    public boolean beforeBreak(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {
        return PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(world, player, pos, state, blockEntity);
    }

    @Override
    public void onBreakCanceled(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {
        PlayerBlockBreakEvents.CANCELED.invoker().onBlockBreakCanceled(world, player, pos, state, blockEntity);
    }

    @Override
    public void afterBreak(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {
        PlayerBlockBreakEvents.AFTER.invoker().afterBlockBreak(world, player, pos, state, blockEntity);
    }
}
