package pw.rxj.iron_quarry.resource.config.server;

import pw.rxj.iron_quarry.resource.config.AbstractInnerConfigHandler;
import pw.rxj.iron_quarry.resource.config.ConfigHandler;
import pw.rxj.iron_quarry.resource.config.util.Comment;

public class ExpansionsConfig {
    @Comment("Gilbert Pattern Expansion Card: Quarries mine each layer along a space-filling curve instead of column by column.")
    public final GilbertPattern gilbertPattern = new GilbertPattern();

    public static class GilbertPattern {
        @Comment("Loot tables that can contain the gilbert pattern expansion card.")
        public final LootPoolEntry[] lootPools = new LootPoolEntry[]{
                LootPoolEntry.lowChance("minecraft:chests/ancient_city"),
                LootPoolEntry.lowChance("minecraft:chests/end_city_treasure")
        };
    }

    public static class Handler extends AbstractInnerConfigHandler {
        private Handler(ConfigHandler handler) {
            super(handler);
        }
        public static Handler of(ConfigHandler handler) {
            return new Handler(handler);
        }

        public ExpansionsConfig.GilbertPattern getGilbertPattern() {
            return this.getServer().expansions.gilbertPattern;
        }
    }
}
