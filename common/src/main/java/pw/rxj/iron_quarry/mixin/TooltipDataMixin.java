package pw.rxj.iron_quarry.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pw.rxj.iron_quarry.gui.ITooltipDataProvider;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.Optional;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemStack.class)
public abstract class TooltipDataMixin {

    @Inject(method="getTooltipImage", at = @At(value = "HEAD"), cancellable = true)
    private void onTooltipRender(CallbackInfoReturnable<Optional<TooltipComponent>> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        if(ZUtil.getBlockOrItem(stack) instanceof ITooltipDataProvider tooltipDataProvider) {
            cir.setReturnValue(tooltipDataProvider.getTooltipImage(stack));
        }
    }
}
