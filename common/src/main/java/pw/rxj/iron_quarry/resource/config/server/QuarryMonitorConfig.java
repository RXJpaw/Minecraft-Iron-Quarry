package pw.rxj.iron_quarry.resource.config.server;

import lombok.AllArgsConstructor;
import pw.rxj.iron_quarry.resource.config.AbstractInnerConfigHandler;
import pw.rxj.iron_quarry.resource.config.ConfigHandler;
import pw.rxj.iron_quarry.resource.config.util.Comment;
import pw.rxj.iron_quarry.resource.config.util.Range;

public class QuarryMonitorConfig {
    public final Entry iron = new Entry(1_024, false);
    public final Entry diamond = new Entry(4_096, false);
    public final Entry netherite = new Entry(16_384, true);
    public final Entry sculk = new Entry(65_536, true);

    @AllArgsConstructor
    public static class Entry {
        @Comment("Horizontal distance in blocks up to which the monitor reaches its quarry.\nIn other dimensions, coordinates are scaled like portals (e.g. The Nether 1:8).")
        @Range(min = 0)
        public final int range;
        @Comment("Reaches quarries in other dimensions.")
        public final boolean interdimensional;
    }

    public static class Handler extends AbstractInnerConfigHandler {
        private Handler(ConfigHandler handler) {
            super(handler);
        }
        public static QuarryMonitorConfig.Handler of(ConfigHandler handler) {
            return new QuarryMonitorConfig.Handler(handler);
        }

        public QuarryMonitorConfig.Entry getIronMonitor() {
            return this.getServer().monitors.iron;
        }
        public QuarryMonitorConfig.Entry getDiamondMonitor() {
            return this.getServer().monitors.diamond;
        }
        public QuarryMonitorConfig.Entry getNetheriteMonitor() {
            return this.getServer().monitors.netherite;
        }
        public QuarryMonitorConfig.Entry getSculkMonitor() {
            return this.getServer().monitors.sculk;
        }
    }
}
