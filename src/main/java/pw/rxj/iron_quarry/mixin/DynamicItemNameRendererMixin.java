package pw.rxj.iron_quarry.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pw.rxj.iron_quarry.interfaces.IDynamicItemName;
import pw.rxj.iron_quarry.util.ZUtil;

@Mixin(ItemStack.class)
public abstract class DynamicItemNameRendererMixin {
    @Inject(method="getHoverName", at = @At(value = "HEAD"), cancellable = true)
    private void getName(CallbackInfoReturnable<Component> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        if(ZUtil.getBlockOrItem(stack) instanceof IDynamicItemName dynamicItemName) {
            Component customName = stack.get(DataComponents.CUSTOM_NAME);

            if (customName != null) {
                MutableComponent itemName = dynamicItemName.getDynamicItemName(stack).getText(customName);
                cir.setReturnValue(itemName);
                return;
            }

            MutableComponent itemName = dynamicItemName.getDynamicItemName(stack).getText(stack.getItem().getName(stack));
            cir.setReturnValue(itemName);
        }
    }
}
