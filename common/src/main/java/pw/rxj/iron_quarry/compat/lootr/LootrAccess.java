package pw.rxj.iron_quarry.compat.lootr;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.storage.loot.LootTable;
import noobanidus.mods.lootr.common.api.LootrAPI;
import noobanidus.mods.lootr.common.api.LootrTags;
import noobanidus.mods.lootr.common.api.data.ILootrInventoryStore;
import noobanidus.mods.lootr.common.api.data.blockentity.ILootrBlockEntity;
import noobanidus.mods.lootr.common.api.interfaces.accessor.ILootrDataAccessor;
import noobanidus.mods.lootr.common.api.interfaces.inventory.ILootrInventory;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

class LootrAccess {
    private static final QuarryLootFiller LOOT_FILLER = QuarryLootFiller.of();

    static @Nullable Container getLootableContainer(RandomizableContainerBlockEntity blockEntity, Supplier<ServerPlayer> ownerSupplier) {
        ILootrBlockEntity lootrBlockEntity = LootrAPI.wrapBlockEntity(blockEntity);
        if(lootrBlockEntity != null) {
            ServerPlayer owner = ownerSupplier.get();
            return owner == null ? null : getOwnerInventory(lootrBlockEntity, owner);
        }
        if(isConvertible(blockEntity)) return null;

        return blockEntity;
    }

    static void markOpened(BlockEntity blockEntity, Supplier<ServerPlayer> ownerSupplier) {
        ILootrBlockEntity lootrBlockEntity = LootrAPI.wrapBlockEntity(blockEntity);
        if(lootrBlockEntity == null) return;

        ServerPlayer owner = ownerSupplier.get();
        if(owner != null && lootrBlockEntity.addOpener(owner)) lootrBlockEntity.performUpdate();
    }

    private static boolean isConvertible(BlockEntity blockEntity) {
        if(LootrAPI.isDisabled()) return false;

        if(!(blockEntity.getLevel() instanceof ServerLevel world)) return false;
        if(blockEntity.typeHolder().is(LootrTags.BlockEntity.CONVERT_BLACKLIST)) return false;

        if(LootrAPI.isDimensionBlocked(world.dimension())) return false;
        if(!LootrAPI.isWorldBorderSafe(world, blockEntity.getBlockPos())) return false;
        if(LootrAPI.replacementBlockState(blockEntity.getBlockState()) == null) return false;

        ILootrDataAccessor<BlockEntity> accessor = LootrAPI.getAccessor(blockEntity);
        if(accessor == null) return false;

        ResourceKey<LootTable> lootTable = accessor.getLootTable(blockEntity);
        return lootTable != null && !LootrAPI.isLootTableBlacklisted(lootTable);
    }

    private static @Nullable Container getOwnerInventory(ILootrBlockEntity lootrBlockEntity, ServerPlayer owner) {
        ILootrInventoryStore inventoryStore = LootrAPI.getData(lootrBlockEntity);
        if(inventoryStore == null) return null;

        ILootrInventory inventory = inventoryStore.getInventory(owner);
        if(inventory != null) return inventory;

        inventory = inventoryStore.createInventory(lootrBlockEntity, owner, LOOT_FILLER);
        if(inventory != null && inventory.isEmpty()) LOOT_FILLER.unpackLootTable(lootrBlockEntity, owner, inventory);

        return inventory;
    }
}
