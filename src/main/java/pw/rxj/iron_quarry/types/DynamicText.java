package pw.rxj.iron_quarry.types;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import pw.rxj.iron_quarry.util.ColorGradient;

import java.awt.*;

public enum DynamicText {
    EMPTY(0, "empty"),
    RAINBOW(1, "rainbow"),
    ELOCIN(2, "elocin");

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

    public MutableText getText(Text text, int offset) {
        long time = System.currentTimeMillis() / 50L;
        String string = text.getString();

        switch (this) {
            case EMPTY -> {
                return text.copy();
            }

            case RAINBOW -> {
                MutableText rainbowName = Text.empty();

                for (int i = 0; i < string.length(); i++) {
                    double hue = 1.0/90.0 * (time - i - offset);
                    Style style = Style.EMPTY.withColor(Color.HSBtoRGB((float) (hue % 360), 0.5F, 1.0F));

                    rainbowName.append(Text.literal(String.valueOf(string.charAt(i))).setStyle(style));
                }

                return rainbowName;
            }

            case ELOCIN -> {
                MutableText rainbowName = Text.empty();
                ColorGradient gradient = new ColorGradient(new Color(71, 119, 76), new Color(102, 69, 124));

                for (int i = 0; i < string.length(); i++) {
                    double hue = (time - i - offset) % 45.0;
                    Style style = Style.EMPTY.withColor(gradient.getColor((float) hue / 22.5F).getRGB());

                    rainbowName.append(Text.literal(String.valueOf(string.charAt(i))).setStyle(style));
                }

                return rainbowName;
            }
        }

        return text.copy();
    }
}
