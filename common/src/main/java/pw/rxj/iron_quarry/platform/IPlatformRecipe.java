package pw.rxj.iron_quarry.platform;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

public interface IPlatformRecipe {
    void synchronizeRecipes(RecipeSerializer<?> recipeSerializer, RecipeType<?> recipeType);
    @Nullable RecipeHolder<?> getSynchronizedRecipe(ResourceKey<Recipe<?>> recipeKey);
}
