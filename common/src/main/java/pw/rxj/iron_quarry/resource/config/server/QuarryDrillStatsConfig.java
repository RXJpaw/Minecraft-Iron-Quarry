package pw.rxj.iron_quarry.resource.config.server;

import lombok.AllArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.resource.config.AbstractInnerConfigHandler;
import pw.rxj.iron_quarry.resource.config.ConfigHandler;
import pw.rxj.iron_quarry.resource.config.util.Comment;
import pw.rxj.iron_quarry.resource.config.util.Range;

import java.util.Optional;

public class QuarryDrillStatsConfig {
    public static final QuarryDrillStatsConfig DEFAULTS = new QuarryDrillStatsConfig();

    public final Entry copperDrill = new Entry(649, 2, "minecraft:copper_pickaxe");
    public final Entry ironDrill = new Entry(1_284, 5, "minecraft:iron_pickaxe");
    public final Entry diamondDrill = new Entry(6_649, 4, "minecraft:diamond_pickaxe");
    public final Entry netheriteDrill = new Entry(93_750,  5, "minecraft:netherite_pickaxe");
    public final Entry shulkerDrill = new Entry(159_375, 7, "minecraft:netherite_pickaxe");
    public final Entry netherStarDrill = new Entry(796_875, 9, "minecraft:netherite_pickaxe");

    @AllArgsConstructor
    public static class Entry {
        @Comment("Blocks the drill can mine before it breaks. Unbreaking makes it last longer.")
        @Range(min = 1)
        public final int durability;
        @Comment("Enchantability like on vanilla tools. Higher gives better enchantments.")
        @Range(min = 1)
        public final int enchantability;
        @Comment("""
        ID of the tool whose mining rules the drill copies, e.g. minecraft:iron_pickaxe. Modded tools work too.
        Decides which blocks drop items. An unknown ID falls back to the default tool. An item without a tool component only gets drops that need no tool.
        Modded tools need a language key for the mining level tooltip, e.g. mining_level.iron_quarry.somemod:pickaxe_of_the_void -> "Void".""")
        public final String toolEquivalent;

        public Optional<Item> getToolItem() {
            return Optional.ofNullable(Identifier.tryParse(this.toolEquivalent)).flatMap(BuiltInRegistries.ITEM::getOptional);
        }
    }

    public static class Handler extends AbstractInnerConfigHandler {
        private Handler(ConfigHandler handler) {
            super(handler);
        }
        public static Handler of(ConfigHandler handler) {
            return new Handler(handler);
        }

        public QuarryDrillStatsConfig.Entry getCopperDrill() {
            return this.getServer().quarryDrillStats.copperDrill;
        }
        public QuarryDrillStatsConfig.Entry getIronDrill() {
            return this.getServer().quarryDrillStats.ironDrill;
        }
        public QuarryDrillStatsConfig.Entry getDiamondDrill() {
            return this.getServer().quarryDrillStats.diamondDrill;
        }
        public QuarryDrillStatsConfig.Entry getNetheriteDrill() {
            return this.getServer().quarryDrillStats.netheriteDrill;
        }
        public QuarryDrillStatsConfig.Entry getShulkerDrill() {
            return this.getServer().quarryDrillStats.shulkerDrill;
        }
        public QuarryDrillStatsConfig.Entry getNetherStarDrill() {
            return this.getServer().quarryDrillStats.netherStarDrill;
        }

        public void validate() {
            validateTool("copperDrill", this.getCopperDrill(), DEFAULTS.copperDrill);
            validateTool("ironDrill", this.getIronDrill(), DEFAULTS.ironDrill);
            validateTool("diamondDrill", this.getDiamondDrill(), DEFAULTS.diamondDrill);
            validateTool("netheriteDrill", this.getNetheriteDrill(), DEFAULTS.netheriteDrill);
            validateTool("shulkerDrill", this.getShulkerDrill(), DEFAULTS.shulkerDrill);
            validateTool("netherStarDrill", this.getNetherStarDrill(), DEFAULTS.netherStarDrill);
        }
        private static void validateTool(String name, QuarryDrillStatsConfig.Entry entry, QuarryDrillStatsConfig.Entry defaultEntry) {
            if(entry.getToolItem().isPresent()) return;

            Main.LOGGER.error("Config quarryDrillStats.{}.toolEquivalent ({}) is not an existing item, using {} instead!", name, entry.toolEquivalent, defaultEntry.toolEquivalent);
        }
    }
}
