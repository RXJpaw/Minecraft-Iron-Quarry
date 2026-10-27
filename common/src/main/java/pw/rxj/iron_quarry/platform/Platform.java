package pw.rxj.iron_quarry.platform;

import java.util.ServiceLoader;

public class Platform {
    public static final IPlatformLoader LOADER = load(IPlatformLoader.class);
    public static final IPlatformRegistry REGISTRY = load(IPlatformRegistry.class);
    public static final IPlatformNetwork NETWORK = load(IPlatformNetwork.class);
    public static final IPlatformMenu MENU = load(IPlatformMenu.class);
    public static final IPlatformRecipe RECIPE = load(IPlatformRecipe.class);
    public static final IPlatformEnergy ENERGY = load(IPlatformEnergy.class);
    public static final IPlatformItemTransfer ITEM_TRANSFER = load(IPlatformItemTransfer.class);
    public static final IPlatformBlockBreak BLOCK_BREAK = load(IPlatformBlockBreak.class);
    public static final IPlatformClient CLIENT = LOADER.isDedicatedServer() ? null : load(IPlatformClient.class);

    private static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz, Platform.class.getClassLoader())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No platform implementation found for " + clazz.getName()));
    }
}
