package pw.rxj.iron_quarry.resource.config;

import pw.rxj.iron_quarry.resource.config.client.BlockBreakingConfig;
import pw.rxj.iron_quarry.resource.config.client.QuarryMonitorOverlayConfig;
import pw.rxj.iron_quarry.resource.config.server.*;

public class Config {
    public final Config.Client CLIENT = new Config.Client();
    public final Config.Server SERVER = new Config.Server();

    private Config() { }
    protected static Config empty() {
        return new Config();
    }

    protected void overrideClient(Config.Client newClientConfig) {
        this.CLIENT.blockBreaking = newClientConfig.blockBreaking;
        this.CLIENT.quarryMonitorOverlay = newClientConfig.quarryMonitorOverlay;
    }
    protected void overrideServer(Config.Server newServerConfig) {
        this.SERVER.quarryStats = newServerConfig.quarryStats;
        this.SERVER.augments = newServerConfig.augments;
        this.SERVER.augmentingTemplate = newServerConfig.augmentingTemplate;
        this.SERVER.quarryMonitor = newServerConfig.quarryMonitor;
        this.SERVER.quarryDrillStats = newServerConfig.quarryDrillStats;
    }

    public static class Client {
        private Client() { }

        public BlockBreakingConfig blockBreaking = new BlockBreakingConfig();
        public QuarryMonitorOverlayConfig quarryMonitorOverlay = new QuarryMonitorOverlayConfig();
    }
    public static class Server {
        private Server() { }

        public QuarryStatsConfig quarryStats = new QuarryStatsConfig();

        public AugmentsConfig augments = new AugmentsConfig();

        public AugmentingTemplateConfig augmentingTemplate = new AugmentingTemplateConfig();

        public QuarryMonitorConfig quarryMonitor = new QuarryMonitorConfig();

        public QuarryDrillStatsConfig quarryDrillStats = new QuarryDrillStatsConfig();
    }
}
