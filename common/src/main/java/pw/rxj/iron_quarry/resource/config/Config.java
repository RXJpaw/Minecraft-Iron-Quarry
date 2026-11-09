package pw.rxj.iron_quarry.resource.config;

import pw.rxj.iron_quarry.resource.config.client.BlockBreakingConfig;
import pw.rxj.iron_quarry.resource.config.client.QuarryMonitorOverlayConfig;
import pw.rxj.iron_quarry.resource.config.server.*;
import pw.rxj.iron_quarry.resource.config.util.Comment;

public class Config {
    //Only used on the client thread.
    private Config.Client client = new Config.Client();
    //Swapped by the integrated server on /reload while the client thread reads it.
    private volatile Config.Server server = new Config.Server();

    private Config() { }
    protected static Config empty() {
        return new Config();
    }

    protected Config.Client getClient() {
        return this.client;
    }
    protected void overrideClient(Config.Client newClientConfig) {
        this.client = newClientConfig;
    }
    protected Config.Server getServer() {
        return this.server;
    }
    protected void overrideServer(Config.Server newServerConfig) {
        this.server = newServerConfig;
    }

    public static class Client {
        private Client() { }

        @Comment("Particles & sounds of blocks mined by quarries.")
        public final BlockBreakingConfig blockBreaking = new BlockBreakingConfig();
        @Comment("HUD displayed when using a Quarry Monitor.")
        public final QuarryMonitorOverlayConfig quarryMonitorOverlay = new QuarryMonitorOverlayConfig();
    }
    public static class Server {
        private Server() { }

        @Comment("Stats of each quarry tier.")
        public final QuarryStatsConfig quarryStats = new QuarryStatsConfig();

        @Comment("Augments that improve quarries.")
        public final AugmentsConfig augments = new AugmentsConfig();

        @Comment("Smithing template that fills augments with augment points.")
        public final AugmentingTemplateConfig augmentingTemplate = new AugmentingTemplateConfig();

        @Comment("Expansion cards that change a quarry's abilities.")
        public final ExpansionsConfig expansions = new ExpansionsConfig();

        @Comment("Stats of each quarry monitor tier.")
        public final QuarryMonitorConfig quarryMonitor = new QuarryMonitorConfig();

        @Comment("Stats of each drill tier.")
        public final QuarryDrillStatsConfig quarryDrillStats = new QuarryDrillStatsConfig();
    }
}
