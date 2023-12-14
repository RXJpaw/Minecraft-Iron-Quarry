package pw.rxj.iron_quarry.interfaces;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import pw.rxj.iron_quarry.types.ScrollDirection;

public interface IHandledMainHandScrolling {
    /**  Other mouse wheel events will be canceled if true is returned. */
    @Environment(EnvType.CLIENT)
    boolean handleMainHandScrolling(ClientPlayerEntity player, ItemStack stack, ScrollDirection scrollDirection);
}
