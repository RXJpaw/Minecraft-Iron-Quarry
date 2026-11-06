package pw.rxj.iron_quarry.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import pw.rxj.iron_quarry.interfaces.ICustomDurability;
import pw.rxj.iron_quarry.interfaces.ICustomEnchantabilityProvider;
import pw.rxj.iron_quarry.resource.config.server.QuarryDrillStatsConfig;
import pw.rxj.iron_quarry.util.ReadableString;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DrillItem extends Item implements ICustomEnchantabilityProvider, ICustomDurability {
    private static final Tool NO_TOOL = new Tool(List.of(), 0.0F, 0, true);

    private final Supplier<QuarryDrillStatsConfig.Entry> config;
    private final QuarryDrillStatsConfig.Entry defaultConfig;

    public DrillItem(Item.Properties settings, Supplier<QuarryDrillStatsConfig.Entry> config, QuarryDrillStatsConfig.Entry defaultConfig) {
        super(settings);

        this.config = config;
        this.defaultConfig = defaultConfig;

        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.add(settings.itemIdOrThrow(), (components, context, key) -> components.set(DataComponents.ENCHANTABLE, new Enchantable(this.getEnchantability())));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag options) {
        int maxDamage = this.getMaxCDamage();
        int durability = maxDamage - this.getCDamage(stack);
        String durabilityPercent = ZUtil.expandableFixedFloat((float) durability / maxDamage * 100);

        MutableComponent LORE_MINING_LEVEL = ReadableString.translatable("item.iron_quarry.lore.mining_level", ReadableString.translatable("mining_level.iron_quarry." + BuiltInRegistries.ITEM.getKey(this.getToolItem())));
        MutableComponent LORE_DURABILITY = ReadableString.translatable("item.iron_quarry.lore.custom_durability", ZUtil.expandableFixedInt(durability), ZUtil.expandableFixedInt(maxDamage), durabilityPercent);

        textConsumer.accept(LORE_MINING_LEVEL);
        textConsumer.accept(LORE_DURABILITY);

        if(stack.isEnchanted()) {
            textConsumer.accept(ReadableString.space());
        }
    }

    @Override
    public Set<ResourceKey<Enchantment>> getViableEnchantments(ItemStack stack) {
        return Set.of(Enchantments.UNBREAKING, Enchantments.EFFICIENCY);
    }
    @Override
    public int getMaxCDamage() {
        return this.config.get().durability;
    }
    public int getEnchantability() {
        return this.config.get().enchantability;
    }

    public Item getToolItem() {
        return this.config.get().getToolItem().or(this.defaultConfig::getToolItem).orElse(Items.AIR);
    }
    public Tool getToolComponent() {
        return this.getToolItem().components().getOrDefault(DataComponents.TOOL, NO_TOOL);
    }
    public ItemStack createToolStack() {
        ItemStack toolStack = new ItemStack(this.getToolItem());
        toolStack.set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

        return toolStack;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return Rarity.UNCOMMON.equals(stack.get(DataComponents.RARITY)) || super.isFoil(stack);
    }
}
