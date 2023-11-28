package pw.rxj.iron_quarry.recipes;

import com.google.gson.JsonObject;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import pw.rxj.iron_quarry.interfaces.IHandledSmithing;
import pw.rxj.iron_quarry.util.ZUtil;

public class HandledSmithingRecipe extends SmithingRecipe {
    public static final RecipeSerializer<HandledSmithingRecipe> SERIALIZER = new RecipeSerializer<>() {
        private ItemStack appendSmithingPreview(Ingredient base, Ingredient addition, ItemStack output) {
            if(ZUtil.getBlockOrItem(output) instanceof IHandledSmithing smithing) {
                ItemStack outputPreview = smithing.getSmithingOutputPreview(base, addition, output);
                if(outputPreview != null) return outputPreview;
            }

            return output;
        }

        @Override
        public HandledSmithingRecipe read(Identifier recipeId, JsonObject json) {
            SmithingRecipe recipe = RecipeSerializer.SMITHING.read(recipeId, json);

            Ingredient base = Ingredient.fromJson(JsonHelper.getObject(json, "base"));
            Ingredient addition = Ingredient.fromJson(JsonHelper.getObject(json, "addition"));
            ItemStack output = this.appendSmithingPreview(base, addition, recipe.getOutput());

            return new HandledSmithingRecipe(recipeId, base, addition, output);
        }

        @Override
        public HandledSmithingRecipe read(Identifier recipeId, PacketByteBuf buffer) {
            SmithingRecipe recipe = RecipeSerializer.SMITHING.read(recipeId, buffer);

            Ingredient base = Ingredient.fromPacket(buffer);
            Ingredient addition = Ingredient.fromPacket(buffer);
            ItemStack output = this.appendSmithingPreview(base, addition, recipe.getOutput());

            return new HandledSmithingRecipe(recipeId, base, addition, output);
        }

        @Override
        public void write(PacketByteBuf buffer, HandledSmithingRecipe recipe) {
            RecipeSerializer.SMITHING.write(buffer, recipe);
        }
    };

    public HandledSmithingRecipe(Identifier id, Ingredient base, Ingredient addition, ItemStack result) {
        super(id, base, addition, result);
    }

    @Override
    public ItemStack craft(Inventory inventory) {
        ItemStack output = getOutput().copy();

        if(ZUtil.getBlockOrItem(output) instanceof IHandledSmithing handledSmithing) {
            return handledSmithing.getSmithingOutput(this, inventory);
        }

        return super.craft(inventory);
    }
}
