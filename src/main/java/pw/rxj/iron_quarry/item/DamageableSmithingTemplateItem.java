package pw.rxj.iron_quarry.item;

import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.Global;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.interfaces.ICustomDurability;
import pw.rxj.iron_quarry.util.ReadableString;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class DamageableSmithingTemplateItem extends SmithingTemplateItem implements ICustomDurability {
    private final Component appliesToText;
    private final Component ingredientsText;
    private final int maxCDamage;

    private DamageableSmithingTemplateItem(Component appliesToText, Component ingredientsText, int maxDamage, Component baseSlotDescriptionText, Component additionsSlotDescriptionText, List<Identifier> emptyBaseSlotTextures, List<Identifier> emptyAdditionsSlotTextures, Item.Properties settings) {
        super(appliesToText, ingredientsText, baseSlotDescriptionText, additionsSlotDescriptionText, emptyBaseSlotTextures, emptyAdditionsSlotTextures, settings);

        this.appliesToText = appliesToText;
        this.ingredientsText = ingredientsText;
        this.maxCDamage = maxDamage;
    }

    public static @Nullable DamageableSmithingTemplateItem instanceFrom(ItemStack stack){
        if (ZUtil.getBlockOrItem(stack) instanceof DamageableSmithingTemplateItem templateItem) {
            return templateItem;
        }

        return null;
    }
    public static boolean isNotOf(ItemStack stack){
        return instanceFrom(stack) == null;
    }

    @Override
    public DataComponentMap components() {
        DataComponentMap components = super.components();

        DataComponentMap.Builder builder = DataComponentMap.builder().addAll(components);
        builder.set(DataComponents.MAX_STACK_SIZE, 1);

        return builder.build();
    }
    @Override
    public int getMaxCDamage() {
        return this.maxCDamage;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag type) {
        int maxDamage = this.getMaxCDamage();
        int durability = this.getCDurability(stack);
        String durabilityPercent = ZUtil.expandableFixedFloat((float) durability / maxDamage * 100);

        MutableComponent LORE_DURABILITY = ReadableString.translatable("item.iron_quarry.lore.custom_durability", ZUtil.expandableFixedInt(durability), ZUtil.expandableFixedInt(maxDamage), durabilityPercent);

        textConsumer.accept(LORE_DURABILITY);
        textConsumer.accept(ReadableString.space());
        textConsumer.accept(APPLIES_TO_TEXT);
        textConsumer.accept(ReadableString.space().append(this.appliesToText));
        textConsumer.accept(INGREDIENTS_TEXT);
        textConsumer.accept(ReadableString.space().append(this.ingredientsText));

    }

    public static DamageableSmithingTemplateItem createAugmentingTemplate(Item.Properties settings) {
        return new DamageableSmithingTemplateItem(AUGMENTING_APPLIES_TO_TEXT, AUGMENTING_INGREDIENTS_TEXT, 18_300, AUGMENTING_BASE_SLOT_DESCRIPTION_TEXT, AUGMENTING_ADDITIONS_SLOT_DESCRIPTION_TEXT,
                List.of(EMPTY_SLOT_AUGMENT_TEXTURE), List.of(EMPTY_SLOT_REDSTONE_DUST_TEXTURE, EMPTY_SLOT_LAPIS_LAZULI_TEXTURE, EMPTY_SLOT_INGOT_TEXTURE), settings);
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
