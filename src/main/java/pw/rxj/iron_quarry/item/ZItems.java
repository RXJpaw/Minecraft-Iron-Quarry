package pw.rxj.iron_quarry.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.event.GameLifecycleCallback;
import pw.rxj.iron_quarry.interfaces.IModifyItemGroupEntries;
import pw.rxj.iron_quarry.resource.config.server.QuarryDrillStatsConfig;
import pw.rxj.iron_quarry.resource.config.server.QuarryMonitorConfig;
import pw.rxj.iron_quarry.types.AugmentType;
import pw.rxj.iron_quarry.types.DynamicText;
import pw.rxj.iron_quarry.util.ItemEntry;

import java.util.List;

public class ZItems {
    private static final QuarryDrillStatsConfig.Handler QUARRY_DRILL_STATS_CONFIG = Main.CONFIG.getQuarryDrillStatsConfig();
    private static final QuarryMonitorConfig.Handler QUARRY_MONITOR_CONFIG = Main.CONFIG.getQuarryMonitorConfig();

    public static final ItemEntry<DamageableSmithingTemplateItem> AUGMENTING_TEMPLATE = ItemEntry.<DamageableSmithingTemplateItem>build()
            .withItem(DamageableSmithingTemplateItem.createAugmentingTemplate())
            .withId(Main.MOD_ID, "augmenting_template");

