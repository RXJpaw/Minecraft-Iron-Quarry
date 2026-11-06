package pw.rxj.iron_quarry.resource.config.server;

import lombok.AllArgsConstructor;
import pw.rxj.iron_quarry.resource.config.AbstractInnerConfigHandler;
import pw.rxj.iron_quarry.resource.config.ConfigHandler;

public class QuarryMonitorConfig {
    public final Entry ironMonitor = new Entry(1_024, false);
    public final Entry diamondMonitor = new Entry(4_096, false);
    public final Entry netheriteMonitor = new Entry(16_384, true);
    public final Entry sculkMonitor = new Entry(65_536, true);

    @AllArgsConstructor
    public static class Entry {
        public final int range;
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
            return this.server.quarryMonitor.ironMonitor;
        }
        public QuarryMonitorConfig.Entry getDiamondMonitor() {
            return this.server.quarryMonitor.diamondMonitor;
        }
        public QuarryMonitorConfig.Entry getNetheriteMonitor() {
            return this.server.quarryMonitor.netheriteMonitor;
        }
        public QuarryMonitorConfig.Entry getSculkMonitor() {
            return this.server.quarryMonitor.sculkMonitor;
        }
    }
}
