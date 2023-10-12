package pw.rxj.iron_quarry.types;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.awt.*;

public enum DynamicText {
    RAINBOW(0, "rainbow");

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

    public MutableText getText(String name) {
        switch (this) {
            case RAINBOW -> {
                long time = System.currentTimeMillis() / 50L;

                MutableText rainbowName = Text.empty();

                for (int i = 0; i < name.length(); i++) {
                    double hue = 1.0/90.0 * (time - i);
                    Style style = Style.EMPTY.withColor(Color.HSBtoRGB((float) (hue % 360), 0.5F, 1.0F));

                    rainbowName.append(Text.literal(String.valueOf(name.charAt(i))).setStyle(style));
                }

                return rainbowName;
            }
        }

        return Text.literal(name);
    }
}
