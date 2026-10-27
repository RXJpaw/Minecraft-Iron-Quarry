package pw.rxj.iron_quarry;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import pw.rxj.iron_quarry.model.ZModels;
import pw.rxj.iron_quarry.platform.FabricEventBridge;

@Environment(EnvType.CLIENT)
public class FabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricEventBridge.registerClient();

        Client.init();
        ZModels.register();
    }
}
