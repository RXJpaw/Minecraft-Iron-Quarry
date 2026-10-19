package pw.rxj.iron_quarry.interfaces;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public interface IBlockTooltipAppender {
    void appendTooltip(ItemStack stack, Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type);
}
