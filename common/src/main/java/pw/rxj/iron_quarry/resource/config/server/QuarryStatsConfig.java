package pw.rxj.iron_quarry.resource.config.server;

import lombok.AllArgsConstructor;
import pw.rxj.iron_quarry.resource.config.AbstractInnerConfigHandler;
import pw.rxj.iron_quarry.resource.config.ConfigHandler;
import pw.rxj.iron_quarry.resource.config.util.Comment;
import pw.rxj.iron_quarry.resource.config.util.Range;

public class QuarryStatsConfig {
    public final Entry copperQuarry = new Entry(0, 40, 20_000, 20);           //       74 RF/t * 269t |   800 RF ~ 1-2 Coal Generators
    public final Entry ironQuarry = new Entry(1, 30, 100_000, 40);            //    1.949 RF/t *  51t | 1.200 RF ~ 10 Advanced Solar Panels (day/night average)
    public final Entry goldQuarry = new Entry(2, 20, 500_000, 90);            //   13.152 RF/t *  38t | 1.800 RF ~ 10 Industrial Solar Panels (day/night average)
    public final Entry diamondQuarry = new Entry(3, 10, 2_500_000, 270);      //   75.907 RF/t *  33t | 2.700 RF ~ 14 Ultimate Solar Panels (day/night average)
    public final Entry netheriteQuarry = new Entry(4, 5, 10_000_000, 810);    //  337.072 RF/t *  30t | 4.050 RF ~  5 Quantum Solar Panels (day/night average)
    public final Entry netherStarQuarry = new Entry(6, 2, 50_000_000, 3_040); //2.085.862 RF/t *  24t | 6.080 RF ~ 16 Quantum Solar Panels (day/night average)

    @AllArgsConstructor
    public static class Entry {
        @Comment("Usable augment slots.")
        @Range(min = 0, max = 6)
        public final int augmentLimit;
        @Comment("Ticks required to mine a block, before augments.")
        @Range(min = 1)
        public final int ticksPerOperation;
        @Comment("Energy buffer in RF.")
        @Range(min = 1)
        public final int energyCapacity;
        @Comment("RF/t while mining, before augments. One block costs baseConsumption * ticksPerOperation.")
        @Range(min = 0)
        public final int baseConsumption;
    }

    public static class Handler extends AbstractInnerConfigHandler {
        private Handler(ConfigHandler handler) {
            super(handler);
        }
        public static Handler of(ConfigHandler handler) {
            return new Handler(handler);
        }

        public QuarryStatsConfig.Entry getCopperQuarry() {
            return this.getServer().quarryStats.copperQuarry;
        }
        public QuarryStatsConfig.Entry getIronQuarry() {
            return this.getServer().quarryStats.ironQuarry;
        }
        public QuarryStatsConfig.Entry getGoldQuarry() {
            return this.getServer().quarryStats.goldQuarry;
        }
        public QuarryStatsConfig.Entry getDiamondQuarry() {
            return this.getServer().quarryStats.diamondQuarry;
        }
        public QuarryStatsConfig.Entry getNetheriteQuarry() {
            return this.getServer().quarryStats.netheriteQuarry;
        }
        public QuarryStatsConfig.Entry getNetherStarQuarry() {
            return this.getServer().quarryStats.netherStarQuarry;
        }
    }
}
