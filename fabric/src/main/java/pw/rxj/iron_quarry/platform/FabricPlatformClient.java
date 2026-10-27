package pw.rxj.iron_quarry.platform;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class FabricPlatformClient implements IPlatformClient {
    @Override
    public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void registerMenuScreen(MenuType<? extends M> menuType, MenuScreens.ScreenConstructor<M, U> screenConstructor) {
        MenuScreens.register(menuType, screenConstructor);
    }

    @Override
    public void registerReloadListener(Identifier id, PreparableReloadListener listener) {
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(id, listener);
    }

    @Override
    public void registerRangeSelectItemModelProperty(Identifier id, MapCodec<? extends RangeSelectItemModelProperty> codec) {
        RangeSelectItemModelProperties.ID_MAPPER.put(id, codec);
    }
}
