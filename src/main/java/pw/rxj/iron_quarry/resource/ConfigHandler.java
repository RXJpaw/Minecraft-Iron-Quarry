package pw.rxj.iron_quarry.resource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.VillagerProfession;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.block.QuarryBlock;
import pw.rxj.iron_quarry.block.ZBlocks;
import pw.rxj.iron_quarry.network.PacketServerConfigApply;
import pw.rxj.iron_quarry.network.ZNetwork;
import pw.rxj.iron_quarry.types.AugmentType;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class ConfigHandler {
    private final Path clientConfigPath;
    private final Path serverConfigPath;
    private final Config config;
    private final Gson gson;
    private int clientConfigHash = 0;
    private int serverConfigHash = 0;

    private ConfigHandler(Path clientPath, Path serverPath, Gson gson) {
        this.clientConfigPath = clientPath;
        this.serverConfigPath = serverPath;
        this.config = Config.empty();
        this.gson = gson;
    }
    public static ConfigHandler of(Path clientPath, Path serverPath, Gson gson) {
        return new ConfigHandler(clientPath, serverPath, gson);
    }
    public static ConfigHandler bake(Path configDir) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Path clientPath = configDir.resolve("client.json");
        Path serverPath = configDir.resolve("server.json");

        return of(clientPath, serverPath, gson);
    }
    public ConfigHandler copy() {
        return ConfigHandler.of(this.clientConfigPath, this.serverConfigPath, this.gson);
    }


    public class BlockBreakingConfigHandler {
        private final Config.Client clientConfig = ConfigHandler.this.config.CLIENT;
        private BlockBreakingConfigHandler() { }

        public void setVolume(float volume) {
            this.clientConfig.blockBreaking.volume = volume;
        }
        public float getVolume() {
            return this.clientConfig.blockBreaking.volume;
        }
        public void setOptionVolume(int volume) {
            this.setVolume(volume / 100.0F);
        }
        public int getOptionVolume() {
            return (int) (this.getVolume() * 100);
        }

        public void setDistance(float distance) {
            this.clientConfig.blockBreaking.distance = distance;
        }
        public float getDistance() {
            return this.clientConfig.blockBreaking.distance;
        }
        public void setOptionDistance(int distance) {
            this.setDistance(distance);
        }
        public int getOptionDistance() {
            return (int) (this.getDistance());
        }
    }
    public BlockBreakingConfigHandler getBlockBreakingConfig() {
        return new BlockBreakingConfigHandler();
    }

    public class SilkTouchAugmentConfigHandler {
        private final Config.Server serverConfig = ConfigHandler.this.config.SERVER;
        private SilkTouchAugmentConfigHandler() { }

        public Optional<VillagerProfession> getVillagerProfession() {
            Identifier villagerProfession = Identifier.tryParse(this.serverConfig.silkTouchAugment.villagerProfession);

            return Registry.VILLAGER_PROFESSION.getOrEmpty(villagerProfession);
        }
        public byte getVillagerLevel() {
            return this.serverConfig.silkTouchAugment.villagerLevel;
        }
        public boolean isWanderingVillagerEnabled() {
            return this.serverConfig.silkTouchAugment.wanderingVillager;
        }
        public byte getWanderingVillagerLevel() {
            return this.serverConfig.silkTouchAugment.wanderingVillagerLevel;
        }
    }
    public SilkTouchAugmentConfigHandler getSilkTouchAugmentConfig() {
        return new SilkTouchAugmentConfigHandler();
    }

    public class QuarryStatsConfigHandler {
        private final Config.Server serverConfig = ConfigHandler.this.config.SERVER;
        private QuarryStatsConfigHandler() { }

        public Config.Server.QuarryStatsConfig.Entry getCopperQuarry() {
            return serverConfig.quarryStats.copperQuarry;
        }
        public Config.Server.QuarryStatsConfig.Entry getIronQuarry() {
            return serverConfig.quarryStats.ironQuarry;
        }
        public Config.Server.QuarryStatsConfig.Entry getGoldQuarry() {
            return serverConfig.quarryStats.goldQuarry;
        }
        public Config.Server.QuarryStatsConfig.Entry getDiamondQuarry() {
            return serverConfig.quarryStats.diamondQuarry;
        }
        public Config.Server.QuarryStatsConfig.Entry getNetheriteQuarry() {
            return serverConfig.quarryStats.netheriteQuarry;
        }
        public Config.Server.QuarryStatsConfig.Entry getNetherStarQuarry() {
            return serverConfig.quarryStats.netherStarQuarry;
        }

        public void applyChanges() {
            ((QuarryBlock) ZBlocks.COPPER_QUARRY.getBlock()).override(this.getCopperQuarry());
            ((QuarryBlock) ZBlocks.IRON_QUARRY.getBlock()).override(this.getIronQuarry());
            ((QuarryBlock) ZBlocks.GOLD_QUARRY.getBlock()).override(this.getGoldQuarry());
            ((QuarryBlock) ZBlocks.DIAMOND_QUARRY.getBlock()).override(this.getDiamondQuarry());
            ((QuarryBlock) ZBlocks.NETHERITE_QUARRY.getBlock()).override(this.getNetheriteQuarry());
            ((QuarryBlock) ZBlocks.NETHER_STAR_QUARRY.getBlock()).override(this.getNetherStarQuarry());
        }
    }
    public QuarryStatsConfigHandler getQuarryStatsConfig() {
        return new QuarryStatsConfigHandler();
    }

    public class AugmentStatsConfigHandler {
        private final Config.Server serverConfig = ConfigHandler.this.config.SERVER;
        private AugmentStatsConfigHandler() { }

        public Config.Server.AugmentStatsConfig.Entry getSpeed() {
            return serverConfig.augmentStats.speed;
        }
        public Config.Server.AugmentStatsConfig.Entry getFortune() {
            return serverConfig.augmentStats.fortune;
        }
        public Config.Server.AugmentStatsConfig.Entry getSilkTouch() {
            return serverConfig.augmentStats.silkTouch;
        }

        public void applyChanges() {
            AugmentType.SPEED.override(this.getSpeed());
            AugmentType.FORTUNE.override(this.getFortune());
            AugmentType.SILK_TOUCH.override(this.getSilkTouch());
        }
    }
    public AugmentStatsConfigHandler getAugmentStatsConfig() {
        return new AugmentStatsConfigHandler();
    }


    public void read(EnvType environment) {
        if(environment.equals(EnvType.CLIENT)) {
            if(!Files.exists(this.clientConfigPath)) {
                this.write(environment);

                Main.LOGGER.info("Client config created.");
                return;
            }

            try {
                BufferedReader reader = Files.newBufferedReader(this.clientConfigPath);
                this.config.overrideClient(this.gson.fromJson(reader, Config.Client.class));
                reader.close();

                Main.LOGGER.info("Client config read from disk.");

                //Make sure a complete version is saved on disk!
                this.write(environment);
            } catch (Exception e) {
                Main.LOGGER.error("ConfigHandler couldn't read client config file: ", e);

                this.config.overrideClient(Config.empty().CLIENT);
                this.write(environment);

                Main.LOGGER.warn("Client config reset to default.");
            }
        } else if(environment.equals(EnvType.SERVER)) {
            if(!Files.exists(this.serverConfigPath)) {
                this.write(environment);

                Main.LOGGER.info("Server config created.");
                return;
            }

            try {
                BufferedReader reader = Files.newBufferedReader(this.serverConfigPath);
                this.config.overrideServer(this.gson.fromJson(reader, Config.Server.class));
                reader.close();

                Main.LOGGER.info("Server config read from disk.");

                //Make sure a complete version is saved on disk!
                this.write(environment);
            } catch (Exception e) {
                Main.LOGGER.error("ConfigHandler couldn't read server config file: ", e);

                this.config.overrideServer(Config.empty().SERVER);
                this.write(environment);

                Main.LOGGER.warn("Server config reset to default.");
            }
        }
    }
    public void write(EnvType environment) {
        if(environment.equals(EnvType.CLIENT)) {
            if(this.clientConfigHash == this.config.CLIENT.hashCode()) return;
            if(this.clientConfigHash != 0) Main.LOGGER.info("Client config may have changed..");

            if(!Files.exists(this.clientConfigPath)) {
                try {
                    Files.createDirectories(this.clientConfigPath.getParent());
                    Files.createFile(this.clientConfigPath);
                } catch (Exception e) {
                    Main.LOGGER.error("ConfigHandler couldn't initialize client config file: ", e);
                    return;
                }
            }

            try {
                BufferedWriter writer = Files.newBufferedWriter(this.clientConfigPath);
                this.gson.toJson(this.config.CLIENT, writer);
                writer.close();

                this.clientConfigHash = this.config.CLIENT.hashCode();
                Main.LOGGER.info("Client config written to disk.");
            } catch (Exception e) {
                Main.LOGGER.error("ConfigHandler couldn't write to client config file: ", e);
            }
        } else if(environment.equals(EnvType.SERVER)) {
            if(this.serverConfigHash == this.config.SERVER.hashCode()) return;
            if(this.serverConfigHash != 0) Main.LOGGER.info("Server config may have changed..");

            if(!Files.exists(this.serverConfigPath)) {
                try {
                    Files.createDirectories(this.serverConfigPath.getParent());
                    Files.createFile(this.serverConfigPath);
                } catch (Exception e) {
                    Main.LOGGER.error("ConfigHandler couldn't initialize server config file: ", e);
                    return;
                }
            }

            try {
                BufferedWriter writer = Files.newBufferedWriter(this.serverConfigPath);
                this.gson.toJson(this.config.SERVER, writer);
                writer.close();

                this.serverConfigHash = this.config.SERVER.hashCode();
                Main.LOGGER.info("Server config written to disk.");
            } catch (Exception e) {
                Main.LOGGER.error("ConfigHandler couldn't write to server config file: ", e);
            }
        }
    }

    public byte[] asByteArray(EnvType environment) {
        Gson uglyGson = new GsonBuilder().create();

        if(environment.equals(EnvType.CLIENT)) {
            return uglyGson.toJson(this.config.CLIENT).getBytes();
        } else if(environment.equals(EnvType.SERVER)) {
            return uglyGson.toJson(this.config.SERVER).getBytes();
        } else {
            return new byte[]{};
        }
    }
    public void readByteArray(EnvType environment, byte[] bytes) {
        String json = new String(bytes);

        if(environment.equals(EnvType.CLIENT)) {
            this.config.overrideClient(this.gson.fromJson(json, Config.Client.class));
        } else if(environment.equals(EnvType.SERVER)) {
            this.config.overrideServer(this.gson.fromJson(json, Config.Server.class));
        }
    }

    public void registerServer() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            this.read(EnvType.SERVER);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ZNetwork.sendToPlayer(handler.player, PacketServerConfigApply.bake(this));
            Main.LOGGER.info("Sent server config to: {} ({})", handler.player.getEntityName(), handler.player.getUuid());
        });
    }
    @Environment(EnvType.CLIENT)
    public void registerClient() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            this.read(EnvType.SERVER);
        });
    }
}
