package pw.rxj.iron_quarry.event;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;

public interface EnchantmentCallback {
    Event<AllowEnchanting> ALLOW_ENCHANTING = Event.createArrayBacked(AllowEnchanting.class, (listeners) -> (enchantment, target) -> {
        for (AllowEnchanting event : listeners) {
            Boolean allowed = event.onAllowEnchanting(enchantment, target);
            if(allowed != null) return allowed;
        }

        return null;
    });

    interface AllowEnchanting {
        @Nullable Boolean onAllowEnchanting(Holder<Enchantment> enchantment, ItemStack target);
    }
}
