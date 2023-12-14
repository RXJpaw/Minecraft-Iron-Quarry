package pw.rxj.iron_quarry.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pw.rxj.iron_quarry.interfaces.IHandledMainHandScrolling;
import pw.rxj.iron_quarry.types.ScrollDirection;
import pw.rxj.iron_quarry.util.ZUtil;

@Mixin(Mouse.class)
public abstract class MouseScrollMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;scrollInHotbar(D)V", shift = At.Shift.BEFORE), cancellable = true)
    public void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        //this.client.player was already checked in injected method.
        assert this.client.player != null;

        ItemStack stack = this.client.player.getStackInHand(Hand.MAIN_HAND);
        if (ZUtil.getBlockOrItem(stack) instanceof IHandledMainHandScrolling handledMainHandScrolling) {
            if(handledMainHandScrolling.handleMainHandScrolling(this.client.player, stack, ScrollDirection.from(vertical))) ci.cancel();
        }
    }
}
