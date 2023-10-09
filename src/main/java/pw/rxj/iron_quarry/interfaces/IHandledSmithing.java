package pw.rxj.iron_quarry.interfaces;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.screen.ScreenHandlerContext;
import pw.rxj.iron_quarry.recipes.HandledSmithingRecipe;

public interface IHandledSmithing {
    ItemStack getSmithingOutput(HandledSmithingRecipe handler, Inventory inventory);

    default ItemStack getSmithingOutputPreview(Ingredient base, Ingredient addition, ItemStack output) {
        return null;
    }

    default Boolean handleSmithingTakeOutput(PlayerEntity player, Inventory inputInv, CraftingResultInventory outputInv, ItemStack output, ScreenHandlerContext context) {
        return false;
    }
}