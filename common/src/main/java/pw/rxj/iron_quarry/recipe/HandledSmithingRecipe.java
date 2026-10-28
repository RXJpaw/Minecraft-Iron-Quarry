package pw.rxj.iron_quarry.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SmithingRecipeDisplay;

public abstract class HandledSmithingRecipe extends SmithingTransformRecipe {
    private final ItemStackTemplate result;

    @Override
    @SuppressWarnings("unchecked")
    public RecipeSerializer<SmithingTransformRecipe> getSerializer() {
        return (RecipeSerializer<SmithingTransformRecipe>) (RecipeSerializer<?>) this.getHandledSerializer();
    }

    protected abstract RecipeSerializer<? extends HandledSmithingRecipe> getHandledSerializer();

    public interface Factory<T extends HandledSmithingRecipe> {
        T create(Optional<Ingredient> template, Ingredient base, Optional<Ingredient> addition, ItemStackTemplate result);
    }

    protected static <T extends HandledSmithingRecipe> RecipeSerializer<T> createSerializer(Factory<T> factory) {
        MapCodec<T> codec = RecordCodecBuilder.mapCodec((instance) ->
                instance.group(Ingredient.CODEC.optionalFieldOf("template").forGetter((T recipe) -> recipe.templateIngredient()),
                               Ingredient.CODEC.fieldOf("base").forGetter((T recipe) -> recipe.baseIngredient()),
                               Ingredient.CODEC.optionalFieldOf("addition").forGetter((T recipe) -> recipe.additionIngredient()),
                               ItemStackTemplate.CODEC.fieldOf("result").forGetter((T recipe) -> recipe.getResultTemplate()))
                .apply(instance, factory::create)
        );
        StreamCodec<RegistryFriendlyByteBuf, T> streamCodec = StreamCodec.of(HandledSmithingRecipe::write, (buf) -> read(buf, factory));

        return new RecipeSerializer<>(codec, streamCodec);
    }

    private static <T extends HandledSmithingRecipe> T read(RegistryFriendlyByteBuf buf, Factory<T> factory) {
        Optional<Ingredient> template = Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.decode(buf);
        Ingredient base = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
        Optional<Ingredient> addition = Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.decode(buf);
        ItemStackTemplate result = ItemStackTemplate.STREAM_CODEC.decode(buf);

        return factory.create(template, base, addition, result);
    }

    private static void write(RegistryFriendlyByteBuf buf, HandledSmithingRecipe recipe) {
        Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.encode(buf, recipe.templateIngredient());
        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.baseIngredient());
        Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.encode(buf, recipe.additionIngredient());
        ItemStackTemplate.STREAM_CODEC.encode(buf, recipe.getResultTemplate());
    }

    protected HandledSmithingRecipe(Optional<Ingredient> template, Ingredient base, Optional<Ingredient> addition, ItemStackTemplate result) {
        super(new Recipe.CommonInfo(true), template, base, addition, result);

        this.result = result;
    }

    public ItemStackTemplate getResultTemplate() {
        return this.result;
    }
    public ItemStack getResultStack() {
        return this.result.create();
    }

    public abstract ItemStack getResultPreview();

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new SmithingRecipeDisplay(
                Ingredient.optionalIngredientToDisplay(this.templateIngredient()),
                this.baseIngredient().display(),
                Ingredient.optionalIngredientToDisplay(this.additionIngredient()),
                new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(this.getResultPreview())),
                new SlotDisplay.ItemSlotDisplay(Items.SMITHING_TABLE)
        ));
    }

    @Override
    public abstract ItemStack assemble(SmithingRecipeInput input);
}
