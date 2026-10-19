package pw.rxj.iron_quarry.factory;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.loot.LootPool;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.resource.config.server.ChestLootingAugmentConfig;

public class ZLootTables {
    private static final ChestLootingAugmentConfig.Handler CHEST_LOOTING_AUGMENT_CONFIG = Main.CONFIG.getChestLootingAugmentConfig();
    public static void register() {
        Main.LOGGER.info("Registering loot tables..");

        LootTableEvents.MODIFY.register((registryKey, builder, lootTableSource, registries) -> {
            LootPool.Builder poolBuilder = CHEST_LOOTING_AUGMENT_CONFIG.getLootPoolBuilder(registryKey.getValue());

            if(poolBuilder != null) builder.pool(poolBuilder);
        });
    }
}
