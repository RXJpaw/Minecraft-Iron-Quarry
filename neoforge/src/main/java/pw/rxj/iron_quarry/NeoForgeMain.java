package pw.rxj.iron_quarry;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import pw.rxj.iron_quarry.platform.NeoForgeEventBridge;
import pw.rxj.iron_quarry.platform.NeoForgePlatformNetwork;
import pw.rxj.iron_quarry.platform.NeoForgePlatformRegistry;

@Mod(Main.MOD_ID)
public class NeoForgeMain {
    public NeoForgeMain(IEventBus modEventBus) {
        modEventBus.addListener(NeoForgePlatformRegistry::registerPendingEntries);
        modEventBus.addListener(NeoForgePlatformNetwork::register);
        NeoForgeEventBridge.register();

        Main.init();
    }
}
