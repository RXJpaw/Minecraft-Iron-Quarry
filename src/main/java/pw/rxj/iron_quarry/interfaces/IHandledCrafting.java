package pw.rxj.iron_quarry.interfaces;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import pw.rxj.iron_quarry.util.ZUtil;

public interface IHandledCrafting {
    static ItemStack copyCenterSlotComponents(ItemStack output, CraftingRecipeInput input) {
        ItemStack stack = getCenterStack(input);
        if(ZUtil.getBlockOrItem(stack) instanceof IHandledCrafting) {
            return stack.copyComponentsToNewStack(output.getItem(), output.getCount());
        }

        return output;
    }

    ItemStack getCraftingOutput(ItemStack output, CraftingRecipeInput input, RegistryWrapper.WrapperLookup wrapperLookup);

    static ItemStack getCenterStack(CraftingRecipeInput input) {
        if(input.getWidth() != 3 || input.getHeight() != 3) return ItemStack.EMPTY;

        return input.getStackInSlot(1, 1);
    }

    default ItemStack getCraftingOutputPreview(CraftingRecipe recipe) {
        return null;
    }
}
