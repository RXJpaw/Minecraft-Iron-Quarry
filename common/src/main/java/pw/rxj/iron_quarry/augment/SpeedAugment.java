package pw.rxj.iron_quarry.augment;

import net.minecraft.client.Minecraft;
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
        return !this.config.getSpeed().enabled;
    }
    @Override
    public int getBaseAmount() {
        return this.config.getSpeed().baseAmount;
    }

    public float getSpeedBonus(float progress) {
        return progress * this.config.getSpeed().maxSpeedBonus;
    }
    public float getEnergyBonus(float progress) {
        return this.getSpeedBonus(progress) * this.getEnergyRatio(progress);
    }
    public float getEnergyRatio(float progress) {
        float curve = (float) Math.pow(progress, this.config.getSpeed().energyRatioExponent);

        return 1 + (this.getFullEnergyRatio() - 1) * curve;
    }
    public float getSpeedLimit() {
        return this.config.getSpeed().speedFactorLimit;
    }
    public float getFullEnergyRatio() {
        AugmentsConfig.Speed speed = this.config.getSpeed();
        if(speed.maxSpeedBonus == 0) return 1.0F;

        return speed.maxEnergyBonus / speed.maxSpeedBonus;
    }

    @Override
    public void appendEffectTooltip(int amount, Consumer<Component> textConsumer) {
        float progress = this.getProgress(amount);

        MutableComponent LORE_BENEFIT = ReadableString.translatable("item.iron_quarry.augment.lore.benefit.speed", ZUtil.expandableFixedFloat(this.getSpeedBonus(progress)));
        MutableComponent LORE_DRAWBACK = ReadableString.translatable("item.iron_quarry.augment.lore.drawback.speed_energy_factor", ZUtil.expandableFixedFloat(this.getEnergyBonus(progress)));

        textConsumer.accept(LORE_BENEFIT);
        textConsumer.accept(LORE_DRAWBACK);

        if(Minecraft.getInstance().hasShiftDown()) {
            ReadableString.translatableLines("item.iron_quarry.augment.lore.energy_factor_stacking.speed").forEach(textConsumer);
        }
    }
}
