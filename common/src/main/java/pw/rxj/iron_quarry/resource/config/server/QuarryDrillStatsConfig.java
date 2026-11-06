package pw.rxj.iron_quarry.resource.config.server;

import lombok.AllArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.resource.config.AbstractInnerConfigHandler;
import pw.rxj.iron_quarry.resource.config.ConfigHandler;

import java.util.Optional;

public class QuarryDrillStatsConfig {
    public static final QuarryDrillStatsConfig DEFAULTS = new QuarryDrillStatsConfig();

    public final Entry copperDrill = new Entry(649, 2, "minecraft:stone_pickaxe");
    public final Entry ironDrill = new Entry(1_284, 5, "minecraft:iron_pickaxe");
    public final Entry diamondDrill = new Entry(6_649, 4, "minecraft:diamond_pickaxe");
    public final Entry netheriteDrill = new Entry(93_750,  5, "minecraft:netherite_pickaxe");
    public final Entry shulkerDrill = new Entry(159_375, 7, "minecraft:netherite_pickaxe");
    public final Entry netherStarDrill = new Entry(796_875, 9, "minecraft:netherite_pickaxe");

    @AllArgsConstructor
    public static class Entry {
        public final int durability;
        public final int enchantability;
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
            return this.server.quarryDrillStats.copperDrill;
        }
        public QuarryDrillStatsConfig.Entry getIronDrill() {
            return this.server.quarryDrillStats.ironDrill;
        }
        public QuarryDrillStatsConfig.Entry getDiamondDrill() {
            return this.server.quarryDrillStats.diamondDrill;
        }
        public QuarryDrillStatsConfig.Entry getNetheriteDrill() {
            return this.server.quarryDrillStats.netheriteDrill;
        }
        public QuarryDrillStatsConfig.Entry getShulkerDrill() {
            return this.server.quarryDrillStats.shulkerDrill;
        }
        public QuarryDrillStatsConfig.Entry getNetherStarDrill() {
            return this.server.quarryDrillStats.netherStarDrill;
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
