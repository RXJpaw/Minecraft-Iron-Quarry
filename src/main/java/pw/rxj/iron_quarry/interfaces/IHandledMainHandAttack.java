package pw.rxj.iron_quarry.interfaces;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

public interface IHandledMainHandAttack {
    @Environment(EnvType.CLIENT)
    boolean handleMainHandAttack(LocalPlayer player, ItemStack stack, int clickCount);
}
