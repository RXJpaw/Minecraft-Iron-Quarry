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

    public final Entry copper = new Entry(649, 2, "minecraft:copper_pickaxe");
    public final Entry iron = new Entry(1_284, 5, "minecraft:iron_pickaxe");
    public final Entry diamond = new Entry(6_649, 4, "minecraft:diamond_pickaxe");
    public final Entry netherite = new Entry(93_750,  5, "minecraft:netherite_pickaxe");
    public final Entry shulker = new Entry(159_375, 7, "minecraft:netherite_pickaxe");
    public final Entry netherStar = new Entry(796_875, 9, "minecraft:netherite_pickaxe");

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
            return this.getServer().drills.copper;
        }
        public QuarryDrillStatsConfig.Entry getIronDrill() {
            return this.getServer().drills.iron;
        }
        public QuarryDrillStatsConfig.Entry getDiamondDrill() {
            return this.getServer().drills.diamond;
        }
        public QuarryDrillStatsConfig.Entry getNetheriteDrill() {
            return this.getServer().drills.netherite;
        }
        public QuarryDrillStatsConfig.Entry getShulkerDrill() {
            return this.getServer().drills.shulker;
        }
        public QuarryDrillStatsConfig.Entry getNetherStarDrill() {
            return this.getServer().drills.netherStar;
        }

        public void validate() {
            validateTool("copper", this.getCopperDrill(), DEFAULTS.copper);
            validateTool("iron", this.getIronDrill(), DEFAULTS.iron);
            validateTool("diamond", this.getDiamondDrill(), DEFAULTS.diamond);
            validateTool("netherite", this.getNetheriteDrill(), DEFAULTS.netherite);
            validateTool("shulker", this.getShulkerDrill(), DEFAULTS.shulker);
            validateTool("netherStar", this.getNetherStarDrill(), DEFAULTS.netherStar);
        }
        private static void validateTool(String name, QuarryDrillStatsConfig.Entry entry, QuarryDrillStatsConfig.Entry defaultEntry) {
            if(entry.getToolItem().isPresent()) return;

            Main.LOGGER.error("Config drills.{}.toolEquivalent ({}) is not an existing item, using {} instead!", name, entry.toolEquivalent, defaultEntry.toolEquivalent);
        }
    }
}
