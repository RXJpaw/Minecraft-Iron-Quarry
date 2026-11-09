package pw.rxj.iron_quarry.resource.config.server;

import lombok.AllArgsConstructor;
import pw.rxj.iron_quarry.resource.config.util.Comment;
import pw.rxj.iron_quarry.resource.config.util.Range;

@AllArgsConstructor
public class LootPoolEntry {
    @Comment("ID of the loot table.")
    public final String lootTable;
    @Comment("Least rolls of the pool.")
    @Range(min = 0)
    public final float rollsMin;
    @Comment("Most rolls of the pool.")
    @Range(min = 0)
    public final float rollsMax;
    @Comment("Chance that the pool is rolled at all.")
    @Range(min = 0, max = 1)
    public final float chance;

    public static LootPoolEntry lowChance(String lootTable) {
        return new LootPoolEntry(lootTable, 1.0F, 1.0F, 0.05F);
    }
    public static LootPoolEntry mediumChance(String lootTable) {
        return new LootPoolEntry(lootTable, 1.0F, 1.0F, 0.12F);
    }
    public static LootPoolEntry highChance(String lootTable) {
        return new LootPoolEntry(lootTable, 1.0F, 1.0F, 0.26F);
    }
}
