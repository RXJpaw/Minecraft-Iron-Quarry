package pw.rxj.iron_quarry.interfaces;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public interface ITickingInventoryItem {
    void tick(ServerPlayer playerEntity, ServerLevel world, ItemStack stack);
}
