package pw.rxj.iron_quarry.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.Global;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.util.ReadableString;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.List;
import java.util.function.Consumer;

public abstract class AugmentingTemplateItem extends SmithingTemplateItem {
    protected AugmentingTemplateItem(Item.Properties settings) {
        super(AUGMENTING_APPLIES_TO_TEXT, AUGMENTING_INGREDIENTS_TEXT, AUGMENTING_BASE_SLOT_DESCRIPTION_TEXT, AUGMENTING_ADDITIONS_SLOT_DESCRIPTION_TEXT,
                List.of(EMPTY_SLOT_AUGMENT_TEXTURE), List.of(EMPTY_SLOT_REDSTONE_DUST_TEXTURE, EMPTY_SLOT_LAPIS_LAZULI_TEXTURE, EMPTY_SLOT_INGOT_TEXTURE), settings.stacksTo(1));
    }

    public static @Nullable AugmentingTemplateItem instanceFrom(ItemStack stack){
        if (ZUtil.getBlockOrItem(stack) instanceof AugmentingTemplateItem templateItem) {
            return templateItem;
        }

        return null;
    }
    public static boolean isNotOf(ItemStack stack){
        return instanceFrom(stack) == null;
    }

    public abstract boolean canUse(ItemStack stack, int cost);
    public abstract void use(ItemStack stack, int cost);
    public abstract void breakIfUsedUp(ItemStack stack, Vec3 breakPos, Level breakWorld);

    protected abstract void appendDurabilityText(ItemStack stack, Consumer<Component> textConsumer);

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag type) {
        this.appendDurabilityText(stack, textConsumer);
        textConsumer.accept(ReadableString.space());
        textConsumer.accept(APPLIES_TO_TEXT);
        textConsumer.accept(ReadableString.space().append(AUGMENTING_APPLIES_TO_TEXT));
        textConsumer.accept(INGREDIENTS_TEXT);
        textConsumer.accept(ReadableString.space().append(AUGMENTING_INGREDIENTS_TEXT));
    }

    private static final MutableComponent APPLIES_TO_TEXT = Component.translatable("item.minecraft.smithing_template.applies_to").withStyle(ChatFormatting.GRAY);
    private static final MutableComponent INGREDIENTS_TEXT = Component.translatable("item.minecraft.smithing_template.ingredients").withStyle(ChatFormatting.GRAY);

    private static final MutableComponent AUGMENTING_APPLIES_TO_TEXT = Component.translatable("item.iron_quarry.augmenting_template.lore.any_augment").setStyle(Style.EMPTY.withColor(Global.RGB_WARNING));
    private static final MutableComponent AUGMENTING_INGREDIENTS_TEXT = Component.translatable("item.iron_quarry.augmenting_template.lore.use_rei").setStyle(Style.EMPTY.withColor(Global.RGB_WARNING));
    private static final MutableComponent AUGMENTING_BASE_SLOT_DESCRIPTION_TEXT = Component.translatable("item.iron_quarry.augmenting_template.base_slot_description");
    private static final MutableComponent AUGMENTING_ADDITIONS_SLOT_DESCRIPTION_TEXT = Component.translatable("item.iron_quarry.augmenting_template.additions_slot_description");

    private static final Identifier EMPTY_SLOT_AUGMENT_TEXTURE = Identifier.fromNamespaceAndPath(Main.MOD_ID, "container/slot/augment");
    private static final Identifier EMPTY_SLOT_INGOT_TEXTURE = Identifier.withDefaultNamespace("container/slot/ingot");
    private static final Identifier EMPTY_SLOT_LAPIS_LAZULI_TEXTURE = Identifier.withDefaultNamespace("container/slot/lapis_lazuli");
    private static final Identifier EMPTY_SLOT_REDSTONE_DUST_TEXTURE = Identifier.withDefaultNamespace("container/slot/redstone_dust");
}
