package pw.rxj.iron_quarry.augment;

import net.minecraft.network.chat.Component;
import pw.rxj.iron_quarry.item.AugmentItem;
import pw.rxj.iron_quarry.util.MathUtil;

import java.util.Objects;
import java.util.function.Consumer;

public abstract class AbstractAugment {
    public abstract boolean isDisabled();
    public abstract int getBaseAmount();

    /**
     *  C(U) = B * (1 + 0.56 * U * (U + 1))
     */
    public int getCapacity(int capacityUpgrades) {
        Objects.checkIndex(capacityUpgrades, AugmentItem.CAPACITY_UPGRADE_SLOTS + 1);

        return Math.round(this.getBaseAmount() * (1 + 0.56F * capacityUpgrades * (capacityUpgrades + 1)));
    }
    /**
     *  P(A) = log_2(log_2(2 + A / 10 000)) / log_2(log_2(2 + C(7) / 10 000))
     */
    public float getProgress(int amount) {
        double divisor = MathUtil.log2(MathUtil.log2(2 + this.getCapacity(AugmentItem.CAPACITY_UPGRADE_SLOTS) / 10_000d));
        if(divisor == 0) return 0.0F;

        return (float) (MathUtil.log2(MathUtil.log2(2 + amount / 10_000d)) / divisor);
    }

    public abstract void appendEffectTooltip(int amount, Consumer<Component> textConsumer);
}
