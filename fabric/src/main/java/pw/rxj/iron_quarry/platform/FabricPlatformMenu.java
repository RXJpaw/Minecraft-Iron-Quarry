package pw.rxj.iron_quarry.platform;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

public class FabricPlatformMenu implements IPlatformMenu {
    @Override
    public <T extends AbstractContainerMenu, D> MenuType<T> createMenuType(MenuFactory<T, D> factory, StreamCodec<? super RegistryFriendlyByteBuf, D> codec) {
        return new ExtendedMenuType<>(factory::create, codec);
    }

    @Override
    public <D> void openMenu(ServerPlayer player, MenuProvider provider, D data, StreamCodec<? super RegistryFriendlyByteBuf, D> codec) {
        player.openMenu(new ExtendedMenuProvider<D>() {
            @Override
            public D getScreenOpeningData(ServerPlayer player) {
                return data;
            }

            @Override
            public Component getDisplayName() {
                return provider.getDisplayName();
            }

            @Override
            public @Nullable AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
                return provider.createMenu(syncId, playerInventory, player);
            }

            @Override
            public boolean shouldCloseCurrentScreen() {
                return provider.shouldCloseCurrentScreen();
            }
        });
    }
}
