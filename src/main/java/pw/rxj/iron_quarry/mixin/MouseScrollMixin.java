package pw.rxj.iron_quarry.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pw.rxj.iron_quarry.interfaces.IHandledMainHandScroll;
import pw.rxj.iron_quarry.types.ScrollDirection;
import pw.rxj.iron_quarry.util.ZUtil;

@Mixin(MouseHandler.class)
public abstract class MouseScrollMixin {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;setSelectedSlot(I)V", shift = At.Shift.BEFORE), cancellable = true)
    public void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        //this.client.player was already checked in injected method.
        assert this.minecraft.player != null;

        ItemStack stack = this.minecraft.player.getItemInHand(InteractionHand.MAIN_HAND);
        if (ZUtil.getBlockOrItem(stack) instanceof IHandledMainHandScroll handledMainHandScroll) {
            if(handledMainHandScroll.handleMainHandScroll(this.minecraft.player, stack, ScrollDirection.from(vertical))) ci.cancel();
        }
    }
}
