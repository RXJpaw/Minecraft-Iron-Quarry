package pw.rxj.iron_quarry.expansion;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import pw.rxj.iron_quarry.resource.config.server.ExpansionsConfig;
import pw.rxj.iron_quarry.util.ReadableString;

import java.util.function.Consumer;

public class InterdimensionalExpansion extends AbstractExpansion {
    private final ExpansionsConfig.Handler config;

    private InterdimensionalExpansion(ExpansionsConfig.Handler config) {
        this.config = config;
    }
    public static InterdimensionalExpansion of(ExpansionsConfig.Handler config) {
        return new InterdimensionalExpansion(config);
    }

    @Override
    public boolean isDisabled() {
        return !this.config.getInterdimensional().enabled;
    }

    public void appendOtherDimensionDeniedTooltip(Consumer<Component> textConsumer) {
        textConsumer.accept(ReadableString.translatable("item.iron_quarry.warning.quarry_other_dimension_denied"));

        if(Minecraft.getInstance().hasShiftDown() && !this.isDisabled()) {
            textConsumer.accept(ReadableString.translatable("item.iron_quarry.warning.quarry_other_dimension_denied.hint"));
        }
    }
}
