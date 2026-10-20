package pw.rxj.iron_quarry.interfaces;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import pw.rxj.iron_quarry.types.ScrollDirection;

public interface IHandledMainHandScrolling {
    /**  Other mouse wheel events will be canceled if true is returned. */
    @Environment(EnvType.CLIENT)
    boolean handleMainHandScrolling(LocalPlayer player, ItemStack stack, ScrollDirection scrollDirection);
}
