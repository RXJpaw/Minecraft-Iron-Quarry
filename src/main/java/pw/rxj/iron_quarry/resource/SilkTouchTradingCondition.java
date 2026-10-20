package pw.rxj.iron_quarry.resource;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import pw.rxj.iron_quarry.Main;

public class SilkTouchTradingCondition implements ResourceCondition {
    public static final SilkTouchTradingCondition INSTANCE = new SilkTouchTradingCondition();
    public static final ResourceConditionType<SilkTouchTradingCondition> TYPE = ResourceConditionType.create(Identifier.fromNamespaceAndPath(Main.MOD_ID, "silk_touch_trading_enabled"), MapCodec.unit(INSTANCE));

    private SilkTouchTradingCondition() { }

    @Override
    public ResourceConditionType<?> getType() {
        return TYPE;
    }

    @Override
    public boolean test(RegistryOps.RegistryInfoLookup registryInfo) {
        return Main.CONFIG.getSilkTouchAugmentConfig().isTradingEnabled();
    }
}
