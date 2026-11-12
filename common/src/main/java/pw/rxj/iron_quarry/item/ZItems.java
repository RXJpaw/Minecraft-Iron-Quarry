package pw.rxj.iron_quarry.item;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.interfaces.IModifyCreativeTabEntries;
import pw.rxj.iron_quarry.resource.config.server.AugmentingTemplateConfig;
import pw.rxj.iron_quarry.resource.config.server.QuarryDrillStatsConfig;
import pw.rxj.iron_quarry.resource.config.server.QuarryMonitorConfig;
import pw.rxj.iron_quarry.types.AugmentType;
import pw.rxj.iron_quarry.types.DynamicText;
import pw.rxj.iron_quarry.types.ExpansionType;
import pw.rxj.iron_quarry.types.MiningPattern;
import pw.rxj.iron_quarry.util.ItemEntry;

import java.util.List;

public class ZItems {
    private static final AugmentingTemplateConfig.Handler AUGMENTING_TEMPLATE_CONFIG = Main.CONFIG.getAugmentingTemplateConfig();
    private static final QuarryDrillStatsConfig.Handler QUARRY_DRILL_STATS_CONFIG = Main.CONFIG.getQuarryDrillStatsConfig();
    private static final QuarryMonitorConfig.Handler QUARRY_MONITOR_CONFIG = Main.CONFIG.getQuarryMonitorConfig();

    public static final ItemEntry<DamageableAugmentingTemplateItem> AUGMENTING_TEMPLATE = ItemEntry.of("augmenting_template", settings -> DamageableAugmentingTemplateItem.of(settings, AUGMENTING_TEMPLATE_CONFIG::getDurability));
    public static final ItemEntry<UnbreakableAugmentingTemplateItem> CELESTIAL_AUGMENTING_TEMPLATE = ItemEntry.of("celestial_augmenting_template", settings -> UnbreakableAugmentingTemplateItem.of(settings));

