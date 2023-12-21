package pw.rxj.iron_quarry.resource;

import pw.rxj.iron_quarry.types.AugmentType;

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
    }
    protected void overrideServer(Config.Server newServerConfig){
        this.SERVER.silkTouchAugment = newServerConfig.silkTouchAugment;
        this.SERVER.quarryStats = newServerConfig.quarryStats;
        this.SERVER.augmentStats = newServerConfig.augmentStats;
    }
    @Override
    public int hashCode() {
        return Objects.hash(CLIENT, SERVER);
    }

    public static class Client {
        private Client() { }

        public static class BlockBreakingConfig {
            public float volume = 1.0F;
            public float distance = 64.0F;

            @Override
            public int hashCode() {
                return Objects.hash(volume, distance);
            }
        }
        public BlockBreakingConfig blockBreaking = new BlockBreakingConfig();

        @Override
        public int hashCode() {
            return Objects.hash(blockBreaking);
        }
    }
    public static class Server {
        private Server() { }

        public static class SilkTouchAugmentConfig {
            public final String villagerProfession = "minecraft:toolsmith";
            public final byte villagerLevel = 3;
            public final boolean wanderingVillager = true;
            public final byte wanderingVillagerLevel = 2;

            @Override
            public int hashCode() {
                return Objects.hash(villagerProfession, villagerLevel, wanderingVillager, wanderingVillagerLevel);
            }
        }
        public SilkTouchAugmentConfig silkTouchAugment = new SilkTouchAugmentConfig();

        public static class QuarryStatsConfig {
            public final Entry copperQuarry = new Entry(0, 40, 6_000, 16); //640 RF ~ 50% Coal Generator
            public final Entry ironQuarry = new Entry(1, 30, 40_000, 48); //1.440 RF ~ 12 Advanced Solar Panels (day/night average)
            public final Entry goldQuarry = new Entry(2, 20, 260_000, 160); //3.200 RF ~ 16 Industrial Solar Panels (day/night average)
            public final Entry diamondQuarry = new Entry(3, 10, 1_500_000, 640); //6.400 RF ~ 32 Ultimate Solar Panels (day/night average)
            public final Entry netheriteQuarry = new Entry(4, 5, 7_000_000, 2_500); //12.500 RF ~ Nitro Reactor with Packed Ice
            public final Entry netherStarQuarry = new Entry(6, 2, 50_000_000, 12_300); //24.600 RF ~ Nitro Reactor with Blue Ice

            @SuppressWarnings({"Class can be a record"})
            public static class Entry {
                public final int augmentLimit;
                public final int ticksPerOperation;
                public final int energyCapacity;
                public final int baseConsumption;

                public Entry(int augmentLimit, int ticksPerOperation, int energyCapacity, int baseConsumption) {
                    this.augmentLimit = augmentLimit;
                    this.ticksPerOperation = ticksPerOperation;
                    this.energyCapacity = energyCapacity;
                    this.baseConsumption = baseConsumption;
                }

                @Override
                public int hashCode() {
                    return Objects.hash(augmentLimit, ticksPerOperation, energyCapacity, baseConsumption);
                }
            }

            @Override
            public int hashCode() {
                return Objects.hash(copperQuarry, ironQuarry, goldQuarry, diamondQuarry, netheriteQuarry, netherStarQuarry);
            }
        }
        public QuarryStatsConfig quarryStats = new QuarryStatsConfig();

        public static class AugmentStatsConfig {
            public final Entry speed = new Entry(AugmentType.SPEED);
            public final Entry fortune = new Entry(AugmentType.FORTUNE);
            public final Entry silkTouch = new Entry(AugmentType.SILK_TOUCH);

            public static class Entry {
                public final int baseAmount;
                public final float multiplier;
                public final float inefficiency;

                public Entry(AugmentType augmentType) {
                    this.baseAmount = augmentType.getBaseAmount();
                    this.multiplier = augmentType.getMultiplier();
                    this.inefficiency = augmentType.getInefficiency();
                }

                @Override
                public int hashCode() {
                    return Objects.hash(baseAmount, multiplier, inefficiency);
                }
            }

            @Override
            public int hashCode() {
                return Objects.hash(speed, fortune, silkTouch);
            }
        }
        public AugmentStatsConfig augmentStats = new AugmentStatsConfig();

        @Override
        public int hashCode() {
            return Objects.hash(silkTouchAugment, quarryStats, augmentStats);
        }
    }
}
