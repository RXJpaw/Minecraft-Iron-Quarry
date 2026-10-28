package pw.rxj.iron_quarry.recipe;

import pw.rxj.iron_quarry.item.AugmentItem;
import pw.rxj.iron_quarry.item.DamageableSmithingTemplateItem;
import pw.rxj.iron_quarry.records.AugmentStack;

import java.util.Optional;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipeInput;

public class SmithingAugmentRecipe extends HandledSmithingRecipe {
    public static final RecipeSerializer<SmithingAugmentRecipe> SERIALIZER = HandledSmithingRecipe.createSerializer(SmithingAugmentRecipe::new);

    private SmithingAugmentRecipe(Optional<Ingredient> template, Ingredient base, Optional<Ingredient> addition, ItemStackTemplate result) {
        super(template, base, addition, result);
    }

    @Override
    protected RecipeSerializer<SmithingAugmentRecipe> getHandledSerializer() {
        return SERIALIZER;
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput input) {
        ItemStack template = input.getItem(0).copy();
        if(DamageableSmithingTemplateItem.isNotOf(template)) return ItemStack.EMPTY;

        ItemStack base = input.getItem(1).copy();
        if(!(base.getItem() instanceof AugmentItem augmentItem)) return ItemStack.EMPTY;

        ItemStack addition = input.getItem(2).copy();
        Container outcome = augmentItem.getSmithingOutcome(template, base, addition);

        return outcome.getItem(3);
    }

    @Override
    public ItemStack getResultPreview() {
        ItemStack preview = this.getResultStack();
        if(!(preview.getItem() instanceof AugmentItem augmentItem)) return preview;

        Optional<ItemStack> addition = this.additionIngredient()
                .flatMap((ingredient) -> ingredient.items().findFirst())
                .map((item) -> item.value().getDefaultInstance());
        if(addition.isEmpty()) return preview;

        AugmentStack augmentStack = AugmentStack.from(addition.get());
        if(augmentStack == null) return preview;

        augmentItem.setType(preview, augmentStack.type());
        augmentItem.setAmount(preview, augmentStack.amount());

        return preview;
    }
}
