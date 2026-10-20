package pw.rxj.iron_quarry.util;

import org.jetbrains.annotations.NotNull;
import pw.rxj.iron_quarry.Global;
import pw.rxj.iron_quarry.render.Cuboid;
import pw.rxj.iron_quarry.types.DynamicText;
import com.mojang.blaze3d.platform.InputConstants;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public class ReadableString {
    public static Component ERROR = Component.nullToEmpty("<error>");

    public static DecimalFormat getDecimalFormatter(String pattern) {
        return new DecimalFormat(pattern, DecimalFormatSymbols.getInstance(Locale.ROOT));
    }
    public static <T extends Number> String intFrom(T number) {
        DecimalFormat formatter = new DecimalFormat("#,##0", DecimalFormatSymbols.getInstance(Locale.ROOT));

        return formatter.format(number);
    }
    public static <T extends Number> String cIntFrom(T number) {
        NumberFormat formatter = NumberFormat.getCompactNumberInstance(Locale.ROOT, NumberFormat.Style.SHORT);
        formatter.setMinimumFractionDigits(1);

        return formatter.format(number).replace("K", "k");
    }

    public static String minPrecisionFloat(float number) {
        if(number == (int) number)
            return String.format("%d", (int) number);
        else
            return String.format("%s", number);
    }
    public static String minPrecisionDouble(double number) {
        if(number == (long) number)
            return String.format("%d", (long) number);
        else
            return String.format("%s", number);
    }

    public static MutableComponent space() {
        return Component.literal(" ");
    }

    public static Optional<String> from(BlockPos blockPos) {
        if(blockPos == null) return Optional.empty();

        return String.format("%s, %s, %s", blockPos.getX(), blockPos.getY(), blockPos.getZ()).describeConstable();
    }
    public static Optional<Component> textFrom(BlockPos blockPos) {
        return from(blockPos).map(Component::nullToEmpty);
    }

    public static Optional<String> from(Identifier identifier) {
        if(identifier == null) return Optional.empty();
        String path = identifier.getPath();

        StringBuilder readablePath = new StringBuilder();
        for (String part : path.split("[-_]")) {
            if(!readablePath.isEmpty()) readablePath.append(" ");
            readablePath.append(ZUtil.capitalizeFirstLetter(part));
        }

        return readablePath.toString().describeConstable();
    }
    public static Optional<Component> textFrom(Identifier identifier) {
        return from(identifier).map(Component::nullToEmpty);
    }

    public static Optional<String> from(Cuboid cuboid) {
        if(cuboid == null) return Optional.empty();

        Vec3 abs = cuboid.fullblock().abs();

        return String.format("%s × %s × %s", (int) abs.x, (int) abs.y, (int) abs.z).describeConstable();
    }
    public static Optional<Component> textFrom(Cuboid cuboid) {
        return from(cuboid).map(Component::nullToEmpty);
    }

    public static Component textFrom(@NotNull KeyMapping keyBinding) {
        InputConstants.Type type = keyBinding.key.getType();
        int code = keyBinding.key.getValue();

        if(type.equals(InputConstants.Type.MOUSE)) {
            return switch (code) {
                case 0 -> Component.translatable("short_key.mouse.c.0");
                case 1 -> Component.translatable("short_key.mouse.c.1");
                case 2 -> Component.translatable("short_key.mouse.c.2");

                default -> keyBinding.getTranslatedKeyMessage();
            };
        } else {
            return keyBinding.getTranslatedKeyMessage();
        }
    }

    public static MutableComponent toTimeAgo(long timestamp) {
        long seconds = (System.currentTimeMillis() - timestamp) / 1000;

        if(seconds < 10) {
            return Component.translatable("time.unit.c.a_moment");
        } else if (seconds < 60) {
            return Component.translatable("time.unit.c.second.plural", seconds);
        } else if (seconds < 120) {
            return Component.translatable("time.unit.c.minute.singular", seconds / 60);
        } else if (seconds < 3_600) {
            return Component.translatable("time.unit.c.minute.plural", seconds / 60);
        } else if (seconds < 7_200) {
            return Component.translatable("time.unit.c.hour.singular", seconds / 3_600);
        } else if (seconds < 86_400) {
            return Component.translatable("time.unit.c.hour.plural", seconds / 3_600);
        } else if (seconds < 172_800) {
            return Component.translatable("time.unit.c.day.singular", seconds / 86_400);
        } else {
            return Component.translatable("time.unit.c.day.plural", seconds / 86_400);
        }
    }

    public static MutableComponent translatable(String key, Object... args) {
        MutableComponent translation = Component.translatable(key, args);
        MutableComponent parsed = Component.empty();

        String string = translation.getString();
        Pattern pattern = Pattern.compile("(.*?)§\\{(.*?) (.*?)\\}");
        Matcher matcher = pattern.matcher(string);

        int offset = 0;
        int textOffset = 0;
        while (matcher.find()) {
            offset = matcher.end();
            String text = matcher.group(3);
            parsed.append(matcher.group(1));

            Object modifier = Global.get(matcher.group(2));
            if(modifier == null) {
                textOffset += text.length();
                parsed.append(text);
                continue;
            }

            if(modifier instanceof Integer rgb) {
                Style style = Style.EMPTY.withColor(rgb);

                parsed.append(Component.literal(text).setStyle(style));
            } else if(modifier instanceof DynamicText dynamicText) {
                MutableComponent dynamicResult = dynamicText.getText(Component.literal(text), textOffset);

                parsed.append(dynamicResult);
            }

            textOffset += text.length();
        }

        return parsed.append(string.substring(offset));
    }
}
