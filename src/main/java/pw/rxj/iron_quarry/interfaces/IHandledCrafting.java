package pw.rxj.iron_quarry.interfaces;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import pw.rxj.iron_quarry.util.ZUtil;

public interface IHandledCrafting {
    static ItemStack copyCenterSlotComponents(ItemStack output, CraftingInput input) {
        ItemStack stack = getCenterStack(input);
        if(ZUtil.getBlockOrItem(stack) instanceof IHandledCrafting) {
            return stack.transmuteCopy(output.getItem(), output.getCount());
        }

        return output;
    }

    ItemStack getCraftingOutput(ItemStack output, CraftingInput input);

    static ItemStack getCenterStack(CraftingInput input) {
        if(input == null || input.width() != 3 || input.height() != 3) return ItemStack.EMPTY;

        return input.getItem(1, 1);
    }

    default ItemStack getCraftingOutputPreview(CraftingRecipe recipe) {
        return null;
    }
}
