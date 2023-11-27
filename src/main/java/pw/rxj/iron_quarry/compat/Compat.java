package pw.rxj.iron_quarry.compat;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class Compat implements ModInitializer {
    public static boolean REI_LOADED = false;

    @Override
    public void onInitialize() {
        FabricLoader fabricLoader = FabricLoader.getInstance();

        if(fabricLoader.isModLoaded("roughlyenoughitems")) REI_LOADED = true;
    }
}
