package pw.rxj.iron_quarry.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.GrindstoneScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pw.rxj.iron_quarry.interfaces.IHandledGrinding;
import pw.rxj.iron_quarry.util.ZUtil;

/** {@link GrindstoneScreenHandler} */
@Mixin(targets = {"net.minecraft.screen.GrindstoneScreenHandler$2", "net.minecraft.screen.GrindstoneScreenHandler$3"})
public abstract class GrindstoneInsertionMixin {
    @Inject(method = "canInsert", at = @At(value = "RETURN"), cancellable = true)
    private void onTakeOutput(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if(ZUtil.getBlockOrItem(stack) instanceof IHandledGrinding handledGrinding) {
            if(handledGrinding.isGrindable(stack)) cir.setReturnValue(true);
        }
    }
}
