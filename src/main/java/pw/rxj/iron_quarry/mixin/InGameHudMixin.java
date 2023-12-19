package pw.rxj.iron_quarry.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pw.rxj.iron_quarry.event.InGameHudRenderCallback;
import pw.rxj.iron_quarry.interfaces.IAlwaysRenderItemName;
import pw.rxj.iron_quarry.util.ZUtil;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Inject(method = "render", at = @At(value = "HEAD"))
    public void render(MatrixStack matrices, float tickDelta, CallbackInfo callbackInfo) {
        matrices.push();

        matrices.translate(0, 0, -90);
        InGameHudRenderCallback.START.invoker().onStart(matrices, tickDelta);

        matrices.pop();
    }

    @Shadow @Final private MinecraftClient client;
    @Shadow private int heldItemTooltipFade;

    @Inject(method = "tick()V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;player:Lnet/minecraft/client/network/ClientPlayerEntity;", ordinal = 1, shift = At.Shift.BEFORE))
    public void tick(CallbackInfo ci) {
        //this.client.player was already checked in injected method.
        assert this.client.player != null;

        ItemStack stack = this.client.player.getInventory().getMainHandStack();

        if(ZUtil.getBlockOrItem(stack) instanceof IAlwaysRenderItemName alwaysRenderItemName) {
            if(alwaysRenderItemName.renderItemName(stack)) this.heldItemTooltipFade = 40;
        }
    }
}