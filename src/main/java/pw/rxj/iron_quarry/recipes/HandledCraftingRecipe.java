package pw.rxj.iron_quarry.recipes;

import com.google.gson.JsonObject;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import pw.rxj.iron_quarry.interfaces.IHandledCrafting;
import pw.rxj.iron_quarry.util.ZUtil;

public class HandledCraftingRecipe extends ShapedRecipe {
    public static final RecipeSerializer<HandledCraftingRecipe> SERIALIZER = new RecipeSerializer<>() {
        private ItemStack appendCraftingPreview(ShapedRecipe recipe) {
            ItemStack output = recipe.getOutput().copy();

            if(ZUtil.getBlockOrItem(output) instanceof IHandledCrafting handledCrafting) {
                ItemStack outputPreview = handledCrafting.getCraftingOutputPreview(recipe);
                if(outputPreview != null) return outputPreview;
            }

            return output;
        }

        @Override
        public HandledCraftingRecipe read(Identifier recipeId, JsonObject json) {
            ShapedRecipe recipe = RecipeSerializer.SHAPED.read(recipeId, json);

            ItemStack output = this.appendCraftingPreview(recipe);

            return new HandledCraftingRecipe(recipeId, recipe.getGroup(), recipe.getWidth(), recipe.getHeight(), recipe.getIngredients(), output);
        }

        @Override
        public HandledCraftingRecipe read(Identifier recipeId, PacketByteBuf buffer) {
            ShapedRecipe recipe = RecipeSerializer.SHAPED.read(recipeId, buffer);

            ItemStack output = this.appendCraftingPreview(recipe);

            return new HandledCraftingRecipe(recipeId, recipe.getGroup(), recipe.getWidth(), recipe.getHeight(), recipe.getIngredients(), output);
        }

        @Override
        public void write(PacketByteBuf buffer, HandledCraftingRecipe recipe) {
            RecipeSerializer.SHAPED.write(buffer, recipe);
        }
    };

    public HandledCraftingRecipe(Identifier id, String group, int width, int height, DefaultedList<Ingredient> input, ItemStack output) {
        super(id, group, width, height, input, output);
    }

    @Override
    public ItemStack craft(CraftingInventory craftingInventory) {
        ItemStack output = getOutput().copy();

        if(ZUtil.getBlockOrItem(output) instanceof IHandledCrafting handledCrafting) {
            return handledCrafting.getCraftingOutput(this, craftingInventory);
        }

        return super.craft(craftingInventory);
    }
}
