package pw.rxj.iron_quarry.resource.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.event.ClientConnectionCallback;
import pw.rxj.iron_quarry.event.ServerConnectionCallback;
import pw.rxj.iron_quarry.event.ServerLifecycleCallback;
import pw.rxj.iron_quarry.network.PacketServerConfigApply;
import pw.rxj.iron_quarry.network.ZNetwork;
import pw.rxj.iron_quarry.resource.config.client.BlockBreakingConfig;
import pw.rxj.iron_quarry.resource.config.client.QuarryMonitorOverlayConfig;
import pw.rxj.iron_quarry.resource.config.server.*;
import pw.rxj.iron_quarry.resource.config.util.JsoncReader;
import pw.rxj.iron_quarry.resource.config.util.JsoncWriter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

public class ConfigHandler {
    private static final String CLIENT_HEADER = "Iron Quarry: Client Config\nRead when the game starts. Edit it while the game is closed, the config screen overwrites it.";
    private static final String SERVER_HEADER = "Iron Quarry: Server Config\nRead when the server starts, then sent to joining players.";

    private final Path clientConfigPath;
    private final Path serverConfigPath;
    private final Config config;
    private final Gson gson;

    private ConfigHandler(Path clientPath, Path serverPath, Gson gson) {
        this.clientConfigPath = clientPath;
        this.serverConfigPath = serverPath;
        this.config = Config.empty();
        this.gson = gson;
    }
    private static ConfigHandler of(Path clientPath, Path serverPath, Gson gson) {
        return new ConfigHandler(clientPath, serverPath, gson);
    }
    public static ConfigHandler bake(Path configDir) {
        Gson gson = new GsonBuilder().setStrictness(Strictness.LENIENT).create();
        Path clientPath = configDir.resolve("client.jsonc");
        Path serverPath = configDir.resolve("server.jsonc");

        return of(clientPath, serverPath, gson);
    }

    protected Config getConfig() {
        return this.config;
    }

    //Client Configs
    public BlockBreakingConfig.Handler getBlockBreakingConfig() {
        return BlockBreakingConfig.Handler.of(this);
    }
    public QuarryMonitorOverlayConfig.Handler getQuarryMonitorOverlayConfig() {
        return QuarryMonitorOverlayConfig.Handler.of(this);
    }

    //Server Configs
    public QuarryStatsConfig.Handler getQuarryStatsConfig() {
        return QuarryStatsConfig.Handler.of(this);
    }
    public AugmentsConfig.Handler getAugmentsConfig() {
        return AugmentsConfig.Handler.of(this);
    }
    public AugmentingTemplateConfig.Handler getAugmentingTemplateConfig() {
        return AugmentingTemplateConfig.Handler.of(this);
    }
    public QuarryMonitorConfig.Handler getQuarryMonitorConfig() {
        return QuarryMonitorConfig.Handler.of(this);
    }
    public QuarryDrillStatsConfig.Handler getQuarryDrillStatsConfig() {
        return QuarryDrillStatsConfig.Handler.of(this);
    }

    //Reading
    public synchronized void read(ConfigSide side) {
        boolean safeToWrite = switch (side) {
            case CLIENT -> this.readFile(this.clientConfigPath, Config.Client.class, this.config.CLIENT, Config.empty().CLIENT, this.config::overrideClient);
            case SERVER -> this.readFile(this.serverConfigPath, Config.Server.class, this.config.SERVER, Config.empty().SERVER, this.config::overrideServer);
        };

        //Make sure a complete version is saved on disk!
        if(safeToWrite) this.write(side);
    }
    private <T> boolean readFile(Path path, Class<T> type, T current, T defaults, Consumer<T> apply) {
        if(!Files.exists(path)) {
            Main.LOGGER.info("Config file {} doesn't exist yet, creating it.", path);
            return true;
        }

        try {
            apply.accept(JsoncReader.read(this.gson, Files.readString(path), type, defaults));

            Main.LOGGER.info("Config file {} read.", path);
            return true;
        } catch (Exception e) {
            Main.LOGGER.error("Couldn't read config file {}, using {} until it is fixed: {}", path, this.describeValuesInUse(current, defaults), e.toString());
            return false;
        }
    }
    private String describeValuesInUse(Object current, Object defaults) {
        return this.gson.toJsonTree(current).equals(this.gson.toJsonTree(defaults)) ? "the default values" : "the previously read values";
    }

    //Writing
    public synchronized void write(ConfigSide side) {
        switch (side) {
            case CLIENT -> this.writeFile(this.clientConfigPath, CLIENT_HEADER, this.config.CLIENT, Config.empty().CLIENT);
            case SERVER -> this.writeFile(this.serverConfigPath, SERVER_HEADER, this.config.SERVER, Config.empty().SERVER);
        }
    }
    private void writeFile(Path path, String header, Object config, Object defaults) {
        String text = JsoncWriter.write(this.gson, header, config, defaults);
        if(isUpToDate(path, text)) return;

        Path temporaryPath = path.resolveSibling(path.getFileName() + ".tmp");

        try {
            Files.createDirectories(path.getParent());
            Files.writeString(temporaryPath, text);
            replaceFile(temporaryPath, path);

            Main.LOGGER.info("Config file {} written.", path);
        } catch (Exception e) {
            Main.LOGGER.error("Couldn't write config file {}: ", path, e);
        }
    }
    private static boolean isUpToDate(Path path, String text) {
        try {
            return Files.exists(path) && Files.readString(path).equals(text);
        } catch (IOException e) {
            return false;
        }
    }
    private static void replaceFile(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    //Server Sync
    public byte[] asServerByteArray() {
        return this.gson.toJson(this.config.SERVER).getBytes(StandardCharsets.UTF_8);
    }
    public synchronized void readServerByteArray(byte[] bytes) {
        String json = new String(bytes, StandardCharsets.UTF_8);

        this.config.overrideServer(JsoncReader.read(this.gson, json, Config.Server.class, Config.empty().SERVER));
    }
    private synchronized void discardReceivedServerConfig() {
        this.config.overrideServer(Config.empty().SERVER);
    }

    //Lifecycle
    public void applyServerChanges() {
        this.getQuarryStatsConfig().applyChanges();
        this.getAugmentingTemplateConfig().applyChanges();
        this.getQuarryMonitorConfig().applyChanges();
        this.getQuarryDrillStatsConfig().applyChanges();
    }
    public void registerServer() {
        ServerLifecycleCallback.STARTING.register(server -> {
            this.read(ConfigSide.SERVER);
            this.applyServerChanges();
        });

        ServerConnectionCallback.JOIN.register(player -> {
            ZNetwork.sendToPlayer(player, PacketServerConfigApply.bake(this));
            Main.LOGGER.info("Sent server config to: {} ({})", player.getGameProfile().name(), player.getUUID());
        });
    }
    @Environment(EnvType.CLIENT)
    public void registerClient() {
        ClientConnectionCallback.DISCONNECT.register(() -> {
            this.discardReceivedServerConfig();
            this.read(ConfigSide.SERVER);
            this.applyServerChanges();
        });
    }
}
