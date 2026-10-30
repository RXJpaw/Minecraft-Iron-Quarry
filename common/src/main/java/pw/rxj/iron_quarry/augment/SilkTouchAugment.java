package pw.rxj.iron_quarry.augment;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import pw.rxj.iron_quarry.resource.config.server.AugmentsConfig;
import pw.rxj.iron_quarry.util.ReadableString;

import java.util.function.Consumer;

public class SilkTouchAugment extends AbstractAugment {
    private final AugmentsConfig.Handler config;

    private SilkTouchAugment(AugmentsConfig.Handler config) {
        this.config = config;
    }
    public static SilkTouchAugment of(AugmentsConfig.Handler config) {
        return new SilkTouchAugment(config);
    }

    @Override
    public boolean isDisabled() {
        return this.config.getSilkTouch().disabled;
    }
    @Override
    public int getBaseAmount() {
        return 0;
    }

    public float getSlowness() {
        return this.config.getSilkTouch().slowness;
    }
    public boolean isTradingEnabled() {
        return this.config.getSilkTouch().tradingEnabled;
    }

    @Override
    public void appendEffectTooltip(int amount, Consumer<Component> textConsumer) {
        MutableComponent LORE_BENEFIT = ReadableString.translatable("item.iron_quarry.augment.lore.benefit.silk_touch");
        MutableComponent LORE_DRAWBACK = ReadableString.translatable("item.iron_quarry.augment.lore.drawback.speed", ReadableString.minPrecisionFloat(this.getSlowness()));

        textConsumer.accept(LORE_BENEFIT);
        textConsumer.accept(LORE_DRAWBACK);
    }
}
