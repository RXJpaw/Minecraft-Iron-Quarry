package pw.rxj.iron_quarry.util;

import java.awt.Color;

public class ColorGradient {
    private final Color start;
    private final Color end;

    public ColorGradient(Color start, Color end) {
        this.start = start;
        this.end = end;
    }

    public Color getColor(float progression) {
        progression = (float) ZUtil.bounceBack(progression, 1);

        int red = (int) (start.getRed() + progression * (end.getRed() - start.getRed()));
        int green = (int) (start.getGreen() + progression * (end.getGreen() - start.getGreen()));
        int blue = (int) (start.getBlue() + progression * (end.getBlue() - start.getBlue()));

        return new Color(red, green, blue);
    }
}
