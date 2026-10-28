package pw.rxj.iron_quarry.compat.jei;

import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.recipe.category.extensions.vanilla.smithing.ISmithingCategoryExtension;
import pw.rxj.iron_quarry.recipe.HandledSmithingRecipe;

public class HandledSmithingCategoryExtension implements ISmithingCategoryExtension<HandledSmithingRecipe> {
    @Override
    public <T extends IIngredientAcceptor<T>> void setTemplate(HandledSmithingRecipe recipe, T ingredientAcceptor) {
        recipe.templateIngredient().ifPresent(ingredientAcceptor::add);
    }

    @Override
    public <T extends IIngredientAcceptor<T>> void setBase(HandledSmithingRecipe recipe, T ingredientAcceptor) {
        ingredientAcceptor.add(recipe.baseIngredient());
    }

    @Override
    public <T extends IIngredientAcceptor<T>> void setAddition(HandledSmithingRecipe recipe, T ingredientAcceptor) {
        recipe.additionIngredient().ifPresent(ingredientAcceptor::add);
    }

    @Override
    public <T extends IIngredientAcceptor<T>> void setOutput(HandledSmithingRecipe recipe, T ingredientAcceptor) {
        ingredientAcceptor.add(recipe.getResultPreview());
    }
}
