package pw.rxj.iron_quarry.interfaces;

import java.util.Set;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public interface ICustomEnchantabilityProvider {
    Set<ResourceKey<Enchantment>> getViableEnchantments(ItemStack stack);
}
