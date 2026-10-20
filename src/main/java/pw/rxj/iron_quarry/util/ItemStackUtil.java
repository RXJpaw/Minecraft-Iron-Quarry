package pw.rxj.iron_quarry.util;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ItemStackUtil<T extends ItemLike> {
    public final ItemStack stack;
    public final T item;

    private ItemStackUtil(ItemStack stack, T item) {
        this.stack = stack;
        this.item = item;
    }

    public static @Nullable <T extends ItemLike> ItemStackUtil<T> of(ItemStack stack, Class<T> clazz) {
        ItemLike item = ZUtil.getBlockOrItem(stack);

        if(clazz.isInstance(item)) {
            return new ItemStackUtil<>(stack, clazz.cast(item));
        } else {
            return null;
        }
    }
    public static <T extends ItemLike> Optional<ItemStackUtil<T>> ofAnyHand(Player player, Class<T> clazz) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stackInHand = player.getItemInHand(hand);
            ItemStackUtil<T> stackUtil = of(stackInHand, clazz);
            if(stackUtil != null) return Optional.of(stackUtil);
        }

        return Optional.empty();
    }
}
