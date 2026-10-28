package pw.rxj.iron_quarry.recipe;

import pw.rxj.iron_quarry.item.BlueprintItem;

import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipeInput;

public class SmithingBlueprintSealRecipe extends HandledSmithingRecipe {
    public static final RecipeSerializer<SmithingBlueprintSealRecipe> SERIALIZER = HandledSmithingRecipe.createSerializer(SmithingBlueprintSealRecipe::new);

    private SmithingBlueprintSealRecipe(Optional<Ingredient> template, Ingredient base, Optional<Ingredient> addition, ItemStackTemplate result) {
        super(template, base, addition, result);
    }

    @Override
    protected RecipeSerializer<SmithingBlueprintSealRecipe> getHandledSerializer() {
        return SERIALIZER;
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput input) {
        ItemStack output = this.getResultStack();
        if(!(output.getItem() instanceof BlueprintItem blueprintItem)) return ItemStack.EMPTY;

        ItemStack base = input.getItem(1).copy();

        if(blueprintItem.isSealed(base)) return ItemStack.EMPTY;
        if(blueprintItem.getFirstPos(base).isEmpty()) return ItemStack.EMPTY;
        if(blueprintItem.getSecondPos(base).isEmpty()) return ItemStack.EMPTY;

        blueprintItem.setSealed(base, true);

        return base.transmuteCopy(output.getItem(), output.getCount());
    }

    @Override
    public List<ItemStack> getResultPreviews() {
        ItemStack preview = this.getResultStack();

        if(preview.getItem() instanceof BlueprintItem blueprintItem) {
            blueprintItem.setSealed(preview, true);
        }

        return List.of(preview);
    }
}
