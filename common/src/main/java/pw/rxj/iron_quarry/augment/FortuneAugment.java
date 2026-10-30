package pw.rxj.iron_quarry.augment;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import pw.rxj.iron_quarry.resource.config.server.AugmentsConfig;
import pw.rxj.iron_quarry.util.ReadableString;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.function.Consumer;

public class FortuneAugment extends AbstractAugment {
    private final AugmentsConfig.Handler config;

    private FortuneAugment(AugmentsConfig.Handler config) {
        this.config = config;
    }
    public static FortuneAugment of(AugmentsConfig.Handler config) {
        return new FortuneAugment(config);
    }

    @Override
    public boolean isDisabled() {
        return this.config.getFortune().disabled;
    }
    @Override
    public int getBaseAmount() {
        return this.config.getFortune().baseAmount;
    }

    public float getYieldBonus(float progress) {
        return progress * this.config.getFortune().maxYieldBonus;
    }
    public float getEnergyBonus(float progress) {
        return progress * this.config.getFortune().maxEnergyBonus;
    }

    @Override
    public void appendEffectTooltip(int amount, Consumer<Component> textConsumer) {
        float progress = this.getProgress(amount);

        MutableComponent LORE_BENEFIT = ReadableString.translatable("item.iron_quarry.augment.lore.benefit.fortune", ZUtil.expandableFixedFloat(this.getYieldBonus(progress)));
        MutableComponent LORE_DRAWBACK = ReadableString.translatable("item.iron_quarry.augment.lore.drawback.energy", ZUtil.expandableFixedFloat(this.getEnergyBonus(progress)));

        textConsumer.accept(LORE_BENEFIT);
        textConsumer.accept(LORE_DRAWBACK);
    }
}
