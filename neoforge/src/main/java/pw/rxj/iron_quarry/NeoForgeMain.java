package pw.rxj.iron_quarry;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import pw.rxj.iron_quarry.platform.NeoForgeEventBridge;
import pw.rxj.iron_quarry.platform.NeoForgePlatformEnergy;
import pw.rxj.iron_quarry.platform.NeoForgePlatformItemTransfer;
import pw.rxj.iron_quarry.platform.NeoForgePlatformNetwork;
import pw.rxj.iron_quarry.platform.NeoForgePlatformRecipe;
import pw.rxj.iron_quarry.platform.NeoForgePlatformRegistry;

@Mod(Main.MOD_ID)
public class NeoForgeMain {
    public NeoForgeMain(IEventBus modEventBus) {
        modEventBus.addListener(NeoForgePlatformRegistry::registerPendingEntries);
        modEventBus.addListener(NeoForgePlatformNetwork::register);
        modEventBus.addListener(NeoForgePlatformEnergy::registerCapabilities);
        modEventBus.addListener(NeoForgePlatformItemTransfer::registerCapabilities);
        NeoForge.EVENT_BUS.addListener(NeoForgePlatformRecipe::sendRecipes);
        NeoForgeEventBridge.register();

        Main.init();
    }
}
