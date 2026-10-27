package pw.rxj.iron_quarry.compat;

import pw.rxj.iron_quarry.platform.Platform;

public class Compat {
    public static boolean REI_LOADED = false;
    public static boolean JEI_LOADED = false;
    public static boolean IRIS_LOADED = false;

    public static void init() {
        if(Platform.LOADER.isModLoaded("roughlyenoughitems")) REI_LOADED = true;
        if(Platform.LOADER.isModLoaded("jei")) JEI_LOADED = true;
        if(Platform.LOADER.isModLoaded("iris")) IRIS_LOADED = true;
    }
}
