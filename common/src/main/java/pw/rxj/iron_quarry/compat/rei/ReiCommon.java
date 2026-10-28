package pw.rxj.iron_quarry.compat.rei;

import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.plugin.common.SmithingDisplay;
import me.shedaniel.rei.plugin.common.displays.DefaultSmithingDisplay;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import pw.rxj.iron_quarry.recipe.SmithingAugmentCapacityRecipe;

import java.util.List;
import java.util.Optional;

public class ReiCommon implements REICommonPlugin {
    @Override
    public void registerDisplays(ServerDisplayRegistry registry) {
        registry.beginRecipeFiller(SmithingAugmentCapacityRecipe.class)
                .filterType(RecipeType.SMITHING)
                .fillMultiple(ReiCommon::createCapacityDisplays);
    }

    private static List<DefaultSmithingDisplay> createCapacityDisplays(RecipeHolder<SmithingAugmentCapacityRecipe> holder) {
        SmithingAugmentCapacityRecipe recipe = holder.value();
        if(recipe.additionIngredient().isEmpty()) return List.of();

        EntryIngredient template = recipe.templateIngredient().map(EntryIngredients::ofIngredient).orElse(EntryIngredient.empty());
        EntryIngredient base = EntryIngredients.ofIngredient(recipe.baseIngredient());

        return recipe.additionIngredient().get().items().map((item) -> {
            ItemStack addition = item.value().getDefaultInstance();

            return new DefaultSmithingDisplay(
                    List.of(template, base, EntryIngredients.of(addition)),
                    List.of(EntryIngredients.of(recipe.getResultPreview(addition))),
                    Optional.of(SmithingDisplay.SmithingRecipeType.TRANSFORM),
                    Optional.of(holder.id().identifier())
            );
        }).toList();
    }
}
