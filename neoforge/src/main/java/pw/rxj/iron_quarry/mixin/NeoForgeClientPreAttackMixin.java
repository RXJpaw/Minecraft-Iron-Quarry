package pw.rxj.iron_quarry.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pw.rxj.iron_quarry.event.ClientInteractionCallback;

@Mixin(Minecraft.class)
public abstract class NeoForgeClientPreAttackMixin {
    @Shadow public @Nullable LocalPlayer player;
    @Shadow public Options options;
    @Shadow public @Nullable MultiPlayerGameMode gameMode;

    @Unique
    private boolean attackCancelled = false;

    @Inject(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z", ordinal = 0))
    private void handleKeybinds(CallbackInfo ci) {
        int clickCount = this.options.keyAttack.clickCount;

        if(this.options.keyAttack.isDown() || clickCount != 0) {
            this.attackCancelled = ClientInteractionCallback.PRE_ATTACK.invoker().onPreAttack(this.player, clickCount);
        } else {
            this.attackCancelled = false;
        }
    }

    @Inject(method = "startAttack", at = @At(value = "HEAD"), cancellable = true)
    private void startAttack(CallbackInfoReturnable<Boolean> cir) {
        if(this.attackCancelled) cir.setReturnValue(false);
    }

    @Inject(method = "continueAttack", at = @At(value = "HEAD"), cancellable = true)
    private void continueAttack(boolean down, CallbackInfo ci) {
        if(!this.attackCancelled) return;

        if(this.gameMode != null) this.gameMode.stopDestroyBlock();
        ci.cancel();
    }
}
