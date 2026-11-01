package pw.rxj.iron_quarry.compat.lootr;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import noobanidus.mods.lootr.common.api.LootrAPI;
import noobanidus.mods.lootr.common.api.data.ILootrContainerInstance;
import noobanidus.mods.lootr.common.api.filler.ILootFiller;
import org.jetbrains.annotations.Nullable;

class QuarryLootFiller implements ILootFiller {
    private QuarryLootFiller() {
    }
    static QuarryLootFiller of() {
        return new QuarryLootFiller();
    }

    @Override
    public void unpackLootTable(ILootrContainerInstance provider, @Nullable Player player, Container inventory) {
        if(!(provider.getDataLevel() instanceof ServerLevel world)) return;

        NonNullList<ItemStack> referenceInventory = provider.getDataReferenceInventory();
        if(provider.isDataReferenceInventory() && referenceInventory != null) {
            for (int slot = 0; slot < referenceInventory.size(); slot++) {
                inventory.setItem(slot, referenceInventory.get(slot).copy());
            }
            return;
        }

        ResourceKey<LootTable> lootTableKey = provider.getDataLootTable();
        if(lootTableKey == null) return;

        LootTable lootTable = world.getServer().reloadableRegistries().getLootTable(lootTableKey);
        LootParams lootParams = new LootParams.Builder(world)
                .withParameter(LootContextParams.ORIGIN, provider.getDataVec())
                .create(LootContextParamSets.CHEST);

        this.fill(provider, null, lootTableKey, lootTable, inventory, lootParams, LootrAPI.getLootSeed(provider.getDataLootSeed()));
    }
}
