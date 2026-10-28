package pw.rxj.iron_quarry.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

public class HandledCraftingRecipe extends ShapedRecipe {
    private final ShapedRecipePattern pattern;
    private final char source;
    private final int sourceIndex;
    private final ItemStackTemplate result;

    @Override
    @SuppressWarnings("unchecked")
    public RecipeSerializer<ShapedRecipe> getSerializer() {
        return (RecipeSerializer<ShapedRecipe>) (RecipeSerializer<?>) SERIALIZER;
    }

    public static RecipeSerializer<HandledCraftingRecipe> SERIALIZER = new RecipeSerializer<>(Serializer.CODEC, Serializer.PACKET_CODEC);

    public static class Serializer {
        private static final Codec<Character> SOURCE_CODEC = Codec.STRING.comapFlatMap(Serializer::parseSource, String::valueOf);
        private static final MapCodec<HandledCraftingRecipe> CODEC = RecordCodecBuilder.<HandledCraftingRecipe>mapCodec((instance) ->
                instance.group(Recipe.CommonInfo.MAP_CODEC.forGetter((recipe) -> recipe.commonInfo),
                               CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter((recipe) -> recipe.bookInfo),
                               ShapedRecipePattern.MAP_CODEC.forGetter((recipe) -> recipe.pattern),
                               SOURCE_CODEC.fieldOf("source").forGetter((recipe) -> recipe.source),
                               ItemStackTemplate.CODEC.fieldOf("result").forGetter((recipe) -> recipe.result))
                .apply(instance, HandledCraftingRecipe::of)
        ).validate(Serializer::validate);
        public static final StreamCodec<RegistryFriendlyByteBuf, HandledCraftingRecipe> PACKET_CODEC = StreamCodec.composite(
                Recipe.CommonInfo.STREAM_CODEC, (recipe) -> recipe.commonInfo,
                CraftingRecipe.CraftingBookInfo.STREAM_CODEC, (recipe) -> recipe.bookInfo,
                ShapedRecipePattern.STREAM_CODEC, (recipe) -> recipe.pattern,
                ByteBufCodecs.VAR_INT, (recipe) -> recipe.sourceIndex,
                ItemStackTemplate.STREAM_CODEC, (recipe) -> recipe.result,
                HandledCraftingRecipe::fromNetwork
        );

        private static DataResult<Character> parseSource(String source) {
            if(source.length() != 1 || source.charAt(0) == ShapedRecipePattern.EMPTY_SLOT) {
                return DataResult.error(() -> "Invalid source symbol: '" + source + "', must be a single non-space character");
            }

            return DataResult.success(source.charAt(0));
        }

        private static DataResult<HandledCraftingRecipe> validate(HandledCraftingRecipe recipe) {
            if(recipe.sourceIndex < 0) {
                return DataResult.error(() -> "Source symbol '" + recipe.source + "' must appear exactly once in pattern");
            }

            return DataResult.success(recipe);
        }
    }

    private HandledCraftingRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, ShapedRecipePattern pattern, char source, int sourceIndex, ItemStackTemplate result) {
        super(commonInfo, bookInfo, pattern, result);

        this.pattern = pattern;
        this.source = source;
        this.sourceIndex = sourceIndex;
        this.result = result;
    }
    public static HandledCraftingRecipe of(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, ShapedRecipePattern pattern, char source, ItemStackTemplate result) {
        return new HandledCraftingRecipe(commonInfo, bookInfo, pattern, source, findSourceIndex(pattern, source), result);
    }
    private static HandledCraftingRecipe fromNetwork(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, ShapedRecipePattern pattern, int sourceIndex, ItemStackTemplate result) {
        return new HandledCraftingRecipe(commonInfo, bookInfo, pattern, ShapedRecipePattern.EMPTY_SLOT, sourceIndex, result);
    }

    public int getSourceIndex() {
        return this.sourceIndex;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack output = super.assemble(input);

        ItemStack sourceStack = this.getSourceStack(input);
        if(sourceStack.isEmpty()) return output;

        return sourceStack.transmuteCopy(output.getItem(), output.getCount());
    }

    private ItemStack getSourceStack(@Nullable CraftingInput input) {
        if(input == null || this.sourceIndex < 0) return ItemStack.EMPTY;
        if(input.width() != this.pattern.width() || input.height() != this.pattern.height()) return ItemStack.EMPTY;

        int x = this.sourceIndex % this.pattern.width();
        int y = this.sourceIndex / this.pattern.width();
        if(!this.pattern.matches(input, false)) x = this.pattern.width() - 1 - x;

        return input.getItem(x, y);
    }

    private static int findSourceIndex(ShapedRecipePattern pattern, char source) {
        if(pattern.data.isEmpty()) return -1;

        List<String> rows = pattern.data.get().pattern();
        int firstRow = -1;
        int firstColumn = Integer.MAX_VALUE;
        int sourceRow = -1;
        int sourceColumn = -1;

        for (int row = 0; row < rows.size(); row++) {
            String line = rows.get(row);

            for (int column = 0; column < line.length(); column++) {
                char symbol = line.charAt(column);
                if(symbol == ShapedRecipePattern.EMPTY_SLOT) continue;

                if(firstRow < 0) firstRow = row;
                firstColumn = Math.min(firstColumn, column);

                if(symbol != source) continue;
                if(sourceRow >= 0) return -1;

                sourceRow = row;
                sourceColumn = column;
            }
        }

        if(sourceRow < 0) return -1;

        return (sourceRow - firstRow) * pattern.width() + (sourceColumn - firstColumn);
    }
}
