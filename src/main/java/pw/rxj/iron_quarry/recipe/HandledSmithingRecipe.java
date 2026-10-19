package pw.rxj.iron_quarry.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmithingTransformRecipe;
import net.minecraft.registry.RegistryWrapper;
import pw.rxj.iron_quarry.interfaces.IHandledSmithing;
import pw.rxj.iron_quarry.util.ZUtil;

public class HandledSmithingRecipe extends SmithingTransformRecipe {
    private final Ingredient template;
    private final Ingredient base;
    private final Ingredient addition;
    private final ItemStack result;

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    public static RecipeSerializer<?> SERIALIZER = new Serializer();

    public static class Serializer implements RecipeSerializer<HandledSmithingRecipe> {
        private static final MapCodec<HandledSmithingRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) ->
                instance.group(Ingredient.ALLOW_EMPTY_CODEC.fieldOf("template").forGetter((recipe) -> recipe.template),
                               Ingredient.ALLOW_EMPTY_CODEC.fieldOf("base").forGetter((recipe) -> recipe.base),
                               Ingredient.ALLOW_EMPTY_CODEC.fieldOf("addition").forGetter((recipe) -> recipe.addition),
                               ItemStack.VALIDATED_CODEC.fieldOf("result").forGetter((recipe) -> recipe.result))
                .apply(instance, (template, base, addition, result) ->
                        new HandledSmithingRecipe(template, base, addition, appendSmithingPreview(base, addition, result))
                )
        );
        public static final PacketCodec<RegistryByteBuf, HandledSmithingRecipe> PACKET_CODEC = PacketCodec.ofStatic(Serializer::write, Serializer::read);


        private static ItemStack appendSmithingPreview(Ingredient base, Ingredient addition, ItemStack result) {
            if(ZUtil.getBlockOrItem(result) instanceof IHandledSmithing smithing) {
                ItemStack resultPreview = smithing.getSmithingOutputPreview(base, addition, result);
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
            Ingredient template = Ingredient.PACKET_CODEC.decode(buf);
            Ingredient base = Ingredient.PACKET_CODEC.decode(buf);
            Ingredient addition = Ingredient.PACKET_CODEC.decode(buf);
            ItemStack result = ItemStack.PACKET_CODEC.decode(buf);
            return new HandledSmithingRecipe(template, base, addition, result);
        }

        private static void write(RegistryByteBuf buf, HandledSmithingRecipe recipe) {
            Ingredient.PACKET_CODEC.encode(buf, recipe.template);
            Ingredient.PACKET_CODEC.encode(buf, recipe.base);
            Ingredient.PACKET_CODEC.encode(buf, recipe.addition);
            ItemStack.PACKET_CODEC.encode(buf, recipe.result);
        }
    };

    public HandledSmithingRecipe(Ingredient template, Ingredient base, Ingredient addition, ItemStack result) {
        super(template, base, addition, result);

        this.template = template;
        this.base = base;
        this.addition = addition;
        this.result = result;
    }

    @Override
    public ItemStack craft(Inventory inventory, RegistryWrapper.WrapperLookup registryLookup) {
        ItemStack result = this.getResult(registryLookup).copy();

        if(ZUtil.getBlockOrItem(result) instanceof IHandledSmithing handledSmithing) {
            return handledSmithing.getSmithingOutput(this, inventory, registryLookup);
        }

        return super.craft(inventory, registryLookup);
    }
}
