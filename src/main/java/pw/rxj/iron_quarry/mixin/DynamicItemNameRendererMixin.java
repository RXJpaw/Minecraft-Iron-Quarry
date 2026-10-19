package pw.rxj.iron_quarry.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pw.rxj.iron_quarry.interfaces.IDynamicItemName;
import pw.rxj.iron_quarry.util.ZUtil;

@Mixin(ItemStack.class)
public abstract class DynamicItemNameRendererMixin {
    @Inject(method="getName", at = @At(value = "HEAD"), cancellable = true)
    private void getName(CallbackInfoReturnable<Text> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        if(ZUtil.getBlockOrItem(stack) instanceof IDynamicItemName dynamicItemName) {
            Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);

            if (customName != null) {
                MutableText itemName = dynamicItemName.getDynamicItemName(stack).getText(customName);
                cir.setReturnValue(itemName);
                return;
            }

            MutableText itemName = dynamicItemName.getDynamicItemName(stack).getText(stack.getItem().getName(stack));
            cir.setReturnValue(itemName);
        }
    }
}
