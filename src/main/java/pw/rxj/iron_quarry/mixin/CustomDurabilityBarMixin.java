package pw.rxj.iron_quarry.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pw.rxj.iron_quarry.interfaces.ICustomDurability;

@Mixin(DrawContext.class)
public abstract class CustomDurabilityBarMixin {
    @Inject(method="drawItemBar(Lnet/minecraft/item/ItemStack;II)V", at = @At(value = "TAIL"))
    private void renderGuiItemOverlay(ItemStack stack, int x, int y, CallbackInfo ci) {
        if (stack.getItem() instanceof ICustomDurability customDurability && customDurability.getCDamage(stack) > 0) {
            DrawContext context = (DrawContext) (Object) this;

            int step = customDurability.getCItemBarStep(stack);
            int color = customDurability.getCItemBarColor(stack);
            int fillX = x + 2;
            int fillY = y + 13;

            context.fill(RenderLayer.getGuiOverlay(), fillX, fillY, fillX + 13,   fillY + 2, 200, -16777216);
            context.fill(RenderLayer.getGuiOverlay(), fillX, fillY, fillX + step, fillY + 1, 200, color | -16777216);
        }
    }
}
