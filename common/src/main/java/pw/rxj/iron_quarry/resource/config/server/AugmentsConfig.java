package pw.rxj.iron_quarry.resource.config.server;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import pw.rxj.iron_quarry.resource.config.AbstractInnerConfigHandler;
import pw.rxj.iron_quarry.resource.config.ConfigHandler;

@EqualsAndHashCode
public class AugmentsConfig {
    public final Speed speed = new Speed();
    public final Fortune fortune = new Fortune();
    public final SilkTouch silkTouch = new SilkTouch();
    public final ChestLooting chestLooting = new ChestLooting();

    @EqualsAndHashCode
    public static class Speed {
        public final boolean disabled;
        public final int baseAmount;
        public final int maxSpeedBonus;
        public final int maxEnergyBonus;
        public final float speedBonusLimit;

        public Speed() {
            this.disabled = false;
            this.baseAmount = 1_000;
            this.maxSpeedBonus = 1_500;
            this.maxEnergyBonus = 2_500;
            this.speedBonusLimit = 9_271.293F;
        }
    }
    @EqualsAndHashCode
    public static class Fortune {
        public final boolean disabled;
        public final int baseAmount;
        public final float maxLevel;
        public final int maxEnergyBonus;
        public final float levelLimit;

        public Fortune() {
            this.disabled = false;
            this.baseAmount = 1_500;
            this.maxLevel = 4.7F;
            this.maxEnergyBonus = 200;
            this.levelLimit = 4.7F;
        }
    }
    @EqualsAndHashCode
    public static class SilkTouch {
        public final boolean disabled;
        public final float slowness;
        public final boolean tradingEnabled;

        public SilkTouch() {
            this.disabled = false;
            this.slowness = 2.0F;
            this.tradingEnabled = true;
        }
    }
    @EqualsAndHashCode
    public static class ChestLooting {
        public final boolean disabled;
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

        @EqualsAndHashCode
        @AllArgsConstructor
        public static class Entry {
            public final String lootTable;
            public final float rollsMin;
            public final float rollsMax;
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
            return this.server.augments.speed;
        }
        public AugmentsConfig.Fortune getFortune() {
            return this.server.augments.fortune;
        }
        public AugmentsConfig.SilkTouch getSilkTouch() {
            return this.server.augments.silkTouch;
        }
        public AugmentsConfig.ChestLooting getChestLooting() {
            return this.server.augments.chestLooting;
        }
    }
}
