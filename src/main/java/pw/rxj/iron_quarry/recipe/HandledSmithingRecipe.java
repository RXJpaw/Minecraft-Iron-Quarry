package pw.rxj.iron_quarry.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.interfaces.IHandledSmithing;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
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

public class HandledSmithingRecipe extends SmithingTransformRecipe {
    private final Optional<Ingredient> template;
    private final Optional<Ingredient> base;
    private final Optional<Ingredient> addition;
    private final @Nullable TagKey<Item> additionTag;
    private final ItemStackTemplate result;

    @Override
    @SuppressWarnings("unchecked")
    public RecipeSerializer<SmithingTransformRecipe> getSerializer() {
        return (RecipeSerializer<SmithingTransformRecipe>) (RecipeSerializer<?>) SERIALIZER;
    }

    public static RecipeSerializer<HandledSmithingRecipe> SERIALIZER = new RecipeSerializer<>(Serializer.CODEC, Serializer.PACKET_CODEC);

    public static class Serializer {
        private static final MapCodec<HandledSmithingRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) ->
                instance.group(Ingredient.CODEC.optionalFieldOf("template").forGetter((recipe) -> recipe.template),
                               Ingredient.CODEC.fieldOf("base").xmap(Optional::of, Optional::orElseThrow).forGetter((recipe) -> recipe.base),
                               Ingredient.CODEC.optionalFieldOf("addition").forGetter((recipe) -> recipe.addition),
                               Codec.STRING.lenientOptionalFieldOf("addition").forGetter((recipe) -> Optional.empty()),
                               ItemStackTemplate.CODEC.fieldOf("result").forGetter((recipe) -> recipe.result))
                .apply(instance, (template, base, addition, rawAddition, result) ->
                        new HandledSmithingRecipe(template, base, addition, parseTag(rawAddition), result)
                )
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, HandledSmithingRecipe> PACKET_CODEC = StreamCodec.of(pw.rxj.iron_quarry.recipe.HandledSmithingRecipe.Serializer::write, pw.rxj.iron_quarry.recipe.HandledSmithingRecipe.Serializer::read);

        private static @Nullable TagKey<Item> parseTag(Optional<String> raw) {
            if(raw.isEmpty() || !raw.get().startsWith("#")) return null;

            Identifier id = Identifier.tryParse(raw.get().substring(1));
            if(id == null) return null;

            return TagKey.create(Registries.ITEM, id);
        }

        private static ItemStack appendSmithingPreview(Optional<Ingredient> base, Optional<Ingredient> addition, @Nullable TagKey<Item> additionTag, ItemStack result) {
            if(ZUtil.getBlockOrItem(result) instanceof IHandledSmithing smithing) {
                ItemStack resultPreview = smithing.getSmithingOutputPreview(base, addition, additionTag, result);
                if(resultPreview != null) return resultPreview;
            }

            return result;
        }

        private static HandledSmithingRecipe read(RegistryFriendlyByteBuf buf) {
            Optional<Ingredient> template = Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.decode(buf);
            Optional<Ingredient> base = Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.decode(buf);
            Optional<Ingredient> addition = Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.decode(buf);
            Identifier additionTag = buf.readNullable(FriendlyByteBuf::readIdentifier);
            ItemStackTemplate result = ItemStackTemplate.STREAM_CODEC.decode(buf);
            return new HandledSmithingRecipe(template, base, addition, additionTag == null ? null : TagKey.create(Registries.ITEM, additionTag), result);
        }

        private static void write(RegistryFriendlyByteBuf buf, HandledSmithingRecipe recipe) {
            Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.encode(buf, recipe.template);
            Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.encode(buf, recipe.base);
            Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.encode(buf, recipe.addition);
            buf.writeNullable(recipe.additionTag == null ? null : recipe.additionTag.location(), FriendlyByteBuf::writeIdentifier);
            ItemStackTemplate.STREAM_CODEC.encode(buf, recipe.result);
        }
    }

    public HandledSmithingRecipe(Optional<Ingredient> template, Optional<Ingredient> base, Optional<Ingredient> addition, @Nullable TagKey<Item> additionTag, ItemStackTemplate result) {
        super(new Recipe.CommonInfo(true), template, base.orElseThrow(), addition, result);

        this.template = template;
        this.base = base;
        this.addition = addition;
        this.additionTag = additionTag;
        this.result = result;
    }

    public ItemStack getResultStack() {
        return this.result.create();
    }

    @Override
    public List<RecipeDisplay> display() {
        ItemStack preview = Serializer.appendSmithingPreview(this.base, this.addition, this.additionTag, this.getResultStack());

        return List.of(new SmithingRecipeDisplay(
                Ingredient.optionalIngredientToDisplay(this.template),
                this.base.orElseThrow().display(),
                Ingredient.optionalIngredientToDisplay(this.addition),
                new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(preview)),
                new SlotDisplay.ItemSlotDisplay(Items.SMITHING_TABLE)
        ));
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput input) {
        ItemStack result = this.getResultStack();

        if(ZUtil.getBlockOrItem(result) instanceof IHandledSmithing handledSmithing) {
            return handledSmithing.getSmithingOutput(this, input);
        }

        return super.assemble(input);
    }
}
