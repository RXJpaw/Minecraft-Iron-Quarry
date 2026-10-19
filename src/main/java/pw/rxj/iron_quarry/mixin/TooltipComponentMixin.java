package pw.rxj.iron_quarry.mixin;

import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.tooltip.TooltipData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pw.rxj.iron_quarry.gui.TooltipInventoryComponent;
import pw.rxj.iron_quarry.gui.TooltipInventoryData;
import pw.rxj.iron_quarry.gui.TooltipQuarryInventoryComponent;
import pw.rxj.iron_quarry.gui.TooltipQuarryInventoryData;

@Mixin(TooltipComponent.class)
public interface TooltipComponentMixin {

    @Inject(method="of(Lnet/minecraft/item/tooltip/TooltipData;)Lnet/minecraft/client/gui/tooltip/TooltipComponent;", at = @At(value = "HEAD"), cancellable = true)
    private static void onTooltipRender(TooltipData data, CallbackInfoReturnable<TooltipComponent> cir) {
        if(data instanceof TooltipQuarryInventoryData augmentInventoryData) {
            cir.setReturnValue(TooltipQuarryInventoryComponent.of(augmentInventoryData));
        } else if(data instanceof TooltipInventoryData inventoryData) {
            cir.setReturnValue(TooltipInventoryComponent.of(inventoryData));
        }
    }
}
