package pw.rxj.iron_quarry.augment;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import pw.rxj.iron_quarry.resource.config.server.AugmentsConfig;
import pw.rxj.iron_quarry.util.ReadableString;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.function.Consumer;

public class SpeedAugment extends AbstractAugment {
    private final AugmentsConfig.Handler config;

    private SpeedAugment(AugmentsConfig.Handler config) {
        this.config = config;
    }
    public static SpeedAugment of(AugmentsConfig.Handler config) {
        return new SpeedAugment(config);
    }

    @Override
    public boolean isDisabled() {
        return this.config.getSpeed().disabled;
    }
    @Override
    public int getBaseAmount() {
        return this.config.getSpeed().baseAmount;
    }

    public float getSpeedBonus(float progress) {
        return progress * this.config.getSpeed().maxSpeedBonus;
    }
    public float getEnergyBonus(float progress) {
        return progress * this.config.getSpeed().maxEnergyBonus;
    }
    public float getEnergyPerSpeed() {
        AugmentsConfig.Speed speed = this.config.getSpeed();
        if(speed.maxSpeedBonus == 0) return 0.0F;

        return (float) speed.maxEnergyBonus / speed.maxSpeedBonus;
    }

    @Override
    public void appendEffectTooltip(int amount, Consumer<Component> textConsumer) {
        float progress = this.getProgress(amount);

        MutableComponent LORE_BENEFIT = ReadableString.translatable("item.iron_quarry.augment.lore.benefit.speed", ZUtil.expandableFixedFloat(this.getSpeedBonus(progress)));
        MutableComponent LORE_DRAWBACK = ReadableString.translatable("item.iron_quarry.augment.lore.drawback.energy", ZUtil.expandableFixedFloat(this.getEnergyBonus(progress)));

        textConsumer.accept(LORE_BENEFIT);
        textConsumer.accept(LORE_DRAWBACK);
    }
}
