package pw.rxj.iron_quarry.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import pw.rxj.iron_quarry.event.EnchantmentCallback;

@Mixin(Item.class)
public abstract class NeoForgeItemEnchantabilityMixin {
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        Boolean allowed = EnchantmentCallback.ALLOW_ENCHANTING.invoker().onAllowEnchanting(enchantment, stack);
        if(allowed != null) return allowed;

        return stack.is(Items.ENCHANTED_BOOK) || enchantment.value().isSupportedItem(stack);
    }
}
