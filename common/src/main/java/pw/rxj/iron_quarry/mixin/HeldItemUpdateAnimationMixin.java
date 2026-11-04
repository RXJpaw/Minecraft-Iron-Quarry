package pw.rxj.iron_quarry.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pw.rxj.iron_quarry.interfaces.ISilentComponentUpdate;

@Mixin(ItemInHandRenderer.class)
public abstract class HeldItemUpdateAnimationMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow private ItemStack mainHandItem;
    @Shadow private ItemStack offHandItem;

    @Inject(method = "tick", at = @At("HEAD"))
    private void skipSilentUpdateAnimation(CallbackInfo ci) {
        LocalPlayer player = this.minecraft.player;
        if(player == null) return;

        ItemStack mainHandStack = player.getMainHandItem();
        if(ISilentComponentUpdate.isSilent(this.mainHandItem, mainHandStack)) this.mainHandItem = mainHandStack;

        ItemStack offHandStack = player.getOffhandItem();
        if(ISilentComponentUpdate.isSilent(this.offHandItem, offHandStack)) this.offHandItem = offHandStack;
    }
}
