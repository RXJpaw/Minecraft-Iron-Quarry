package pw.rxj.iron_quarry.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmithingTransformRecipe;
import net.minecraft.registry.DynamicRegistryManager;
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

    public static final RecipeSerializer<HandledSmithingRecipe> SERIALIZER = new RecipeSerializer<>() {
        private static final Codec<HandledSmithingRecipe> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(Ingredient.ALLOW_EMPTY_CODEC.fieldOf("template").forGetter((recipe) -> {
                return recipe.template;
            }), Ingredient.ALLOW_EMPTY_CODEC.fieldOf("base").forGetter((recipe) -> {
                return recipe.base;
            }), Ingredient.ALLOW_EMPTY_CODEC.fieldOf("addition").forGetter((recipe) -> {
                return recipe.addition;
            }), ItemStack.RECIPE_RESULT_CODEC.fieldOf("result").forGetter((recipe) -> {
                return recipe.result;
            })).apply(instance, (template, base, addition, result) -> {
                return new HandledSmithingRecipe(template, base, addition, appendSmithingPreview(base, addition, result));
            });
        });

        private static ItemStack appendSmithingPreview(Ingredient base, Ingredient addition, ItemStack result) {
            if(ZUtil.getBlockOrItem(result) instanceof IHandledSmithing smithing) {
                ItemStack resultPreview = smithing.getSmithingOutputPreview(base, addition, result);
                if(resultPreview != null) return resultPreview;
            }

            return result;
        }

        @Override
        public Codec<HandledSmithingRecipe> codec() {
            return CODEC;
        }

        @Override
        public HandledSmithingRecipe read(PacketByteBuf buffer) {
            Ingredient template = Ingredient.fromPacket(buffer);
            Ingredient base = Ingredient.fromPacket(buffer);
            Ingredient addition = Ingredient.fromPacket(buffer);
            ItemStack result = buffer.readItemStack();

            return new HandledSmithingRecipe(template, base, addition, result);
        }

        @Override
        public void write(PacketByteBuf buffer, HandledSmithingRecipe recipe) {
            RecipeSerializer.SMITHING_TRANSFORM.write(buffer, recipe);
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
    public ItemStack craft(Inventory inventory, DynamicRegistryManager dynamicRegistryManager) {
        ItemStack result = this.getResult(dynamicRegistryManager).copy();

        if(ZUtil.getBlockOrItem(result) instanceof IHandledSmithing handledSmithing) {
            return handledSmithing.getSmithingOutput(this, inventory, dynamicRegistryManager);
        }

        return super.craft(inventory, dynamicRegistryManager);
    }
}
