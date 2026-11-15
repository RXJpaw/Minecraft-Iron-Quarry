package pw.rxj.iron_quarry.util;

import com.mojang.serialization.Codec;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import pw.rxj.iron_quarry.resource.config.util.ConfigFields;

public class ComplexOption {
    private ComplexOption() { }


    public static OptionInstance<String> rotatingOptionFrom(String key, boolean autoTooltip, Class<? extends Enum<?>> clazz, String defaultValue, Consumer<String> write) {
        return rotatingOptionFrom(
                key,
                autoTooltip ? OptionInstance.cachedConstantTooltip(ReadableString.translatable(key + ".tooltip")) : OptionInstance.noTooltip(),
                getOptionList(clazz),
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
                write
        );
    }

    public static OptionInstance<String> resolvingOptionFrom(String key, UnaryOperator<String> resolve, Class<? extends Enum<?>> clazz, String defaultValue, Consumer<String> write) {
        return new OptionInstance<>(
                key,
                value -> Tooltip.create(ReadableString.translatable(key + "." + value + ".tooltip")),
                (optionText, value) -> {
                    String resolved = resolve.apply(value);
                    String resolvedText = Component.translatable(key + "." + resolved).getString();

                    return resolved.equals(value) ? ReadableString.parse(resolvedText) : ReadableString.translatable(key + "." + value, resolvedText);
                },
                new OptionInstance.LazyEnum<>(() -> getOptionList(clazz), Optional::of, Codec.STRING),
                defaultValue,
                write
        );
    }

    private static List<String> getOptionList(Class<? extends Enum<?>> clazz) {
        return clazz.isEnum() ? ConfigFields.getOptionNames(clazz) : List.of();
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
                write
        );
    }

    public static <T> OptionInstance.TooltipSupplier<T> emptyTooltip() {
        return (client) -> null;
    }

    public static <T extends Number> ValueFormatter<T> valueDivider(String formatter, double divider) {
        return (key -> (optionText, value) -> {
            Object castValue;

            try {
                castValue = String.format(Locale.ROOT, formatter, value.doubleValue() / divider);
            } catch (IllegalFormatException ignored) {
                castValue = "NaN";
            }

            return ReadableString.translatable(key, castValue);
        });
    }

    public interface ValueFormatter<T extends Number> extends Function<String, OptionInstance.CaptionBasedToString<T>> { }
}
