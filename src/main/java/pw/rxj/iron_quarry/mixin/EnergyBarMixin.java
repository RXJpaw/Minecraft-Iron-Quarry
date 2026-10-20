package pw.rxj.iron_quarry.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pw.rxj.iron_quarry.render.EnergyBarRenderer;

@Mixin(GuiGraphics.class)
public abstract class EnergyBarMixin {
    @Inject(method="renderItemBar(Lnet/minecraft/world/item/ItemStack;II)V", at = @At(value = "TAIL"))
    private void renderGuiItemOverlay(ItemStack stack, int x, int y, CallbackInfo ci) {
        EnergyBarRenderer.renderGuiItemOverlay((GuiGraphics) (Object) this, stack, x, y);
    }
}
