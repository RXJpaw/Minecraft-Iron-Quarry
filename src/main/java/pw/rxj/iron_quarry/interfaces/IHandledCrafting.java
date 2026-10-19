package pw.rxj.iron_quarry.interfaces;

import com.mojang.datafixers.util.Function3;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import pw.rxj.iron_quarry.util.ZUtil;

public interface IHandledCrafting {
    Function3<ShapedRecipe, CraftingRecipeInput, RegistryWrapper.WrapperLookup, ItemStack> COPY_CENTER_SLOT_NBT = (handler, input, wrapperLookup) -> {
        ItemStack output = handler.getResult(wrapperLookup);

        ItemStack stack = getCenterStack(input);
        if(ZUtil.getBlockOrItem(stack) instanceof IHandledCrafting) {
            output = stack.copyComponentsToNewStack(output.getItem(), output.getCount());
        }

        return output;
    };

    ItemStack getCraftingOutput(ShapedRecipe handler, CraftingRecipeInput input, RegistryWrapper.WrapperLookup wrapperLookup);

    static ItemStack getCenterStack(CraftingRecipeInput input) {
        if(input.getWidth() != 3 || input.getHeight() != 3) return ItemStack.EMPTY;

        return input.getStackInSlot(1, 1);
    }

    default ItemStack getCraftingOutputPreview(CraftingRecipe recipe) {
        return null;
    }
}
