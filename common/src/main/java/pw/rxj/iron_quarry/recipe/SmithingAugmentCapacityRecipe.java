package pw.rxj.iron_quarry.recipe;

import pw.rxj.iron_quarry.item.AugmentItem;
import pw.rxj.iron_quarry.item.DamageableSmithingTemplateItem;

import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipeInput;

public class SmithingAugmentCapacityRecipe extends HandledSmithingRecipe {
    public static final RecipeSerializer<SmithingAugmentCapacityRecipe> SERIALIZER = HandledSmithingRecipe.createSerializer(SmithingAugmentCapacityRecipe::new);

    private SmithingAugmentCapacityRecipe(Optional<Ingredient> template, Ingredient base, Optional<Ingredient> addition, ItemStackTemplate result) {
        super(template, base, addition, result);
    }

    @Override
    protected RecipeSerializer<SmithingAugmentCapacityRecipe> getHandledSerializer() {
        return SERIALIZER;
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput input) {
        if(DamageableSmithingTemplateItem.isNotOf(input.getItem(0))) return ItemStack.EMPTY;

        ItemStack base = input.getItem(1).copy();
        if(!(base.getItem() instanceof AugmentItem augmentItem)) return ItemStack.EMPTY;

        boolean result = augmentItem.putUpgrade(base, input.getItem(2));

        return result ? base : ItemStack.EMPTY;
    }

    @Override
    public List<ItemStack> getResultPreviews() {
        List<ItemStack> previews = this.additionIngredient()
                .map((addition) -> addition.items().map((item) -> this.getResultPreview(item.value().getDefaultInstance())).toList())
                .orElse(List.of());

        return previews.isEmpty() ? List.of(this.getResultStack()) : previews;
    }
    public ItemStack getResultPreview(ItemStack addition) {
        ItemStack preview = this.getResultStack();

        if(preview.getItem() instanceof AugmentItem augmentItem) {
            augmentItem.putUpgrade(preview, addition);
        }

        return preview;
    }
}
