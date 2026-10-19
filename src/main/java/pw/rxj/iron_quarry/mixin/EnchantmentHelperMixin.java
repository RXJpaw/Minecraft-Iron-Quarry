package pw.rxj.iron_quarry.mixin;


import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pw.rxj.iron_quarry.interfaces.ICustomEnchantabilityProvider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
    @Inject(method = "getPossibleEntries", at = @At(value = "HEAD"), cancellable = true)
    private static void getPossibleEntries(FeatureSet enabledFeatures, int power, ItemStack stack, boolean treasureAllowed, CallbackInfoReturnable<List<EnchantmentLevelEntry>> cir) {
        if(stack.getItem() instanceof ICustomEnchantabilityProvider enchantabilityProvider) {
            List<EnchantmentLevelEntry> enchantmentLevelEntries = new ArrayList<>();
            HashSet<Enchantment> viableEnchantments = enchantabilityProvider.getViableEnchantments(stack);

            for (Enchantment enchantment : viableEnchantments) {
                if(!enchantment.isAvailableForRandomSelection()) continue;
                if(enchantment.isTreasure() && !treasureAllowed) continue;

                for(int i = enchantment.getMaxLevel(); i > enchantment.getMinLevel() - 1; --i) {
                    if (power >= enchantment.getMinPower(i) && power <= enchantment.getMaxPower(i)) {
                        enchantmentLevelEntries.add(new EnchantmentLevelEntry(enchantment, i));
                        break;
                    }
                }
            }

            cir.setReturnValue(enchantmentLevelEntries);
        }
    }
}
