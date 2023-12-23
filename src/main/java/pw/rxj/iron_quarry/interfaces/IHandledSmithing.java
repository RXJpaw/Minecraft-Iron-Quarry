package pw.rxj.iron_quarry.interfaces;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.recipe.HandledSmithingRecipe;

public interface IHandledSmithing {
    ItemStack getSmithingOutput(HandledSmithingRecipe handler, Inventory inventory);

    default ItemStack getSmithingOutputPreview(Ingredient base, Ingredient addition, ItemStack output) {
        return null;
    }

    default Boolean handleSmithingTakeOutput(PlayerEntity player, Inventory inputInv, CraftingResultInventory outputInv, ScreenHandlerContext context) {
        return false;
    }

    default void makeSmithingPreview(ItemStack stack, TagKey<Item> additionTag) {
        stack.getOrCreateNbt().putString("smithing_output_preview", additionTag.id().toString());
    }

    default @Nullable Identifier getSmithingPreviewAdditionId(ItemStack stack) {
        NbtCompound nbtCompound = stack.getNbt();
        if(nbtCompound == null) return null;

        return Identifier.tryParse(nbtCompound.getString("smithing_output_preview"));
    }
    default @Nullable TagKey<Item> getSmithingPreviewAdditionTag(ItemStack stack) {
        Identifier addition = this.getSmithingPreviewAdditionId(stack);
        if(addition == null) return null;

        return TagKey.of(Registry.ITEM_KEY, addition);
    }
    default @Nullable Ingredient getSmithingPreviewAdditionIngredient(ItemStack stack) {
        TagKey<Item> addition = this.getSmithingPreviewAdditionTag(stack);
        if(addition == null) return null;

        return Ingredient.fromTag(addition);
    }

    default String getSmithingPreviewKey(ItemStack stack){
        Identifier addition = this.getSmithingPreviewAdditionId(stack);
        if(addition == null) return "";

        return "smithing_preview.iron_quarry." + Registry.ITEM.getId(stack.getItem()) + "." + addition;
    }
    default boolean isSmithingPreview(ItemStack stack){
        Identifier addition = getSmithingPreviewAdditionId(stack);
        if(addition == null) return false;

        return !addition.getPath().isEmpty();
    }
}