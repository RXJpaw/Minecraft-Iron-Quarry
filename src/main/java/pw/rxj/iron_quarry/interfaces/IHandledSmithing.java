package pw.rxj.iron_quarry.interfaces;

import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.data.ZComponents;
import pw.rxj.iron_quarry.recipe.HandledSmithingRecipe;

import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingRecipeInput;

public interface IHandledSmithing {
    ItemStack getSmithingOutput(HandledSmithingRecipe handler, SmithingRecipeInput input);

    default ItemStack getSmithingOutputPreview(Optional<Ingredient> base, Optional<Ingredient> addition, @Nullable TagKey<Item> additionTag, ItemStack output) {
        return null;
    }

    default Boolean handleSmithingTakeOutput(Player player, Container inputInv, ResultContainer outputInv, ContainerLevelAccess context) {
        return false;
    }

    default void makeSmithingPreview(ItemStack stack, TagKey<Item> additionTag) {
        stack.set(ZComponents.SMITHING_OUTPUT_PREVIEW, additionTag.location());
    }

    default @Nullable Identifier getSmithingPreviewAdditionId(ItemStack stack) {
        return stack.get(ZComponents.SMITHING_OUTPUT_PREVIEW);
    }
    default @Nullable TagKey<Item> getSmithingPreviewAdditionTag(ItemStack stack) {
        Identifier addition = this.getSmithingPreviewAdditionId(stack);
        if(addition == null) return null;

        return TagKey.create(Registries.ITEM, addition);
    }

    default String getSmithingPreviewKey(ItemStack stack){
        Identifier addition = this.getSmithingPreviewAdditionId(stack);
        if(addition == null) return "";

        return "smithing_preview.iron_quarry." + BuiltInRegistries.ITEM.getKey(stack.getItem()) + "." + addition;
    }
    default boolean isSmithingPreview(ItemStack stack){
        Identifier addition = this.getSmithingPreviewAdditionId(stack);
        if(addition == null) return false;

        return !addition.getPath().isEmpty();
    }
}