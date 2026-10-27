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
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class NeoForgePlatformClient implements IPlatformClient {
    private static final List<Consumer<RegisterMenuScreensEvent>> MENU_SCREENS = new ArrayList<>();
    private static final List<Consumer<AddClientReloadListenersEvent>> RELOAD_LISTENERS = new ArrayList<>();
    private static final List<Consumer<RegisterRangeSelectItemModelPropertyEvent>> RANGE_SELECT_PROPERTIES = new ArrayList<>();

    @Override
    public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void registerMenuScreen(MenuType<? extends M> menuType, MenuScreens.ScreenConstructor<M, U> screenConstructor) {
        MENU_SCREENS.add(event -> event.register(menuType, screenConstructor));
    }

    @Override
    public void registerReloadListener(Identifier id, PreparableReloadListener listener) {
        RELOAD_LISTENERS.add(event -> event.addListener(id, listener));
    }

    @Override
    public void registerRangeSelectItemModelProperty(Identifier id, MapCodec<? extends RangeSelectItemModelProperty> codec) {
        RANGE_SELECT_PROPERTIES.add(event -> event.register(id, codec));
    }

    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        MENU_SCREENS.forEach(menuScreen -> menuScreen.accept(event));
    }
    public static void registerReloadListeners(AddClientReloadListenersEvent event) {
        RELOAD_LISTENERS.forEach(reloadListener -> reloadListener.accept(event));
    }
    public static void registerRangeSelectItemModelProperties(RegisterRangeSelectItemModelPropertyEvent event) {
        RANGE_SELECT_PROPERTIES.forEach(property -> property.accept(event));
    }
}
