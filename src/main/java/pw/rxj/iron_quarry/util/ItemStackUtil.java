package pw.rxj.iron_quarry.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ItemStackUtil<T extends ItemConvertible> {
    public final ItemStack stack;
    public final T item;

    private ItemStackUtil(ItemStack stack, T item) {
        this.stack = stack;
        this.item = item;
    }

    public static @Nullable <T extends ItemConvertible> ItemStackUtil<T> of(ItemStack stack, Class<T> clazz) {
        ItemConvertible item = ZUtil.getBlockOrItem(stack);

        if(clazz.isInstance(item)) {
            return new ItemStackUtil<>(stack, clazz.cast(item));
        } else {
            return null;
        }
    }
    public static <T extends ItemConvertible> Optional<ItemStackUtil<T>> ofAnyHand(PlayerEntity player, Class<T> clazz) {
        for (Hand hand : Hand.values()) {
            ItemStack stackInHand = player.getStackInHand(hand);
            ItemStackUtil<T> stackUtil = of(stackInHand, clazz);
            if(stackUtil != null) return Optional.of(stackUtil);
        }

        return Optional.empty();
    }
}
