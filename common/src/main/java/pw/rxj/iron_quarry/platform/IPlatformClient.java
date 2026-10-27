package pw.rxj.iron_quarry.platform;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public interface IPlatformClient {
    <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void registerMenuScreen(MenuType<? extends M> menuType, MenuScreens.ScreenConstructor<M, U> screenConstructor);
    void registerReloadListener(Identifier id, PreparableReloadListener listener);
    void registerRangeSelectItemModelProperty(Identifier id, MapCodec<? extends RangeSelectItemModelProperty> codec);
}
