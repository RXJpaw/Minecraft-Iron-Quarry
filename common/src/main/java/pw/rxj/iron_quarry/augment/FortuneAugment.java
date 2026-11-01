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

    public float getLevel(float progress) {
        return progress * this.config.getFortune().maxLevel;
    }
    public float getLevelLimit() {
        return this.config.getFortune().levelLimit;
    }
    public float getEnergyBonus(float progress) {
        return this.getLevel(progress) * this.getEnergyPerLevel();
    }
    public float getEnergyPerLevel() {
        AugmentsConfig.Fortune fortune = this.config.getFortune();
        if(fortune.maxLevel == 0) return 0.0F;

        return fortune.maxEnergyBonus / fortune.maxLevel;
    }

    @Override
    public void appendEffectTooltip(int amount, Consumer<Component> textConsumer) {
        float progress = this.getProgress(amount);

        MutableComponent LORE_BENEFIT = ReadableString.translatable("item.iron_quarry.augment.lore.benefit.fortune", ZUtil.expandableFixedFloat(this.getLevel(progress)));
        MutableComponent LORE_DRAWBACK = ReadableString.translatable("item.iron_quarry.augment.lore.drawback.fortune_energy_factor", ZUtil.expandableFixedFloat(this.getEnergyBonus(progress) / 100));

        textConsumer.accept(LORE_BENEFIT);
        textConsumer.accept(LORE_DRAWBACK);
    }
}