    public static final ItemEntry<BlueprintItem> BLUEPRINT = ItemEntry.of("blueprint", settings -> new BlueprintItem(settings.stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final ItemEntry<Item> CIRCUIT_BLANK = ItemEntry.of("circuit_blank", settings -> new Item(settings.stacksTo(64)));
    public static final ItemEntry<Item> CIRCUIT_MK1 = ItemEntry.of("circuit_mk1", settings -> new Item(settings.stacksTo(64)));
    public static final ItemEntry<Item> CIRCUIT_MK2 = ItemEntry.of("circuit_mk2", settings -> new Item(settings.stacksTo(64)));
    public static final ItemEntry<Item> CIRCUIT_MK2E = ItemEntry.of("circuit_mk2e", settings -> new Item(settings.stacksTo(64)));
    public static final ItemEntry<Item> CIRCUIT_MK3 = ItemEntry.of("circuit_mk3", settings -> new Item(settings.stacksTo(64)));
    public static final ItemEntry<Item> CIRCUIT_MK4 = ItemEntry.of("circuit_mk4", settings -> new Item(settings.stacksTo(64)));
    public static final ItemEntry<Item> CIRCUIT_MK5 = ItemEntry.of("circuit_mk5", settings -> new Item(settings.stacksTo(64)));

    public static final ItemEntry<Item> REDSTONE_CORE = ItemEntry.of("redstone_core", settings -> new Item(settings.stacksTo(64)));
    public static final ItemEntry<Item> LAPIS_CORE = ItemEntry.of("lapis_core", settings -> new Item(settings.stacksTo(64)));

    public static final ItemEntry<AugmentItem> AUGMENT = ItemEntry.of("augment", settings -> new AugmentItem(settings.stacksTo(1), AUGMENTING_TEMPLATE_CONFIG::getCapacityUpgradeCost));
    public static final ItemEntry<AugmentItem> CHEST_LOOTING_AUGMENT = ItemEntry.of("chest_looting_augment", settings -> new AugmentItem(settings.stacksTo(1), AUGMENTING_TEMPLATE_CONFIG::getCapacityUpgradeCost)
                    .dynamicName(DynamicText.GOLD)
                    .unique(AugmentType.CHEST_LOOTING));
    public static final ItemEntry<AugmentItem> SILK_TOUCH_AUGMENT = ItemEntry.of("silk_touch_augment", settings -> new AugmentItem(settings.stacksTo(1), AUGMENTING_TEMPLATE_CONFIG::getCapacityUpgradeCost)
                    .dynamicName(DynamicText.EMERALD)
                    .unique(AugmentType.SILK_TOUCH));

    public static final ItemEntry<Item> BLANK_EXPANSION_CARD = ItemEntry.of("blank_expansion_card", settings -> new Item(settings.stacksTo(16)));
    public static final ItemEntry<ExpansionCardItem> INTERDIMENSIONAL_EXPANSION_CARD = ItemEntry.of("interdimensional_expansion_card", settings -> ExpansionCardItem.of(settings.stacksTo(1), ExpansionType.INTERDIMENSIONAL));
    public static final ItemEntry<ExpansionCardItem> SNAKE_PATTERN_EXPANSION_CARD = ItemEntry.of("snake_pattern_expansion_card", settings -> ExpansionCardItem.ofMiningPattern(settings.stacksTo(1), MiningPattern.SNAKE));
    public static final ItemEntry<ExpansionCardItem> GILBERT_PATTERN_EXPANSION_CARD = ItemEntry.of("gilbert_pattern_expansion_card", settings -> ExpansionCardItem.ofMiningPattern(settings.stacksTo(1), MiningPattern.GILBERT));

    public static final ItemEntry<DrillItem> COPPER_DRILL = ItemEntry.of("copper_drill", settings -> new DrillItem(settings.stacksTo(1), QUARRY_DRILL_STATS_CONFIG::getCopperDrill, QuarryDrillStatsConfig.DEFAULTS.copper));
    public static final ItemEntry<DrillItem> IRON_DRILL = ItemEntry.of("iron_drill", settings -> new DrillItem(settings.stacksTo(1), QUARRY_DRILL_STATS_CONFIG::getIronDrill, QuarryDrillStatsConfig.DEFAULTS.iron));
    public static final ItemEntry<DrillItem> DIAMOND_DRILL = ItemEntry.of("diamond_drill", settings -> new DrillItem(settings.stacksTo(1), QUARRY_DRILL_STATS_CONFIG::getDiamondDrill, QuarryDrillStatsConfig.DEFAULTS.diamond));
    public static final ItemEntry<DrillItem> NETHERITE_DRILL = ItemEntry.of("netherite_drill", settings -> new DrillItem(settings.stacksTo(1), QUARRY_DRILL_STATS_CONFIG::getNetheriteDrill, QuarryDrillStatsConfig.DEFAULTS.netherite));
    public static final ItemEntry<DrillItem> SHULKER_DRILL = ItemEntry.of("shulker_drill", settings -> new DrillItem(settings.stacksTo(1), QUARRY_DRILL_STATS_CONFIG::getShulkerDrill, QuarryDrillStatsConfig.DEFAULTS.shulker));
    public static final ItemEntry<DrillItem> NETHER_STAR_DRILL = ItemEntry.of("nether_star_drill", settings -> new DrillItem(settings.stacksTo(1).rarity(Rarity.UNCOMMON), QUARRY_DRILL_STATS_CONFIG::getNetherStarDrill, QuarryDrillStatsConfig.DEFAULTS.netherStar));

    public static final ItemEntry<QuarryMonitorItem> IRON_QUARRY_MONITOR = ItemEntry.of("iron_quarry_monitor", settings -> new QuarryMonitorItem(settings.stacksTo(1), QUARRY_MONITOR_CONFIG::getIronMonitor));
    public static final ItemEntry<QuarryMonitorItem> DIAMOND_QUARRY_MONITOR = ItemEntry.of("diamond_quarry_monitor", settings -> new QuarryMonitorItem(settings.stacksTo(1), QUARRY_MONITOR_CONFIG::getDiamondMonitor));
    public static final ItemEntry<QuarryMonitorItem> NETHERITE_QUARRY_MONITOR = ItemEntry.of("netherite_quarry_monitor", settings -> new QuarryMonitorItem(settings.stacksTo(1).fireResistant(), QUARRY_MONITOR_CONFIG::getNetheriteMonitor));
    public static final ItemEntry<QuarryMonitorItem> SCULK_QUARRY_MONITOR = ItemEntry.of("sculk_quarry_monitor", settings -> new QuarryMonitorItem(settings.stacksTo(1).fireResistant(), QUARRY_MONITOR_CONFIG::getSculkMonitor));

    private static final List<ItemEntry<?>> itemEntryList = List.of(
            AUGMENTING_TEMPLATE,
            CELESTIAL_AUGMENTING_TEMPLATE,

            BLUEPRINT,

            CIRCUIT_BLANK,
            CIRCUIT_MK1,
            CIRCUIT_MK2,
            CIRCUIT_MK2E,
            CIRCUIT_MK3,
            CIRCUIT_MK4,
            CIRCUIT_MK5,

            REDSTONE_CORE,
            LAPIS_CORE,

            AUGMENT,
            CHEST_LOOTING_AUGMENT,
            SILK_TOUCH_AUGMENT,

            BLANK_EXPANSION_CARD,
            INTERDIMENSIONAL_EXPANSION_CARD,
            SNAKE_PATTERN_EXPANSION_CARD,
            GILBERT_PATTERN_EXPANSION_CARD,

            COPPER_DRILL,
            IRON_DRILL,
            DIAMOND_DRILL,
            NETHERITE_DRILL,
            SHULKER_DRILL,
            NETHER_STAR_DRILL,

            IRON_QUARRY_MONITOR,
            DIAMOND_QUARRY_MONITOR,
            NETHERITE_QUARRY_MONITOR,
            SCULK_QUARRY_MONITOR
    );

    public static void register(){
        for (ItemEntry<?> itemEntry : itemEntryList) {
            itemEntry.register();
        }
    }

    public static void fillCreativeTab(CreativeModeTab.Output entries) {
        itemEntryList.forEach(blockItemBlockEntry -> {
            Item item = blockItemBlockEntry.getItem();

            if(item instanceof IModifyCreativeTabEntries creativeTabModifier) {
                creativeTabModifier.modifyCreativeTabEntries(entries);
            } else {
                entries.accept(item);
            }
        });
    }
}
