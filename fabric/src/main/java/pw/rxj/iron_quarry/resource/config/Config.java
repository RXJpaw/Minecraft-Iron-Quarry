package pw.rxj.iron_quarry.resource.config;

import lombok.EqualsAndHashCode;
import pw.rxj.iron_quarry.resource.config.client.BlockBreakingConfig;
import pw.rxj.iron_quarry.resource.config.client.QuarryMonitorOverlayConfig;
import pw.rxj.iron_quarry.resource.config.server.*;

import java.util.Objects;

public class Config {
    public final Config.Client CLIENT = new Config.Client();
    public final Config.Server SERVER = new Config.Server();

    private Config() { }
    protected static Config empty() {
        return new Config();
    }

    protected void override(Config newConfig) {
        this.overrideClient(newConfig.CLIENT);
        this.overrideServer(newConfig.SERVER);
    }
    protected void overrideClient(Config.Client newClientConfig){
        this.CLIENT.blockBreaking = newClientConfig.blockBreaking;
        this.CLIENT.quarryMonitorOverlay = newClientConfig.quarryMonitorOverlay;
    }
    protected void overrideServer(Config.Server newServerConfig){
        this.SERVER.quarryStats = newServerConfig.quarryStats;
        this.SERVER.augmentStats = newServerConfig.augmentStats;
        this.SERVER.quarryMonitor = newServerConfig.quarryMonitor;
        this.SERVER.quarryDrillStats = newServerConfig.quarryDrillStats;
        this.SERVER.silkTouchAugment = newServerConfig.silkTouchAugment;
        this.SERVER.chestLootingAugmentConfig = newServerConfig.chestLootingAugmentConfig;
    }
    @Override
    public int hashCode() {
        return Objects.hash(CLIENT, SERVER);
    }

    @EqualsAndHashCode
    public static class Client {
        private Client() { }

        public BlockBreakingConfig blockBreaking = new BlockBreakingConfig();
        public QuarryMonitorOverlayConfig quarryMonitorOverlay = new QuarryMonitorOverlayConfig();
    }
    @EqualsAndHashCode
    public static class Server {
        private Server() { }

        public QuarryStatsConfig quarryStats = new QuarryStatsConfig();

        public AugmentStatsConfig augmentStats = new AugmentStatsConfig();

        public QuarryMonitorConfig quarryMonitor = new QuarryMonitorConfig();

        public QuarryDrillStatsConfig quarryDrillStats = new QuarryDrillStatsConfig();

        public SilkTouchAugmentConfig silkTouchAugment = new SilkTouchAugmentConfig();

        public ChestLootingAugmentConfig chestLootingAugmentConfig = new ChestLootingAugmentConfig();
    }

}
