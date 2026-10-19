package pw.rxj.iron_quarry.interfaces;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.recipe.input.SmithingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.data.ZComponents;
import pw.rxj.iron_quarry.recipe.HandledSmithingRecipe;

public interface IHandledSmithing {
    ItemStack getSmithingOutput(HandledSmithingRecipe handler, SmithingRecipeInput input, RegistryWrapper.WrapperLookup registryLookup);

    default ItemStack getSmithingOutputPreview(Ingredient base, Ingredient addition, ItemStack output) {
        return null;
    }

    default Boolean handleSmithingTakeOutput(PlayerEntity player, Inventory inputInv, CraftingResultInventory outputInv, ScreenHandlerContext context) {
        return false;
    }

    default void makeSmithingPreview(ItemStack stack, TagKey<Item> additionTag) {
        stack.set(ZComponents.SMITHING_OUTPUT_PREVIEW, additionTag.id());
    }

    default @Nullable Identifier getSmithingPreviewAdditionId(ItemStack stack) {
        return stack.get(ZComponents.SMITHING_OUTPUT_PREVIEW);
    }
    default @Nullable TagKey<Item> getSmithingPreviewAdditionTag(ItemStack stack) {
        Identifier addition = this.getSmithingPreviewAdditionId(stack);
        if(addition == null) return null;

        return TagKey.of(RegistryKeys.ITEM, addition);
    }
    default @Nullable Ingredient getSmithingPreviewAdditionIngredient(ItemStack stack) {
        TagKey<Item> addition = this.getSmithingPreviewAdditionTag(stack);
        if(addition == null) return null;

        return Ingredient.fromTag(addition);
    }

    default String getSmithingPreviewKey(ItemStack stack){
        Identifier addition = this.getSmithingPreviewAdditionId(stack);
        if(addition == null) return "";

        return "smithing_preview.iron_quarry." + Registries.ITEM.getId(stack.getItem()) + "." + addition;
    }
    default boolean isSmithingPreview(ItemStack stack){
        Identifier addition = this.getSmithingPreviewAdditionId(stack);
        if(addition == null) return false;

        return !addition.getPath().isEmpty();
    }
}