    public static final ItemEntry<BlueprintItem> BLUEPRINT = ItemEntry.<BlueprintItem>build()
            .withItem(new BlueprintItem(new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON)))
            .withId(Main.MOD_ID, "blueprint");

    public static final ItemEntry<Item> CIRCUIT_BLANK = ItemEntry.build()
            .withItem(new Item(new Item.Settings().maxCount(64)))
            .withId(Main.MOD_ID, "circuit_blank");
    public static final ItemEntry<Item> CIRCUIT_MK1 = ItemEntry.build()
            .withItem(new Item(new Item.Settings().maxCount(64)))
            .withId(Main.MOD_ID, "circuit_mk1");
    public static final ItemEntry<Item> CIRCUIT_MK2 = ItemEntry.build()
            .withItem(new Item(new Item.Settings().maxCount(64)))
            .withId(Main.MOD_ID, "circuit_mk2");
    public static final ItemEntry<Item> CIRCUIT_MK2E = ItemEntry.build()
            .withItem(new Item(new Item.Settings().maxCount(64)))
            .withId(Main.MOD_ID, "circuit_mk2e");
    public static final ItemEntry<Item> CIRCUIT_MK3 = ItemEntry.build()
            .withItem(new Item(new Item.Settings().maxCount(64)))
            .withId(Main.MOD_ID, "circuit_mk3");
    public static final ItemEntry<Item> CIRCUIT_MK4 = ItemEntry.build()
            .withItem(new Item(new Item.Settings().maxCount(64)))
            .withId(Main.MOD_ID, "circuit_mk4");
    public static final ItemEntry<Item> CIRCUIT_MK5 = ItemEntry.build()
            .withItem(new Item(new Item.Settings().maxCount(64)))
            .withId(Main.MOD_ID, "circuit_mk5");

    public static final ItemEntry<AugmentItem> AUGMENT = ItemEntry.<AugmentItem>build()
            .withItem(new AugmentItem(new Item.Settings().maxCount(1)))
            .withId(Main.MOD_ID, "augment");
    public static final ItemEntry<AugmentItem> CHEST_LOOTING_AUGMENT = ItemEntry.<AugmentItem>build()
            .withItem(new AugmentItem(new Item.Settings().maxCount(1))
                    .dynamicName(DynamicText.GOLD)
                    .unique(AugmentType.CHEST_LOOTING)
            )
            .withId(Main.MOD_ID, "chest_looting_augment");
    public static final ItemEntry<AugmentItem> SILK_TOUCH_AUGMENT = ItemEntry.<AugmentItem>build()
            .withItem(new AugmentItem(new Item.Settings().maxCount(1))
                    .dynamicName(DynamicText.EMERALD)
                    .unique(AugmentType.SILK_TOUCH)
            )
            .withId(Main.MOD_ID, "silk_touch_augment");


    public static final ItemEntry<DrillItem> COPPER_DRILL = ItemEntry.<DrillItem>build()
            .withItem(new DrillItem(new Item.Settings().maxCount(1), QUARRY_DRILL_STATS_CONFIG.getCopperDrill()))
            .withId(Main.MOD_ID, "copper_drill");
    public static final ItemEntry<DrillItem> IRON_DRILL = ItemEntry.<DrillItem>build()
            .withItem(new DrillItem(new Item.Settings().maxCount(1), QUARRY_DRILL_STATS_CONFIG.getIronDrill()))
            .withId(Main.MOD_ID, "iron_drill");
    public static final ItemEntry<DrillItem> DIAMOND_DRILL = ItemEntry.<DrillItem>build()
            .withItem(new DrillItem(new Item.Settings().maxCount(1), QUARRY_DRILL_STATS_CONFIG.getDiamondDrill()))
            .withId(Main.MOD_ID, "diamond_drill");
    public static final ItemEntry<DrillItem> NETHERITE_DRILL = ItemEntry.<DrillItem>build()
            .withItem(new DrillItem(new Item.Settings().maxCount(1), QUARRY_DRILL_STATS_CONFIG.getNetheriteDrill()))
            .withId(Main.MOD_ID, "netherite_drill");
    public static final ItemEntry<DrillItem> SHULKER_DRILL = ItemEntry.<DrillItem>build()
            .withItem(new DrillItem(new Item.Settings().maxCount(1), QUARRY_DRILL_STATS_CONFIG.getShulkerDrill()))
            .withId(Main.MOD_ID, "shulker_drill");
    public static final ItemEntry<DrillItem> NETHER_STAR_DRILL = ItemEntry.<DrillItem>build()
            .withItem(new DrillItem(new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON), QUARRY_DRILL_STATS_CONFIG.getNetherStarDrill()))
            .withId(Main.MOD_ID, "nether_star_drill");

    public static final ItemEntry<QuarryMonitorItem> IRON_QUARRY_MONITOR = ItemEntry.<QuarryMonitorItem>build()
            .withItem(new QuarryMonitorItem(new Item.Settings().maxCount(1), QUARRY_MONITOR_CONFIG.getIronMonitor()))
            .withId(Main.MOD_ID, "iron_quarry_monitor");
    public static final ItemEntry<QuarryMonitorItem> DIAMOND_QUARRY_MONITOR = ItemEntry.<QuarryMonitorItem>build()
            .withItem(new QuarryMonitorItem(new Item.Settings().maxCount(1), QUARRY_MONITOR_CONFIG.getDiamondMonitor()))
            .withId(Main.MOD_ID, "diamond_quarry_monitor");
    public static final ItemEntry<QuarryMonitorItem> NETHERITE_QUARRY_MONITOR = ItemEntry.<QuarryMonitorItem>build()
            .withItem(new QuarryMonitorItem(new Item.Settings().maxCount(1).fireproof(), QUARRY_MONITOR_CONFIG.getNetheriteMonitor()))
            .withId(Main.MOD_ID, "netherite_quarry_monitor");
    public static final ItemEntry<QuarryMonitorItem> SCULK_QUARRY_MONITOR = ItemEntry.<QuarryMonitorItem>build()
            .withItem(new QuarryMonitorItem(new Item.Settings().maxCount(1).fireproof(), QUARRY_MONITOR_CONFIG.getSculkMonitor()))
            .withId(Main.MOD_ID, "sculk_quarry_monitor");

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

            Registry.register(Registries.ITEM, id, item);

            if(item instanceof DrillItem drillItem){
                GameLifecycleCallback.IMMINENT_FIRST_RELOAD.register(drillItem::assignCorrectToolComponent);
            }
        }

        ItemGroupEvents.modifyEntriesEvent(Main.ITEM_GROUP).register(entries -> {
            itemEntryList.forEach(blockItemBlockEntry -> {
                Item item = blockItemBlockEntry.getItem();

                if(item instanceof IModifyItemGroupEntries itemGroupModifier) {
                    itemGroupModifier.onModifyItemGroupEntry(entries);
                } else {
                    entries.add(item);
                }
            });
        });
    }
}
