package pw.rxj.iron_quarry.resource.config;

public abstract class AbstractInnerConfigHandler {
    protected final Config.Server server;
    protected final Config.Client client;

    protected AbstractInnerConfigHandler(ConfigHandler handler) {
        this.server = handler.getConfig().SERVER;
        this.client = handler.getConfig().CLIENT;
    }

    public void applyChanges() {}
}
