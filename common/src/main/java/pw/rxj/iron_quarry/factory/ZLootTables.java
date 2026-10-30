package pw.rxj.iron_quarry.factory;

import net.minecraft.world.level.storage.loot.LootPool;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.augment.ZAugments;
import pw.rxj.iron_quarry.event.LootTableCallback;

public class ZLootTables {
    public static void register() {
        Main.LOGGER.info("Registering loot tables..");

        LootTableCallback.MODIFY.register((lootTableId, poolAdder) -> {
            LootPool.Builder poolBuilder = ZAugments.CHEST_LOOTING.getLootPoolBuilder(lootTableId);

            if(poolBuilder != null) poolAdder.accept(poolBuilder);
        });
    }
}
