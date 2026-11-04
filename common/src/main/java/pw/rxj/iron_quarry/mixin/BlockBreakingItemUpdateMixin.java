package pw.rxj.iron_quarry.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pw.rxj.iron_quarry.interfaces.ISilentComponentUpdate;

@Mixin(MultiPlayerGameMode.class)
public abstract class BlockBreakingItemUpdateMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow private ItemStack destroyingItem;

    @Inject(method = "sameDestroyTarget", at = @At("HEAD"))
    private void keepDestroyingOnSilentUpdate(BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {
        LocalPlayer player = this.minecraft.player;
        if(player == null) return;

        ItemStack mainHandStack = player.getMainHandItem();
        if(ISilentComponentUpdate.isSilent(this.destroyingItem, mainHandStack)) this.destroyingItem = mainHandStack;
    }
}
