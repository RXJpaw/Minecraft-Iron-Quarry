package pw.rxj.iron_quarry.util;

public class Color {
    public float r;
    public float g;
    public float b;

    public Color(float r, float g, float b){
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public void darken(float amount) {
        r -= amount;
        g -= amount;
        b -= amount;
    }

    public void lighten(float amount) {
        r += amount;
        g += amount;
        b += amount;
    }
}
