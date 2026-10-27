package pw.rxj.iron_quarry.interfaces;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public interface IBlockTooltipAppender {
    void appendTooltip(ItemStack stack, Item.TooltipContext context, Consumer<Component> textConsumer, TooltipFlag type);
}
