package pw.rxj.iron_quarry.types;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import pw.rxj.iron_quarry.util.ColorGradient;

import java.awt.*;

public enum DynamicText {
    EMPTY(0, "empty"),
    RAINBOW(1, "rainbow"),
    ELOCIN(2, "elocin"),
    EMERALD(3, "emerald");

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


    public MutableText getText(Text text) {
        return this.getText(text, 0);
    }

    private static MutableText gradient(Text text, int offset, Color fromColor, Color toColor) {
        long time = System.currentTimeMillis() / 50L;
        String string = text.getString();

        MutableText name = Text.empty();
        ColorGradient gradient = new ColorGradient(fromColor, toColor);

        for (int i = 0; i < string.length(); i++) {
            double progress = (time - i - offset) % 45.0;
            Style style = Style.EMPTY.withColor(gradient.getColor((float) progress / 22.5F).getRGB());

            name.append(Text.literal(String.valueOf(string.charAt(i))).setStyle(style));
        }

        return name;
    }
    private static MutableText hue(Text text, int offset, float saturation) {
        long time = System.currentTimeMillis() / 50L;
        String string = text.getString();

        MutableText name = Text.empty();

        for (int i = 0; i < string.length(); i++) {
            double hue = 1.0/90.0 * (time - i - offset);
            Style style = Style.EMPTY.withColor(Color.HSBtoRGB((float) (hue % 360), saturation, 1.0F));

            name.append(Text.literal(String.valueOf(string.charAt(i))).setStyle(style));
        }

        return name;
    }


    public MutableText getText(Text text, int offset) {
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
        }

        return text.copy();
    }
}
