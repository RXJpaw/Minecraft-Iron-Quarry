package pw.rxj.iron_quarry.platform;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;

public class NeoForgePlatformMenu implements IPlatformMenu {
    @Override
    public <T extends AbstractContainerMenu, D> MenuType<T> createMenuType(MenuFactory<T, D> factory, StreamCodec<? super RegistryFriendlyByteBuf, D> codec) {
        return IMenuTypeExtension.create((syncId, playerInventory, buf) -> factory.create(syncId, playerInventory, codec.decode(buf)));
    }

    @Override
    public <D> void openMenu(ServerPlayer player, MenuProvider provider, D data, StreamCodec<? super RegistryFriendlyByteBuf, D> codec) {
        player.openMenu(provider, buf -> codec.encode(buf, data));
    }
}
