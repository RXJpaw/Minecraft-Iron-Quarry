package pw.rxj.iron_quarry.interfaces;

import net.minecraft.world.item.ItemStack;
import pw.rxj.iron_quarry.util.ZUtil;

public interface ISilentComponentUpdate {
    boolean isSilentComponentUpdate(ItemStack oldStack, ItemStack newStack);

    static boolean isSilent(ItemStack oldStack, ItemStack newStack) {
        if(oldStack == newStack || !oldStack.is(newStack.getItem())) return false;

        return ZUtil.getBlockOrItem(oldStack) instanceof ISilentComponentUpdate silentComponentUpdate && silentComponentUpdate.isSilentComponentUpdate(oldStack, newStack);
    }
}
