package pw.rxj.iron_quarry.item;

import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SmithingTemplateItem;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.Global;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.interfaces.ICustomDurability;
import pw.rxj.iron_quarry.util.ReadableString;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.List;

public class DamageableSmithingTemplateItem extends SmithingTemplateItem implements ICustomDurability {
    private final Text appliesToText;
    private final Text ingredientsText;
    private final int maxCDamage;

    private DamageableSmithingTemplateItem(Text appliesToText, Text ingredientsText, int maxDamage, Text baseSlotDescriptionText, Text additionsSlotDescriptionText, List<Identifier> emptyBaseSlotTextures, List<Identifier> emptyAdditionsSlotTextures, Item.Settings settings) {
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
    public ComponentMap getComponents() {
        ComponentMap components = super.getComponents();

        ComponentMap.Builder builder = ComponentMap.builder().addAll(components);
        builder.add(DataComponentTypes.MAX_STACK_SIZE, 1);

        return builder.build();
    }
    @Override
    public int getMaxCDamage() {
        return this.maxCDamage;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        int maxDamage = this.getMaxCDamage();
        int durability = this.getCDurability(stack);
        String durabilityPercent = ZUtil.expandableFixedFloat((float) durability / maxDamage * 100);

        MutableText LORE_DURABILITY = ReadableString.translatable("item.iron_quarry.lore.custom_durability", ZUtil.expandableFixedInt(durability), ZUtil.expandableFixedInt(maxDamage), durabilityPercent);

        tooltip.add(LORE_DURABILITY);
        tooltip.add(ReadableString.space());
        tooltip.add(APPLIES_TO_TEXT);
        tooltip.add(ReadableString.space().append(this.appliesToText));
        tooltip.add(INGREDIENTS_TEXT);
        tooltip.add(ReadableString.space().append(this.ingredientsText));

    }

    public static DamageableSmithingTemplateItem createAugmentingTemplate(Item.Settings settings) {
        return new DamageableSmithingTemplateItem(AUGMENTING_APPLIES_TO_TEXT, AUGMENTING_INGREDIENTS_TEXT, 18_300, AUGMENTING_BASE_SLOT_DESCRIPTION_TEXT, AUGMENTING_ADDITIONS_SLOT_DESCRIPTION_TEXT,
                List.of(EMPTY_SLOT_AUGMENT_TEXTURE), List.of(EMPTY_SLOT_REDSTONE_DUST_TEXTURE, EMPTY_SLOT_LAPIS_LAZULI_TEXTURE, EMPTY_SLOT_INGOT_TEXTURE), settings);
    }

    private static final MutableText APPLIES_TO_TEXT = Text.translatable("item.minecraft.smithing_template.applies_to").formatted(Formatting.GRAY);
    private static final MutableText INGREDIENTS_TEXT = Text.translatable("item.minecraft.smithing_template.ingredients").formatted(Formatting.GRAY);

    private static final MutableText AUGMENTING_APPLIES_TO_TEXT = Text.translatable("item.iron_quarry.augmenting_template.lore.any_augment").setStyle(Style.EMPTY.withColor(Global.RGB_WARNING));
    private static final MutableText AUGMENTING_INGREDIENTS_TEXT = Text.translatable("item.iron_quarry.augmenting_template.lore.use_rei").setStyle(Style.EMPTY.withColor(Global.RGB_WARNING));
    private static final MutableText AUGMENTING_BASE_SLOT_DESCRIPTION_TEXT = Text.translatable("item.iron_quarry.augmenting_template.base_slot_description");
    private static final MutableText AUGMENTING_ADDITIONS_SLOT_DESCRIPTION_TEXT = Text.translatable("item.iron_quarry.augmenting_template.additions_slot_description");

    private static final Identifier EMPTY_SLOT_AUGMENT_TEXTURE = Identifier.of(Main.MOD_ID, "container/slot/augment");
    private static final Identifier EMPTY_SLOT_INGOT_TEXTURE = Identifier.ofVanilla("container/slot/ingot");
    private static final Identifier EMPTY_SLOT_LAPIS_LAZULI_TEXTURE = Identifier.ofVanilla("container/slot/lapis_lazuli");
    private static final Identifier EMPTY_SLOT_REDSTONE_DUST_TEXTURE = Identifier.ofVanilla("container/slot/redstone_dust");

}
