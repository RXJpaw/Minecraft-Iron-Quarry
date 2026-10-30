package pw.rxj.iron_quarry.augment;

import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class EmptyAugment extends AbstractAugment {
    private EmptyAugment() { }
    public static EmptyAugment of() {
        return new EmptyAugment();
    }

    @Override
    public boolean isDisabled() {
        return false;
    }
    @Override
    public int getBaseAmount() {
        return 1;
    }

    @Override
    public void appendEffectTooltip(int amount, Consumer<Component> textConsumer) { }
}
