package pw.rxj.iron_quarry.mixin;

import net.minecraft.client.item.TooltipData;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pw.rxj.iron_quarry.gui.ITooltipDataProvider;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.Optional;

@Mixin(ItemStack.class)
public abstract class TooltipDataMixin {

    @Inject(method="getTooltipData", at = @At(value = "HEAD"), cancellable = true)
    private void onTooltipRender(CallbackInfoReturnable<Optional<TooltipData>> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        if(ZUtil.getBlockOrItem(stack) instanceof ITooltipDataProvider tooltipDataProvider) {
            cir.setReturnValue(tooltipDataProvider.getTooltipData(stack));
        }
    }
}
