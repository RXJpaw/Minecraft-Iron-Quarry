package pw.rxj.iron_quarry.resource.config.server;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.item.ZItems;
import pw.rxj.iron_quarry.resource.config.AbstractInnerConfigHandler;
import pw.rxj.iron_quarry.resource.config.ConfigHandler;

import java.util.Arrays;
import java.util.Optional;

@EqualsAndHashCode
public class ChestLootingAugmentConfig {
    public final Entry[] lootPools = new Entry[]{
            Entry.common("minecraft:chests/abandoned_mineshaft"),
            Entry.common("minecraft:chests/desert_pyramid"),
            Entry.common("minecraft:chests/igloo_chest"),
            Entry.common("minecraft:chests/jungle_temple"),
            Entry.common("minecraft:chests/simple_dungeon"),
            Entry.common("minecraft:chests/shipwreck_treasure"),
            Entry.common("minecraft:chests/underwater_ruin_small"),
            Entry.common("minecraft:chests/village/village_weaponsmith"),
            Entry.common("minecraft:chests/village/village_armorer"),
            Entry.common("minecraft:chests/village/village_toolsmith"),

            Entry.uncommon("minecraft:chests/stronghold_corridor"),
            Entry.uncommon("minecraft:chests/stronghold_crossing"),
            Entry.uncommon("minecraft:chests/stronghold_library"),
            Entry.uncommon("minecraft:chests/underwater_ruin_big"),
            Entry.uncommon("minecraft:chests/pillager_outpost"),

            Entry.rare("minecraft:chests/woodland_mansion"),
            Entry.rare("minecraft:chests/ancient_city")
    };

    @EqualsAndHashCode
    @AllArgsConstructor
    public static class Entry {
        public final String lootTable;
        public final float rollsMin;
        public final float rollsMax;
        public final float chance;

        public static Entry common(String lootTable) {
            return new Entry(lootTable, 1.0F, 1.0F, 0.05F);
        }
        public static Entry uncommon(String lootTable) {
            return new Entry(lootTable, 1.0F, 1.0F, 0.12F);
        }
        public static Entry rare(String lootTable) {
            return new Entry(lootTable, 1.0F, 1.0F, 0.26F);
        }
    }

    public static class Handler extends AbstractInnerConfigHandler {
        private Handler(ConfigHandler handler) {
            super(handler);
        }
        public static Handler of(ConfigHandler handler) {
            return new Handler(handler);
        }

        public @Nullable LootPool.Builder getLootPoolBuilder(Identifier id) {
            Optional<Entry> optionalEntry = Arrays.stream(this.server.chestLootingAugmentConfig.lootPools).filter(el -> el.lootTable.equals(id.toString())).findAny();
            if(optionalEntry.isEmpty()) return null;
            Entry entry = optionalEntry.get();

            return LootPool.lootPool()
                    .setRolls(ContextIntProviders.fromFloat(ContextFloatProviders.round(ContextFloatProviders.between(entry.rollsMin, entry.rollsMax))))
                    .when(LootItemRandomChanceCondition.randomChance(entry.chance))
                    .add(LootItem.lootTableItem(ZItems.CHEST_LOOTING_AUGMENT));
        }
    }
}
