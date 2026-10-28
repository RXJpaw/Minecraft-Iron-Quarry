package pw.rxj.iron_quarry.recipe;

import pw.rxj.iron_quarry.item.AugmentItem;
import pw.rxj.iron_quarry.item.DamageableSmithingTemplateItem;
import pw.rxj.iron_quarry.item.ZItemTags;

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
    public ItemStack getResultPreview() {
        ItemStack preview = this.getResultStack();

        if(preview.getItem() instanceof AugmentItem augmentItem) {
            augmentItem.makeSmithingPreview(preview, ZItemTags.AUGMENT_CAPACITY_ENHANCERS);
        }

        return preview;
    }
}
