package pw.rxj.iron_quarry.util;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import pw.rxj.iron_quarry.Global;
import pw.rxj.iron_quarry.types.DynamicText;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReadableString {
    public static Optional<String> from(BlockPos blockPos) {
        if(blockPos == null) return Optional.empty();

        return String.format("%s, %s, %s", blockPos.getX(), blockPos.getY(), blockPos.getZ()).describeConstable();
    }
    public static Optional<Text> textFrom(BlockPos blockPos) {
        return from(blockPos).map(Text::of);
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
    public static Optional<Text> textFrom(Identifier identifier) {
        return from(identifier).map(Text::of);
    }

    public static MutableText translatable(String key, Object... args) {
        MutableText translation = Text.translatable(key, args);
        MutableText parsed = Text.empty();

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

                parsed.append(Text.literal(text).setStyle(style));
            } else if(modifier instanceof DynamicText dynamicText) {
                MutableText dynamicResult = dynamicText.getText(Text.literal(text), textOffset);

                parsed.append(dynamicResult);
            }

            textOffset += text.length();
        }

        return parsed.append(string.substring(offset));
    }
}
