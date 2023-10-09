package pw.rxj.iron_quarry.interfaces;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import pw.rxj.iron_quarry.recipes.HandledCraftingRecipe;

public interface IHandledCrafting {
    ItemStack getCraftingOutput(HandledCraftingRecipe handler, CraftingInventory craftingInventory);

    default ItemStack getCraftingOutputPreview(CraftingRecipe recipe) {
        return null;
    }
}
