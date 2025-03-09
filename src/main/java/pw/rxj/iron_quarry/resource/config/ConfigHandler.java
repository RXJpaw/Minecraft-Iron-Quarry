package pw.rxj.iron_quarry.resource.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.network.PacketServerConfigApply;
import pw.rxj.iron_quarry.network.ZNetwork;
import pw.rxj.iron_quarry.resource.config.client.BlockBreakingConfig;
import pw.rxj.iron_quarry.resource.config.client.QuarryMonitorOverlayConfig;
import pw.rxj.iron_quarry.resource.config.server.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;

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

    protected Config getConfig() {
        return this.config;
    }

    //Client
    public BlockBreakingConfig.Handler getBlockBreakingConfig() {
        return BlockBreakingConfig.Handler.of(this);
    }
    public QuarryMonitorOverlayConfig.Handler getQuarryMonitorOverlayConfig() {
        return QuarryMonitorOverlayConfig.Handler.of(this);
    }
    //Server
    public QuarryStatsConfig.Handler getQuarryStatsConfig() {
        return QuarryStatsConfig.Handler.of(this);
    }
    public AugmentStatsConfig.Handler getAugmentStatsConfig() {
        return AugmentStatsConfig.Handler.of(this);
    }
    public QuarryMonitorConfig.Handler getQuarryMonitorConfig() {
        return QuarryMonitorConfig.Handler.of(this);
    }
    public QuarryDrillStatsConfig.Handler getQuarryDrillStatsConfig() {
        return QuarryDrillStatsConfig.Handler.of(this);
    }
    public SilkTouchAugmentConfig.Handler getSilkTouchAugmentConfig() {
        return SilkTouchAugmentConfig.Handler.of(this);
    }
    public ChestLootingAugmentConfig.Handler getChestLootingAugmentConfig() {
        return ChestLootingAugmentConfig.Handler.of(this);
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

    public void applyServerChanges() {
        this.getQuarryStatsConfig().applyChanges();
        this.getAugmentStatsConfig().applyChanges();
        this.getQuarryMonitorConfig().applyChanges();
        this.getQuarryDrillStatsConfig().applyChanges();
    }
    public void registerServer() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            this.read(EnvType.SERVER);
            this.applyServerChanges();
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ZNetwork.sendToPlayer(handler.player, PacketServerConfigApply.bake(this));
            Main.LOGGER.info("Sent server config to: {} ({})", handler.player.getName(), handler.player.getUuid());
        });
    }
    @Environment(EnvType.CLIENT)
    public void registerClient() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            this.read(EnvType.SERVER);
            this.applyServerChanges();
        });
    }
}
