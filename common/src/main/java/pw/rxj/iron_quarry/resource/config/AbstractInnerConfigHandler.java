package pw.rxj.iron_quarry.resource.config;

public abstract class AbstractInnerConfigHandler {
    protected final ConfigHandler handler;
    protected final Config.Server server;
    protected final Config.Client client;

    protected AbstractInnerConfigHandler(ConfigHandler handler) {
        this.server = handler.getConfig().SERVER;
        this.client = handler.getConfig().CLIENT;
        this.handler = handler;
    }

    public void applyChanges() {}
}
