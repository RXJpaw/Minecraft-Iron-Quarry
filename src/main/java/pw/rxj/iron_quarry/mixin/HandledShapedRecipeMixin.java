package pw.rxj.iron_quarry.mixin;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pw.rxj.iron_quarry.interfaces.IHandledCrafting;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.List;

@Mixin(ShapedRecipe.class)
public class HandledShapedRecipeMixin {
    @Inject(method = "craft(Lnet/minecraft/inventory/RecipeInputInventory;Lnet/minecraft/registry/DynamicRegistryManager;)Lnet/minecraft/item/ItemStack;", at = @At(value = "HEAD"), cancellable = true)
    private void getResult(RecipeInputInventory recipeInputInventory, DynamicRegistryManager dynamicRegistryManager, CallbackInfoReturnable<ItemStack> cir) {
        List<ItemStack> stacks = recipeInputInventory.getHeldStacks();

        if(ZUtil.getBlockOrItem(stacks.get(4)) instanceof IHandledCrafting handledCrafting) {
            ItemStack result = handledCrafting.getCraftingOutput((ShapedRecipe) (Object) this, recipeInputInventory, dynamicRegistryManager);

            cir.setReturnValue(result);
        }
    }
}
