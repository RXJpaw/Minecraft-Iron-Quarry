package pw.rxj.iron_quarry.item;

import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.interfaces.ICustomDurability;
import pw.rxj.iron_quarry.interfaces.ICustomEnchantabilityProvider;
import pw.rxj.iron_quarry.resource.config.server.QuarryDrillStatsConfig;
import pw.rxj.iron_quarry.util.ReadableString;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public class DrillItem extends Item implements ICustomEnchantabilityProvider, ICustomDurability {
    private Tool toolComponent;
    private ItemStack toolStack;

    private String toolEquivalent;
    private int enchantability;
    private int maxCDamage;

    public DrillItem(Item.Properties settings, QuarryDrillStatsConfig.Entry entry) {
        super(settings.enchantable(entry.enchantability));

        this.enchantability = entry.enchantability;
        this.toolEquivalent = entry.toolEquivalent;
        this.maxCDamage = entry.durability;
    }
    public void override(QuarryDrillStatsConfig.Entry quarryDrillStats) {
        this.enchantability = quarryDrillStats.enchantability;
        this.toolEquivalent = quarryDrillStats.toolEquivalent;
        this.maxCDamage = quarryDrillStats.durability;

        if(this.toolComponent != null) {
            this.assignCorrectToolComponent();
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag options) {
        int maxDamage = this.getMaxCDamage();
        int durability = maxDamage - this.getCDamage(stack);
        String durabilityPercent = ZUtil.expandableFixedFloat((float) durability / maxDamage * 100);

        MutableComponent LORE_MINING_LEVEL = ReadableString.translatable("item.iron_quarry.lore.mining_level", ReadableString.translatable("mining_level.iron_quarry." + toolEquivalent));
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
        return this.maxCDamage;
    }
    @Override
    public DataComponentMap components() {
        return DataComponentMap.builder()
                .addAll(super.components())
                .set(DataComponents.ENCHANTABLE, new Enchantable(this.enchantability))
                .build();
    }

    protected void assignCorrectToolComponent() {
        Identifier toolId = Identifier.tryParse(this.toolEquivalent);
        if(toolId == null) throw new IllegalStateException("Config.DrillStats.toolEquivalent (" + this.toolEquivalent + ") has incorrect identifier format!");

        Optional<Item> tool = BuiltInRegistries.ITEM.getOptional(toolId);
        if(tool.isEmpty()) throw new IllegalStateException("Config.DrillStats.toolEquivalent (" + this.toolEquivalent + ") is not an existing tool!");

        this.toolStack = tool.get().getDefaultInstance();
        Tool defaultToolComponent = this.toolStack.get(DataComponents.TOOL);

        if(defaultToolComponent == null) {
            Main.LOGGER.warn("Could not find tool component for tool: {}", toolId);
            this.toolComponent = new Tool(List.of(), 0.0F, 0, true);
        } else {
            Main.LOGGER.info("Found tool component for tool: {} {}", toolId, defaultToolComponent.rules());
            this.toolComponent = defaultToolComponent;
        }
    }
    public Tool getToolComponent() {
        Objects.requireNonNull(this.toolComponent);
        return this.toolComponent;
    }
    public ItemStack getToolStack() {
        Objects.requireNonNull(this.toolComponent);
        return this.toolStack;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return Rarity.UNCOMMON.equals(stack.get(DataComponents.RARITY)) || super.isFoil(stack);
    }
}
