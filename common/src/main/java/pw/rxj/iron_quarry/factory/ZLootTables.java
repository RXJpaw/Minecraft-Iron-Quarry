package pw.rxj.iron_quarry.factory;

import net.minecraft.world.level.storage.loot.LootPool;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.event.LootTableCallback;
import pw.rxj.iron_quarry.resource.config.server.ChestLootingAugmentConfig;

public class ZLootTables {
    private static final ChestLootingAugmentConfig.Handler CHEST_LOOTING_AUGMENT_CONFIG = Main.CONFIG.getChestLootingAugmentConfig();
    public static void register() {
        Main.LOGGER.info("Registering loot tables..");

        LootTableCallback.MODIFY.register((lootTableId, poolAdder) -> {
            LootPool.Builder poolBuilder = CHEST_LOOTING_AUGMENT_CONFIG.getLootPoolBuilder(lootTableId);

            if(poolBuilder != null) poolAdder.accept(poolBuilder);
        });
    }
}
