package pw.rxj.iron_quarry.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pw.rxj.iron_quarry.interfaces.IHandledCrafting;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.ShapedRecipe;

@Mixin(ShapedRecipe.class)
public class HandledShapedRecipeMixin {
    @Inject(method = "assemble(Lnet/minecraft/world/item/crafting/CraftingInput;)Lnet/minecraft/world/item/ItemStack;", at = @At(value = "RETURN"), cancellable = true)
    private void craft(CraftingInput input, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack centerStack = IHandledCrafting.getCenterStack(input);

        if(ZUtil.getBlockOrItem(centerStack) instanceof IHandledCrafting handledCrafting) {
            ItemStack result = handledCrafting.getCraftingOutput(cir.getReturnValue(), input);
            cir.setReturnValue(result);
        }
    }
}
