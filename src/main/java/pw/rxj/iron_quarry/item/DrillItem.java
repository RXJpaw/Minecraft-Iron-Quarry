package pw.rxj.iron_quarry.item;

import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EnchantableComponent;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
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

public class DrillItem extends Item implements ICustomEnchantabilityProvider, ICustomDurability {
    private ToolComponent toolComponent;
    private ItemStack toolStack;

    private String toolEquivalent;
    private int enchantability;
    private int maxCDamage;

    public DrillItem(Item.Settings settings, QuarryDrillStatsConfig.Entry entry) {
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
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType options) {
        int maxDamage = this.getMaxCDamage();
        int durability = maxDamage - this.getCDamage(stack);
        String durabilityPercent = ZUtil.expandableFixedFloat((float) durability / maxDamage * 100);

        MutableText LORE_MINING_LEVEL = ReadableString.translatable("item.iron_quarry.lore.mining_level", ReadableString.translatable("mining_level.iron_quarry." + toolEquivalent));
        MutableText LORE_DURABILITY = ReadableString.translatable("item.iron_quarry.lore.custom_durability", ZUtil.expandableFixedInt(durability), ZUtil.expandableFixedInt(maxDamage), durabilityPercent);

        tooltip.add(LORE_MINING_LEVEL);
        tooltip.add(LORE_DURABILITY);

        if(stack.hasEnchantments()) {
            tooltip.add(ReadableString.space());
        }
    }

    @Override
    public Set<RegistryKey<Enchantment>> getViableEnchantments(ItemStack stack) {
        return Set.of(Enchantments.UNBREAKING, Enchantments.EFFICIENCY);
    }
    @Override
    public int getMaxCDamage() {
        return this.maxCDamage;
    }
    @Override
    public ComponentMap getComponents() {
        return ComponentMap.builder()
                .addAll(super.getComponents())
                .add(DataComponentTypes.ENCHANTABLE, new EnchantableComponent(this.enchantability))
                .build();
    }

    protected void assignCorrectToolComponent() {
        Identifier toolId = Identifier.tryParse(this.toolEquivalent);
        if(toolId == null) throw new IllegalStateException("Config.DrillStats.toolEquivalent (" + this.toolEquivalent + ") has incorrect identifier format!");

        Optional<Item> tool = Registries.ITEM.getOptionalValue(toolId);
        if(tool.isEmpty()) throw new IllegalStateException("Config.DrillStats.toolEquivalent (" + this.toolEquivalent + ") is not an existing tool!");

        this.toolStack = tool.get().getDefaultStack();
        ToolComponent defaultToolComponent = this.toolStack.get(DataComponentTypes.TOOL);

        if(defaultToolComponent == null) {
            Main.LOGGER.warn("Could not find tool component for tool: {}", toolId);
            this.toolComponent = new ToolComponent(List.of(), 0.0F, 0);
        } else {
            Main.LOGGER.info("Found tool component for tool: {} {}", toolId, defaultToolComponent.rules());
            this.toolComponent = defaultToolComponent;
        }
    }
    public ToolComponent getToolComponent() {
        Objects.requireNonNull(this.toolComponent);
        return this.toolComponent;
    }
    public ItemStack getToolStack() {
        Objects.requireNonNull(this.toolComponent);
        return this.toolStack;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return Rarity.UNCOMMON.equals(stack.get(DataComponentTypes.RARITY)) || super.hasGlint(stack);
    }
}
