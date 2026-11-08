package pw.rxj.iron_quarry.resource.config.server;

import lombok.AllArgsConstructor;
import pw.rxj.iron_quarry.resource.config.AbstractInnerConfigHandler;
import pw.rxj.iron_quarry.resource.config.ConfigHandler;
import pw.rxj.iron_quarry.resource.config.util.Comment;
import pw.rxj.iron_quarry.resource.config.util.Range;

public class AugmentsConfig {
    @Comment("Speed Augment: Mines faster, costs more energy.")
    public final Speed speed = new Speed();
    @Comment("Fortune Augment: More drops like the Fortune enchantment, costs more energy.")
    public final Fortune fortune = new Fortune();
    @Comment("Silk Touch Augment: Mines blocks as they are, but slower.")
    public final SilkTouch silkTouch = new SilkTouch();
    @Comment("Chest Looting Augment: Lets quarries empty containers instead of skipping them.")
    public final ChestLooting chestLooting = new ChestLooting();

    public static class Speed {
        @Comment("Turns the augment off: It has no effect, is hidden from the creative inventory and can't be filled at the smithing table.")
        public final boolean disabled;
        @Comment("Augment points of an augment without capacity upgrades. Each capacity upgrade raises this.")
        @Range(min = 1)
        public final int baseAmount;
        @Comment("Speed factor added by an augment with all capacity upgrades that is completely filled.")
        @Range(min = 0)
        public final float maxSpeedBonus;
        @Comment("Speed energy factor added by an augment with all capacity upgrades that is completely filled.")
        @Range(min = 0)
        public final float maxEnergyBonus;
        @Comment("Shape of the energy cost curve. Above 0, partly filled augments cost less energy per speed than full ones; the higher, the less.")
        @Range(min = 0)
        public final float energyRatioExponent;
        @Comment("Highest total speed factor. Speed above it has no effect and costs no energy.\nDefault = 1 + 6 * maxSpeedBonus + Efficiency V (1.3^5 - 1), i.e. a fully upgraded nether star quarry.")
        @Range(min = 1)
        public final float speedFactorLimit;

        public Speed() {
            this.disabled = false;
            this.baseAmount = 1_000;
            this.maxSpeedBonus = 15.0F;
            this.maxEnergyBonus = 45.0F;
            this.energyRatioExponent = 1.5F;
            this.speedFactorLimit = 93.71293F;
        }
    }
    public static class Fortune {
        @Comment("Turns the augment off: It has no effect, is hidden from the creative inventory and can't be filled at the smithing table.")
        public final boolean disabled;
        @Comment("Augment points of an augment without capacity upgrades. Each capacity upgrade raises this.")
        @Range(min = 1)
        public final int baseAmount;
        @Comment("Fortune level added by an augment with all capacity upgrades that is completely filled.\nFractions give a matching chance for one more level.")
        @Range(min = 0)
        public final float maxLevel;
        @Comment("Fortune energy factor added by an augment with all capacity upgrades that is completely filled.")
        @Range(min = 0)
        public final float maxEnergyBonus;
        @Comment("Highest total fortune level of a quarry.")
        @Range(min = 0)
        public final float levelLimit;

        public Fortune() {
            this.disabled = false;
            this.baseAmount = 1_500;
            this.maxLevel = 4.7F;
            this.maxEnergyBonus = 2.0F;
            this.levelLimit = 4.7F;
        }
    }
    public static class SilkTouch {
        @Comment("Turns the augment off: It has no effect, is hidden from the creative inventory and isn't sold by villagers (see tradingEnabled).")
        public final boolean disabled;
        @Comment("Divides the speed of the quarry.")
        @Range(min = 1)
        public final float slowness;
        @Comment("Lets toolsmiths and wandering traders sell the augment. Only checked when a villager's trades are generated, existing trades stay.\nThis only turns the default trade on or off. A data pack can change the price in the villager trade iron_quarry:silk_touch_augment, or which villagers sell it through the villager_trade tags minecraft:toolsmith/level_3 and minecraft:wandering_trader/uncommon.")
        public final boolean tradingEnabled;

        public SilkTouch() {
            this.disabled = false;
            this.slowness = 2.0F;
            this.tradingEnabled = true;
        }
    }
    public static class ChestLooting {
        @Comment("Turns the augment off: It has no effect, is hidden from the creative inventory and no longer appears in chest loot.")
        public final boolean disabled;
        @Comment("Loot tables that can contain the chest looting augment.")
        public final Entry[] lootPools = new Entry[]{
                Entry.common("minecraft:chests/abandoned_mineshaft"),
                Entry.common("minecraft:chests/desert_pyramid"),
                Entry.common("minecraft:chests/igloo_chest"),
                Entry.common("minecraft:chests/jungle_temple"),
                Entry.common("minecraft:chests/simple_dungeon"),
                Entry.common("minecraft:chests/shipwreck_treasure"),
                Entry.common("minecraft:chests/underwater_ruin_small"),
                Entry.common("minecraft:chests/village/village_weaponsmith"),
                Entry.common("minecraft:chests/village/village_armorer"),
                Entry.common("minecraft:chests/village/village_toolsmith"),

                Entry.uncommon("minecraft:chests/stronghold_corridor"),
                Entry.uncommon("minecraft:chests/stronghold_crossing"),
                Entry.uncommon("minecraft:chests/stronghold_library"),
                Entry.uncommon("minecraft:chests/underwater_ruin_big"),
                Entry.uncommon("minecraft:chests/pillager_outpost"),

                Entry.rare("minecraft:chests/woodland_mansion"),
                Entry.rare("minecraft:chests/ancient_city")
        };

        @AllArgsConstructor
        public static class Entry {
            @Comment("ID of the loot table.")
            public final String lootTable;
            @Comment("Least rolls of the pool.")
            @Range(min = 0)
            public final float rollsMin;
            @Comment("Most rolls of the pool.")
            @Range(min = 0)
            public final float rollsMax;
            @Comment("Chance that the pool is rolled at all.")
            @Range(min = 0, max = 1)
            public final float chance;

            public static Entry common(String lootTable) {
                return new Entry(lootTable, 1.0F, 1.0F, 0.05F);
            }
            public static Entry uncommon(String lootTable) {
                return new Entry(lootTable, 1.0F, 1.0F, 0.12F);
            }
            public static Entry rare(String lootTable) {
                return new Entry(lootTable, 1.0F, 1.0F, 0.26F);
            }
        }

        public ChestLooting() {
            this.disabled = false;
        }
    }

    public static class Handler extends AbstractInnerConfigHandler {
        private Handler(ConfigHandler handler) {
            super(handler);
        }
        public static Handler of(ConfigHandler handler) {
            return new Handler(handler);
        }

        public AugmentsConfig.Speed getSpeed() {
            return this.getServer().augments.speed;
        }
        public AugmentsConfig.Fortune getFortune() {
            return this.getServer().augments.fortune;
        }
        public AugmentsConfig.SilkTouch getSilkTouch() {
            return this.getServer().augments.silkTouch;
        }
        public AugmentsConfig.ChestLooting getChestLooting() {
            return this.getServer().augments.chestLooting;
        }
    }
}
