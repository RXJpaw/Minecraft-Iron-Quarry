package pw.rxj.iron_quarry.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import pw.rxj.iron_quarry.types.ExpansionType;
import pw.rxj.iron_quarry.util.ReadableString;

import java.util.function.Consumer;

public class ExpansionCardItem extends Item {
    private final ExpansionType type;

    private ExpansionCardItem(Item.Properties settings, ExpansionType type) {
        super(settings);

        this.type = type;
    }
    public static ExpansionCardItem of(Item.Properties settings, ExpansionType type) {
        return new ExpansionCardItem(settings, type);
    }

    public ExpansionType getType() {
        return this.type;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag options) {
        textConsumer.accept(ReadableString.translatable("item.iron_quarry.expansion.lore.benefit." + this.type.getName()));
    }
}
