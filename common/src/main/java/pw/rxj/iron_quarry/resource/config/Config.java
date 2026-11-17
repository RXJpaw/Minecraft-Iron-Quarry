package pw.rxj.iron_quarry.resource.config;

import pw.rxj.iron_quarry.resource.config.client.BlueprintPreviewConfig;
import pw.rxj.iron_quarry.resource.config.client.MonitorOverlayConfig;
import pw.rxj.iron_quarry.resource.config.client.MonitorPreviewConfig;
import pw.rxj.iron_quarry.resource.config.client.QuarryEffectsConfig;
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

        @Comment("Particles and sounds of quarries.")
        public final QuarryEffectsConfig quarryEffects = new QuarryEffectsConfig();
        @Comment("HUD displayed when using a Quarry Monitor.")
        public final MonitorOverlayConfig monitorOverlay = new MonitorOverlayConfig();
        @Comment("Area outline and markers shown in the world while holding a Blueprint, a quarry with a Blueprint or a linked Quarry Monitor.")
        public final BlueprintPreviewConfig blueprintPreview = new BlueprintPreviewConfig();
        @Comment("Quarry drawn at its position while holding a linked Quarry Monitor, so you can find it from far away.")
        public final MonitorPreviewConfig monitorPreview = new MonitorPreviewConfig();
    }
    public static class Server {
        private Server() { }

        @Comment("Stats of each quarry tier.")
        public final QuarryStatsConfig quarries = new QuarryStatsConfig();

        @Comment("Dimensions quarries are allowed to mine in.")
        public final DimensionsConfig dimensions = new DimensionsConfig();

        @Comment("Augments that improve quarries.")
        public final AugmentsConfig augments = new AugmentsConfig();

        @Comment("Smithing template that fills augments with augment points.")
        public final AugmentingTemplateConfig augmentingTemplate = new AugmentingTemplateConfig();

        @Comment("Expansion cards that change a quarry's abilities.")
        public final ExpansionsConfig expansions = new ExpansionsConfig();

        @Comment("Stats of each quarry monitor tier.")
        public final QuarryMonitorConfig monitors = new QuarryMonitorConfig();

        @Comment("Stats of each drill tier.")
        public final QuarryDrillStatsConfig drills = new QuarryDrillStatsConfig();
    }
}
