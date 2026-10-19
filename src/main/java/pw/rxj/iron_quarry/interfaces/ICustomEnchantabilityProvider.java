package pw.rxj.iron_quarry.interfaces;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;

import java.util.Set;

public interface ICustomEnchantabilityProvider {
    Set<RegistryKey<Enchantment>> getViableEnchantments(ItemStack stack);
}
