package pw.rxj.iron_quarry.resource.config;

public abstract class AbstractInnerConfigHandler {
    private final Config config;

    protected AbstractInnerConfigHandler(ConfigHandler handler) {
        this.config = handler.getConfig();
    }

    protected Config.Client getClient() {
        return this.config.getClient();
    }
    protected Config.Server getServer() {
        return this.config.getServer();
    }
}
