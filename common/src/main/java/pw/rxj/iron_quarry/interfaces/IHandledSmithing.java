package pw.rxj.iron_quarry.interfaces;

import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.data.ZComponents;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface IHandledSmithing {
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

    default boolean isSmithingPreview(ItemStack stack){
        Identifier addition = this.getSmithingPreviewAdditionId(stack);
        if(addition == null) return false;

        return !addition.getPath().isEmpty();
    }
}