package pw.rxj.iron_quarry.resource;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import pw.rxj.iron_quarry.Main;

//Doesn't respect minecraft's usual /reload and applies instantly after config save!
public class SilkTouchTradingCondition implements LootItemCondition {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Main.MOD_ID, "silk_touch_trading_enabled");
    public static final SilkTouchTradingCondition INSTANCE = new SilkTouchTradingCondition();
    public static final MapCodec<SilkTouchTradingCondition> CODEC = MapCodec.unit(INSTANCE);

    private SilkTouchTradingCondition() { }

    @Override
    public boolean test(LootContext context) {
        return Main.CONFIG.getSilkTouchAugmentConfig().isTradingEnabled();
    }

    @Override
    public MapCodec<SilkTouchTradingCondition> codec() {
        return CODEC;
    }
}
