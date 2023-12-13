package pw.rxj.iron_quarry.mixin;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pw.rxj.iron_quarry.event.InGameHudRenderCallback;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Inject(method = "render", at = @At(value = "HEAD"))
    public void render(MatrixStack matrices, float tickDelta, CallbackInfo callbackInfo) {
        matrices.push();

        matrices.translate(0, 0, -90);
        InGameHudRenderCallback.START.invoker().onStart(matrices, tickDelta);

        matrices.pop();
    }
}