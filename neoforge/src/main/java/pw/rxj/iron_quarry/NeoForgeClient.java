package pw.rxj.iron_quarry;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import pw.rxj.iron_quarry.platform.NeoForgeEventBridge;
import pw.rxj.iron_quarry.platform.NeoForgePlatformClient;
import pw.rxj.iron_quarry.platform.NeoForgePlatformNetwork;
import pw.rxj.iron_quarry.platform.NeoForgePlatformRecipe;

@Mod(value = Main.MOD_ID, dist = Dist.CLIENT)
public class NeoForgeClient {
    public NeoForgeClient(IEventBus modEventBus) {
        modEventBus.addListener(NeoForgePlatformClient::registerMenuScreens);
        modEventBus.addListener(NeoForgePlatformClient::registerReloadListeners);
        modEventBus.addListener(NeoForgePlatformClient::registerRangeSelectItemModelProperties);
        modEventBus.addListener(NeoForgePlatformNetwork::registerClient);
        NeoForge.EVENT_BUS.addListener(NeoForgePlatformRecipe::receiveRecipes);
        NeoForgeEventBridge.registerClient();

        Client.init();
    }
}
