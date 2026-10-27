package pw.rxj.iron_quarry.item;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.interfaces.IModifyCreativeTabEntries;
import pw.rxj.iron_quarry.resource.config.server.QuarryDrillStatsConfig;
import pw.rxj.iron_quarry.resource.config.server.QuarryMonitorConfig;
import pw.rxj.iron_quarry.types.AugmentType;
import pw.rxj.iron_quarry.types.DynamicText;
import pw.rxj.iron_quarry.util.ItemEntry;

import java.util.List;

public class ZItems {
    private static final QuarryDrillStatsConfig.Handler QUARRY_DRILL_STATS_CONFIG = Main.CONFIG.getQuarryDrillStatsConfig();
    private static final QuarryMonitorConfig.Handler QUARRY_MONITOR_CONFIG = Main.CONFIG.getQuarryMonitorConfig();

    public static final ItemEntry<DamageableSmithingTemplateItem> AUGMENTING_TEMPLATE = ItemEntry.of("augmenting_template", settings -> DamageableSmithingTemplateItem.createAugmentingTemplate(settings));

    public static final ItemEntry<BlueprintItem> BLUEPRINT = ItemEntry.of("blueprint", settings -> new BlueprintItem(settings.stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final ItemEntry<Item> CIRCUIT_BLANK = ItemEntry.of("circuit_blank", settings -> new Item(settings.stacksTo(64)));
    public static final ItemEntry<Item> CIRCUIT_MK1 = ItemEntry.of("circuit_mk1", settings -> new Item(settings.stacksTo(64)));
    public static final ItemEntry<Item> CIRCUIT_MK2 = ItemEntry.of("circuit_mk2", settings -> new Item(settings.stacksTo(64)));
    public static final ItemEntry<Item> CIRCUIT_MK2E = ItemEntry.of("circuit_mk2e", settings -> new Item(settings.stacksTo(64)));
    public static final ItemEntry<Item> CIRCUIT_MK3 = ItemEntry.of("circuit_mk3", settings -> new Item(settings.stacksTo(64)));
    public static final ItemEntry<Item> CIRCUIT_MK4 = ItemEntry.of("circuit_mk4", settings -> new Item(settings.stacksTo(64)));
    public static final ItemEntry<Item> CIRCUIT_MK5 = ItemEntry.of("circuit_mk5", settings -> new Item(settings.stacksTo(64)));

    public static final ItemEntry<AugmentItem> AUGMENT = ItemEntry.of("augment", settings -> new AugmentItem(settings.stacksTo(1)));
    public static final ItemEntry<AugmentItem> CHEST_LOOTING_AUGMENT = ItemEntry.of("chest_looting_augment", settings -> new AugmentItem(settings.stacksTo(1))
                    .dynamicName(DynamicText.GOLD)
                    .unique(AugmentType.CHEST_LOOTING));
    public static final ItemEntry<AugmentItem> SILK_TOUCH_AUGMENT = ItemEntry.of("silk_touch_augment", settings -> new AugmentItem(settings.stacksTo(1))
                    .dynamicName(DynamicText.EMERALD)
                    .unique(AugmentType.SILK_TOUCH));


    public static final ItemEntry<DrillItem> COPPER_DRILL = ItemEntry.of("copper_drill", settings -> new DrillItem(settings.stacksTo(1), QUARRY_DRILL_STATS_CONFIG.getCopperDrill()));
    public static final ItemEntry<DrillItem> IRON_DRILL = ItemEntry.of("iron_drill", settings -> new DrillItem(settings.stacksTo(1), QUARRY_DRILL_STATS_CONFIG.getIronDrill()));
    public static final ItemEntry<DrillItem> DIAMOND_DRILL = ItemEntry.of("diamond_drill", settings -> new DrillItem(settings.stacksTo(1), QUARRY_DRILL_STATS_CONFIG.getDiamondDrill()));
    public static final ItemEntry<DrillItem> NETHERITE_DRILL = ItemEntry.of("netherite_drill", settings -> new DrillItem(settings.stacksTo(1), QUARRY_DRILL_STATS_CONFIG.getNetheriteDrill()));
    public static final ItemEntry<DrillItem> SHULKER_DRILL = ItemEntry.of("shulker_drill", settings -> new DrillItem(settings.stacksTo(1), QUARRY_DRILL_STATS_CONFIG.getShulkerDrill()));
    public static final ItemEntry<DrillItem> NETHER_STAR_DRILL = ItemEntry.of("nether_star_drill", settings -> new DrillItem(settings.stacksTo(1).rarity(Rarity.UNCOMMON), QUARRY_DRILL_STATS_CONFIG.getNetherStarDrill()));

    public static final ItemEntry<QuarryMonitorItem> IRON_QUARRY_MONITOR = ItemEntry.of("iron_quarry_monitor", settings -> new QuarryMonitorItem(settings.stacksTo(1), QUARRY_MONITOR_CONFIG.getIronMonitor()));
    public static final ItemEntry<QuarryMonitorItem> DIAMOND_QUARRY_MONITOR = ItemEntry.of("diamond_quarry_monitor", settings -> new QuarryMonitorItem(settings.stacksTo(1), QUARRY_MONITOR_CONFIG.getDiamondMonitor()));
    public static final ItemEntry<QuarryMonitorItem> NETHERITE_QUARRY_MONITOR = ItemEntry.of("netherite_quarry_monitor", settings -> new QuarryMonitorItem(settings.stacksTo(1).fireResistant(), QUARRY_MONITOR_CONFIG.getNetheriteMonitor()));
    public static final ItemEntry<QuarryMonitorItem> SCULK_QUARRY_MONITOR = ItemEntry.of("sculk_quarry_monitor", settings -> new QuarryMonitorItem(settings.stacksTo(1).fireResistant(), QUARRY_MONITOR_CONFIG.getSculkMonitor()));

    private static final List<ItemEntry<?>> itemEntryList = List.of(
            AUGMENTING_TEMPLATE,

            BLUEPRINT,

            CIRCUIT_BLANK,
            CIRCUIT_MK1,
            CIRCUIT_MK2,
            CIRCUIT_MK2E,
            CIRCUIT_MK3,
            CIRCUIT_MK4,
            CIRCUIT_MK5,

            AUGMENT,
            CHEST_LOOTING_AUGMENT,
            SILK_TOUCH_AUGMENT,

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
            Identifier id = itemEntry.getId();
            Item item = itemEntry.getItem();

            Registry.register(BuiltInRegistries.ITEM, id, item);

            if(item instanceof DrillItem drillItem){
                ServerLifecycleEvents.SERVER_STARTING.register(server -> drillItem.assignCorrectToolComponent());
            }
        }

        CreativeModeTabEvents.modifyOutputEvent(Main.ITEM_GROUP).register(entries -> {
            itemEntryList.forEach(blockItemBlockEntry -> {
                Item item = blockItemBlockEntry.getItem();

                if(item instanceof IModifyCreativeTabEntries creativeTabModifier) {
                    creativeTabModifier.modifyCreativeTabEntries(entries);
                } else {
                    entries.accept(item);
                }
            });
        });
    }
}
