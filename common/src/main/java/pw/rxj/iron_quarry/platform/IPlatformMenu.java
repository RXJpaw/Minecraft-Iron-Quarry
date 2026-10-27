package pw.rxj.iron_quarry.platform;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public interface IPlatformMenu {
    <T extends AbstractContainerMenu, D> MenuType<T> createMenuType(MenuFactory<T, D> factory, StreamCodec<? super RegistryFriendlyByteBuf, D> codec);
    <D> void openMenu(ServerPlayer player, MenuProvider provider, D data, StreamCodec<? super RegistryFriendlyByteBuf, D> codec);

    interface MenuFactory<T extends AbstractContainerMenu, D> {
        T create(int syncId, Inventory playerInventory, D data);
    }
}
