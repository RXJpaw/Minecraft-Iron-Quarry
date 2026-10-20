package pw.rxj.iron_quarry.mixin;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pw.rxj.iron_quarry.gui.TooltipInventoryComponent;
import pw.rxj.iron_quarry.gui.TooltipInventoryData;
import pw.rxj.iron_quarry.gui.TooltipQuarryInventoryComponent;
import pw.rxj.iron_quarry.gui.TooltipQuarryInventoryData;

@Mixin(ClientTooltipComponent.class)
public interface TooltipComponentMixin {

    @Inject(method="create(Lnet/minecraft/world/inventory/tooltip/TooltipComponent;)Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;", at = @At(value = "HEAD"), cancellable = true)
    private static void onTooltipRender(TooltipComponent data, CallbackInfoReturnable<ClientTooltipComponent> cir) {
        if(data instanceof TooltipQuarryInventoryData augmentInventoryData) {
            cir.setReturnValue(TooltipQuarryInventoryComponent.of(augmentInventoryData));
        } else if(data instanceof TooltipInventoryData inventoryData) {
            cir.setReturnValue(TooltipInventoryComponent.of(inventoryData));
        }
    }
}
