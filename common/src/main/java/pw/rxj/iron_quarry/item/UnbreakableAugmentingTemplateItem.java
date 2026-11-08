package pw.rxj.iron_quarry.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public class UnbreakableAugmentingTemplateItem extends AugmentingTemplateItem {
    private UnbreakableAugmentingTemplateItem(Item.Properties settings) {
        super(settings);
    }
    public static UnbreakableAugmentingTemplateItem of(Item.Properties settings) {
        return new UnbreakableAugmentingTemplateItem(settings);
    }

    @Override
    public boolean canUse(ItemStack stack, int cost) {
        return true;
    }
    @Override
    public void use(ItemStack stack, int cost) { }
    @Override
    public void breakIfUsedUp(ItemStack stack, Vec3 breakPos, Level breakWorld) { }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    protected void appendDurabilityText(ItemStack stack, Consumer<Component> textConsumer) {
        textConsumer.accept(UNBREAKABLE_TEXT);
    }

    private static final MutableComponent UNBREAKABLE_TEXT = Component.translatable("item.unbreakable").withStyle(ChatFormatting.BLUE);
}
