package pw.rxj.iron_quarry.interfaces;

import com.mojang.datafixers.util.Function3;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.registry.RegistryWrapper;
import pw.rxj.iron_quarry.util.ZUtil;

public interface IHandledCrafting {
    Function3<ShapedRecipe, RecipeInputInventory, RegistryWrapper.WrapperLookup, ItemStack> COPY_CENTER_SLOT_NBT = (handler, recipeInputInventory, wrapperLookup) -> {
        ItemStack output = handler.getResult(wrapperLookup);

        ItemStack stack = recipeInputInventory.getStack(4);
        if(ZUtil.getBlockOrItem(stack) instanceof IHandledCrafting) {
            output = stack.copyComponentsToNewStack(output.getItem(), output.getCount());
        }

        return output;
    };

    ItemStack getCraftingOutput(ShapedRecipe handler, RecipeInputInventory recipeInputInventory, RegistryWrapper.WrapperLookup wrapperLookup);

    default ItemStack getCraftingOutputPreview(CraftingRecipe recipe) {
        return null;
    }
}
