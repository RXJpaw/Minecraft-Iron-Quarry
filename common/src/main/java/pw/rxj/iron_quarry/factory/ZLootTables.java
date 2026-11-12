package pw.rxj.iron_quarry.factory;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.augment.ZAugments;
import pw.rxj.iron_quarry.event.LootTableCallback;
import pw.rxj.iron_quarry.item.ZItems;
import pw.rxj.iron_quarry.resource.config.server.ExpansionsConfig;
import pw.rxj.iron_quarry.resource.config.server.LootPoolEntry;

import java.util.Arrays;
import java.util.Optional;

public class ZLootTables {
    private static final ExpansionsConfig.Handler EXPANSIONS_CONFIG = Main.CONFIG.getExpansionsConfig();

    public static void register() {
        Main.LOGGER.info("Registering loot tables..");

        LootTableCallback.MODIFY.register((lootTableId, poolAdder) -> {
            LootPool.Builder chestLootingPool = ZAugments.CHEST_LOOTING.getLootPoolBuilder(lootTableId);
            if(chestLootingPool != null) poolAdder.accept(chestLootingPool);

            LootPool.Builder gilbertPatternPool = poolOf(lootTableId, EXPANSIONS_CONFIG.getGilbertPattern().lootTables, ZItems.GILBERT_PATTERN_EXPANSION_CARD);
            if(gilbertPatternPool != null) poolAdder.accept(gilbertPatternPool);
        });
    }

    public static @Nullable LootPool.Builder poolOf(Identifier lootTableId, LootPoolEntry[] lootPools, ItemLike item) {
        Optional<LootPoolEntry> optionalEntry = Arrays.stream(lootPools).filter(el -> lootTableId.toString().equals(el.lootTable)).findAny();
        if(optionalEntry.isEmpty()) return null;
        LootPoolEntry entry = optionalEntry.get();

        return LootPool.lootPool()
                .setRolls(UniformGenerator.between(entry.rollsMin, entry.rollsMax))
                .when(LootItemRandomChanceCondition.randomChance(entry.chance))
                .add(LootItem.lootTableItem(item));
    }
}
