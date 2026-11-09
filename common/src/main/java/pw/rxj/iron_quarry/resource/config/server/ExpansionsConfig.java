package pw.rxj.iron_quarry.resource.config.server;

import pw.rxj.iron_quarry.resource.config.AbstractInnerConfigHandler;
import pw.rxj.iron_quarry.resource.config.ConfigHandler;
import pw.rxj.iron_quarry.resource.config.util.Comment;

public class ExpansionsConfig {
    @Comment("Interdimensional Expansion Card: Allows quarries to mine in other dimensions.")
    public final Interdimensional interdimensional = new Interdimensional();
    @Comment("Snake Pattern Expansion Card: Quarries mine each layer along a random winding path instead of column by column.")
    public final SnakePattern snakePattern = new SnakePattern();
    @Comment("Gilbert Pattern Expansion Card: Quarries mine each layer along a space-filling curve instead of column by column.")
    public final GilbertPattern gilbertPattern = new GilbertPattern();

    public static class Interdimensional {
        @Comment("Turns the expansion card off: It has no effect, is hidden from the creative inventory and can't be crafted.")
        public final boolean disabled;

        public Interdimensional() {
            this.disabled = false;
        }
    }
    public static class SnakePattern {
        @Comment("Lets cartographers sell the expansion card. Only checked when a villager's trades are generated, existing trades stay.\nThis only turns the default trade on or off. A data pack can change the price in the villager trade iron_quarry:snake_pattern_expansion_card, or which villagers sell it through the villager_trade tag minecraft:cartographer/level_5.")
        public final boolean tradingEnabled;

        public SnakePattern() {
            this.tradingEnabled = true;
        }
    }
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

        public ExpansionsConfig.Interdimensional getInterdimensional() {
            return this.getServer().expansions.interdimensional;
        }
        public ExpansionsConfig.SnakePattern getSnakePattern() {
            return this.getServer().expansions.snakePattern;
        }
        public ExpansionsConfig.GilbertPattern getGilbertPattern() {
            return this.getServer().expansions.gilbertPattern;
        }
    }
}
