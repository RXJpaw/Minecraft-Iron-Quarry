package pw.rxj.iron_quarry;

import net.fabricmc.api.ModInitializer;
import pw.rxj.iron_quarry.platform.FabricEventBridge;

public class FabricMain implements ModInitializer {
	@Override
	public void onInitialize() {
		FabricEventBridge.register();

		Main.init();
	}
}
