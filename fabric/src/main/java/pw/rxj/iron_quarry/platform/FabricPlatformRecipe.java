package pw.rxj.iron_quarry.platform;

import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

public class FabricPlatformRecipe implements IPlatformRecipe {
    @Override
    public void synchronizeRecipes(RecipeSerializer<?> recipeSerializer, RecipeType<?> recipeType) {
        RecipeSynchronization.synchronizeRecipeSerializer(recipeSerializer);
    }

    @Override
    public @Nullable RecipeHolder<?> getSynchronizedRecipe(ResourceKey<Recipe<?>> recipeKey) {
        ClientLevel world = Minecraft.getInstance().level;
        if(world == null) return null;

        return world.recipeAccess().getSynchronizedRecipes().get(recipeKey);
    }
}
