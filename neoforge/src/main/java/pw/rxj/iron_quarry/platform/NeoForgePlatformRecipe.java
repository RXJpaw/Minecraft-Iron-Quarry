package pw.rxj.iron_quarry.platform;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;

public class NeoForgePlatformRecipe implements IPlatformRecipe {
    private static final Set<RecipeType<?>> SYNCHRONIZED_RECIPE_TYPES = new LinkedHashSet<>();
    private static RecipeMap receivedRecipes = RecipeMap.EMPTY;

    @Override
    public void synchronizeRecipes(RecipeSerializer<?> recipeSerializer, RecipeType<?> recipeType) {
        SYNCHRONIZED_RECIPE_TYPES.add(recipeType);
    }

    @Override
    public @Nullable RecipeHolder<?> getSynchronizedRecipe(ResourceKey<Recipe<?>> recipeKey) {
        return receivedRecipes.byKey(recipeKey);
    }

    public static void sendRecipes(OnDatapackSyncEvent event) {
        event.sendRecipes(SYNCHRONIZED_RECIPE_TYPES);
    }
    public static void receiveRecipes(RecipesReceivedEvent event) {
        receivedRecipes = event.getRecipeMap();
    }
}
