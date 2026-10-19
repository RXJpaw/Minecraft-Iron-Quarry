package pw.rxj.iron_quarry.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pw.rxj.iron_quarry.interfaces.IHandledCrafting;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.List;

@Mixin(ShapedRecipe.class)
public class HandledShapedRecipeMixin {
    @Inject(method = "craft(Lnet/minecraft/recipe/input/CraftingRecipeInput;Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)Lnet/minecraft/item/ItemStack;", at = @At(value = "HEAD"), cancellable = true)
    private void craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup wrapperLookup, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack centerStack = IHandledCrafting.getCenterStack(input);

        if(ZUtil.getBlockOrItem(centerStack) instanceof IHandledCrafting handledCrafting) {
            ItemStack result = handledCrafting.getCraftingOutput((ShapedRecipe) (Object) this, input, wrapperLookup);
            cir.setReturnValue(result);
        }
    }
}
