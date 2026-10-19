package pw.rxj.iron_quarry.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmithingTransformRecipe;
import net.minecraft.recipe.TransmuteRecipeResult;
import net.minecraft.recipe.input.SmithingRecipeInput;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.interfaces.IHandledSmithing;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.Optional;

public class HandledSmithingRecipe extends SmithingTransformRecipe {
    private final Optional<Ingredient> template;
    private final Optional<Ingredient> base;
    private final Optional<Ingredient> addition;
    private final ItemStack result;

    @Override
    @SuppressWarnings("unchecked")
    public RecipeSerializer<SmithingTransformRecipe> getSerializer() {
        return (RecipeSerializer<SmithingTransformRecipe>) (RecipeSerializer<?>) SERIALIZER;
    }

    public static RecipeSerializer<HandledSmithingRecipe> SERIALIZER = new Serializer();

    public static class Serializer implements RecipeSerializer<HandledSmithingRecipe> {
        private static final MapCodec<HandledSmithingRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) ->
                instance.group(Ingredient.CODEC.optionalFieldOf("template").forGetter((recipe) -> recipe.template),
                               Ingredient.CODEC.fieldOf("base").xmap(Optional::of, Optional::orElseThrow).forGetter((recipe) -> recipe.base),
                               Ingredient.CODEC.optionalFieldOf("addition").forGetter((recipe) -> recipe.addition),
                               Codec.STRING.lenientOptionalFieldOf("addition").forGetter((recipe) -> Optional.empty()),
                               ItemStack.VALIDATED_CODEC.fieldOf("result").forGetter((recipe) -> recipe.result))
                .apply(instance, (template, base, addition, rawAddition, result) ->
                        new HandledSmithingRecipe(template, base, addition, appendSmithingPreview(base, addition, parseTag(rawAddition), result))
                )
        );
        public static final PacketCodec<RegistryByteBuf, HandledSmithingRecipe> PACKET_CODEC = PacketCodec.ofStatic(Serializer::write, Serializer::read);

        private static @Nullable TagKey<Item> parseTag(Optional<String> raw) {
            if(raw.isEmpty() || !raw.get().startsWith("#")) return null;

            Identifier id = Identifier.tryParse(raw.get().substring(1));
            if(id == null) return null;

            return TagKey.of(RegistryKeys.ITEM, id);
        }

        private static ItemStack appendSmithingPreview(Optional<Ingredient> base, Optional<Ingredient> addition, @Nullable TagKey<Item> additionTag, ItemStack result) {
            if(ZUtil.getBlockOrItem(result) instanceof IHandledSmithing smithing) {
                ItemStack resultPreview = smithing.getSmithingOutputPreview(base, addition, additionTag, result);
                if(resultPreview != null) return resultPreview;
            }

            return result;
        }

        @Override
        public MapCodec<HandledSmithingRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, HandledSmithingRecipe> packetCodec() {
            return PACKET_CODEC;
        }

        private static HandledSmithingRecipe read(RegistryByteBuf buf) {
            Optional<Ingredient> template = Ingredient.OPTIONAL_PACKET_CODEC.decode(buf);
            Optional<Ingredient> base = Ingredient.OPTIONAL_PACKET_CODEC.decode(buf);
            Optional<Ingredient> addition = Ingredient.OPTIONAL_PACKET_CODEC.decode(buf);
            ItemStack result = ItemStack.PACKET_CODEC.decode(buf);
            return new HandledSmithingRecipe(template, base, addition, result);
        }

        private static void write(RegistryByteBuf buf, HandledSmithingRecipe recipe) {
            Ingredient.OPTIONAL_PACKET_CODEC.encode(buf, recipe.template);
            Ingredient.OPTIONAL_PACKET_CODEC.encode(buf, recipe.base);
            Ingredient.OPTIONAL_PACKET_CODEC.encode(buf, recipe.addition);
            ItemStack.PACKET_CODEC.encode(buf, recipe.result);
        }
    }

    public HandledSmithingRecipe(Optional<Ingredient> template, Optional<Ingredient> base, Optional<Ingredient> addition, ItemStack result) {
        super(template, base.orElseThrow(), addition, new TransmuteRecipeResult(result.getRegistryEntry(), result.getCount(), result.getComponentChanges()));

        this.template = template;
        this.base = base;
        this.addition = addition;
        this.result = result;
    }

    public ItemStack getResultStack() {
        return this.result.copy();
    }

    @Override
    public ItemStack craft(SmithingRecipeInput input, RegistryWrapper.WrapperLookup registryLookup) {
        ItemStack result = this.getResultStack();

        if(ZUtil.getBlockOrItem(result) instanceof IHandledSmithing handledSmithing) {
            return handledSmithing.getSmithingOutput(this, input, registryLookup);
        }

        return super.craft(input, registryLookup);
    }
}
