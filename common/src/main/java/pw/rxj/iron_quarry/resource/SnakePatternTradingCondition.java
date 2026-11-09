package pw.rxj.iron_quarry.resource;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import pw.rxj.iron_quarry.Main;

public class SnakePatternTradingCondition implements LootItemCondition {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Main.MOD_ID, "snake_pattern_trading_enabled");
    public static final SnakePatternTradingCondition INSTANCE = new SnakePatternTradingCondition();
    public static final MapCodec<SnakePatternTradingCondition> CODEC = MapCodec.unit(INSTANCE);

    private SnakePatternTradingCondition() { }

    @Override
    public boolean test(LootContext context) {
        return Main.CONFIG.getExpansionsConfig().getSnakePattern().tradingEnabled;
    }

    @Override
    public MapCodec<SnakePatternTradingCondition> codec() {
        return CODEC;
    }
}
