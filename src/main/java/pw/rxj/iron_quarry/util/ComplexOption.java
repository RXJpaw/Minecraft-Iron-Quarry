package pw.rxj.iron_quarry.util;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;

public class ComplexOption {
    private ComplexOption() { }


    public static OptionInstance<String> rotatingOptionFrom(String key, boolean autoTooltip, Class<? extends Enum<?>> clazz, String defaultValue, Consumer<String> write) {
        return rotatingOptionFrom(
                key,
                autoTooltip ? OptionInstance.cachedConstantTooltip(ReadableString.translatable(key + ".tooltip")) : OptionInstance.noTooltip(),
                clazz.isEnum() ? Arrays.stream(clazz.getEnumConstants()).map(anEnum -> anEnum.toString().toLowerCase()).toList() : List.of(),
                defaultValue,
                write
        );
    }
    public static OptionInstance<String> rotatingOptionFrom(String key, OptionInstance.TooltipSupplier<String> tooltipFactory, List<String> optionList, String defaultValue, Consumer<String> write) {
        return new OptionInstance<>(
                key,
                tooltipFactory,
                (optionText, value) -> Component.translatable(key + "." + value),
                new OptionInstance.LazyEnum<>(() -> optionList, Optional::of, Codec.STRING),
                defaultValue,
                write::accept
        );
    }

    public static OptionInstance<Integer> sliderOptionFrom(String key, ValueFormatter<Integer> valueDivider, boolean autoTooltip, int min, int max, int defaultValue, Consumer<Integer> write) {
        return sliderOptionFrom(
                key,
                valueDivider,
                autoTooltip ? OptionInstance.cachedConstantTooltip(ReadableString.translatable(key + ".tooltip")) : OptionInstance.noTooltip(),
                min, max,
                defaultValue,
                write
        );
    }
    public static OptionInstance<Integer> sliderOptionFrom(String key, ValueFormatter<Integer> valueDivider, OptionInstance.TooltipSupplier<Integer> tooltipFactory, int min, int max, int defaultValue, Consumer<Integer> write) {
        return new OptionInstance<>(
                key,
                tooltipFactory,
                valueDivider.apply(key),
                new OptionInstance.IntRange(min, max),
                Codec.intRange(min, max),
                defaultValue,
                write::accept
        );
    }

    public static <T> OptionInstance.TooltipSupplier<T> emptyTooltip() {
        return (client) -> null;
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

    public interface ValueFormatter<T extends Number> extends Function<String, OptionInstance.CaptionBasedToString<T>> { }
}
