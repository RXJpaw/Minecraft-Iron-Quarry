package pw.rxj.iron_quarry.mixin;

import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pw.rxj.iron_quarry.interfaces.IHandledGrinding;
import pw.rxj.iron_quarry.util.ZUtil;

/** {@link GrindstoneMenu} */
@Mixin(targets = {"net.minecraft.world.inventory.GrindstoneMenu$2", "net.minecraft.world.inventory.GrindstoneMenu$3"})
public abstract class GrindstoneInsertionMixin {
    @Inject(method = "mayPlace", at = @At(value = "RETURN"), cancellable = true)
    private void canInsert(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if(ZUtil.getBlockOrItem(stack) instanceof IHandledGrinding handledGrinding) {
            if(handledGrinding.isGrindable(stack)) cir.setReturnValue(true);
        }
    }
}
