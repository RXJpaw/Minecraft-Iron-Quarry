package pw.rxj.iron_quarry.compat.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import pw.rxj.iron_quarry.platform.FabricPlatformClient;

public class ModMenuClient implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return FabricPlatformClient::createConfigScreen;
    }
}
