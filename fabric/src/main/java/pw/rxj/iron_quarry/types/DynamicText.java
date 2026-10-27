package pw.rxj.iron_quarry.types;

import pw.rxj.iron_quarry.util.ColorGradient;

import java.awt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public enum DynamicText {
    EMPTY(0, "empty"),
    RAINBOW(1, "rainbow"),
    ELOCIN(2, "elocin"),
    EMERALD(3, "emerald"),
    GOLD(4, "gold");

    private final int id;
    private final String name;

    private DynamicText(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName(){
        return this.name;
    }
    public int getId(){
        return this.id;
    }

    public boolean isEmpty() { return this.equals(DynamicText.EMPTY); }
    public boolean isPresent() { return !this.isEmpty(); }

    public MutableComponent getText(Component text) {
        return this.getText(text, 0);
    }

    private static MutableComponent gradient(Component text, int offset, Color fromColor, Color toColor) {
        long time = System.currentTimeMillis() / 50L;
        String string = text.getString();

        MutableComponent name = Component.empty();
        ColorGradient gradient = new ColorGradient(fromColor, toColor);

        for (int i = 0; i < string.length(); i++) {
            double progress = (time - i - offset) % 45.0;
            Style style = Style.EMPTY.withColor(gradient.getColor((float) progress / 22.5F).getRGB());

            name.append(Component.literal(String.valueOf(string.charAt(i))).setStyle(style));
        }

        return name;
    }
    private static MutableComponent hue(Component text, int offset, float saturation) {
        long time = System.currentTimeMillis() / 50L;
        String string = text.getString();

        MutableComponent name = Component.empty();

        for (int i = 0; i < string.length(); i++) {
            double hue = 1.0/90.0 * (time - i - offset);
            Style style = Style.EMPTY.withColor(Color.HSBtoRGB((float) (hue % 360), saturation, 1.0F));

            name.append(Component.literal(String.valueOf(string.charAt(i))).setStyle(style));
        }

        return name;
    }


    public MutableComponent getText(Component text, int offset) {
        switch (this) {
            case EMPTY -> {
                return text.copy();
            }
            case RAINBOW -> {
                return hue(text, offset, 0.5F);
            }
            case ELOCIN -> {
                return gradient(text, offset, new Color(71, 119, 76), new Color(102, 69, 124));
            }
            case EMERALD -> {
                return gradient(text, offset, new Color(173, 250, 203), new Color(64, 240, 130));
            }
            case GOLD -> {
                return gradient(text, offset, new Color(230, 175, 21), new Color(250, 242, 94));
            }
        }

        return text.copy();
    }
}
