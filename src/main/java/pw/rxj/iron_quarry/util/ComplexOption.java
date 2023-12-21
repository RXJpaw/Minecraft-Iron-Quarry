package pw.rxj.iron_quarry.util;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import net.minecraft.client.option.SimpleOption;

import java.util.IllegalFormatException;
import java.util.function.Consumer;
import java.util.function.Function;

public class ComplexOption {
    private ComplexOption() { }


    public static SimpleOption<Integer> sliderOptionFrom(String key, ValueFormatter<Integer> valueDivider, boolean autoTooltip, int min, int max, int defaultValue, Consumer<Integer> write) {
        return sliderOptionFrom(
                key,
                valueDivider,
                autoTooltip ? SimpleOption.constantTooltip(ReadableString.translatable(key + ".tooltip")) : SimpleOption.emptyTooltip(),
                min, max,
                defaultValue,
                write
        );
    }

    public static SimpleOption<Integer> sliderOptionFrom(String key, ValueFormatter<Integer> valueDivider, SimpleOption.TooltipFactoryGetter<Integer> tooltipFactory, int min, int max, int defaultValue, Consumer<Integer> write) {
        return new SimpleOption<>(
                key,
                tooltipFactory,
                valueDivider.apply(key),
                new SimpleOption.ValidatingIntSliderCallbacks(min, max),
                Codec.intRange(min, max),
                defaultValue,
                write
        );
    }

    public static <T> SimpleOption.TooltipFactoryGetter<T> emptyTooltip() {
        return (client) -> (value) -> ImmutableList.of();
    }

    public static <T extends Number> ValueFormatter<T> valueDivider(String formatter, double divider) {
        return (key -> (optionText, value) -> {
            Object castValue;

            try {
                castValue = String.format(formatter, value.doubleValue() / divider);
            } catch (IllegalFormatException ignored) {
                castValue = "NaN";
            }

            return ReadableString.translatable(key, castValue);
        });
    }

    public interface ValueFormatter<T extends Number> extends Function<String, SimpleOption.ValueTextGetter<T>> { }
}
