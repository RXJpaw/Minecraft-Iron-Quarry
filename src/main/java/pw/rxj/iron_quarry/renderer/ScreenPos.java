package pw.rxj.iron_quarry.renderer;

import net.minecraft.util.math.Vector4f;

public class ScreenPos {
    private final float x;
    private final float y;
    private final float depth;

    private ScreenPos(float x, float y, float depth) {
        this.x = x;
        this.y = y;
        this.depth = depth;
    }

    public static ScreenPos from(float x, float y, float depth) {
        return new ScreenPos(x, y, depth);
    }
    public static ScreenPos of(Vector4f transformed) {
        return from(transformed.getX(), transformed.getX(), transformed.getW());
    }

    public float getX() {
        return this.x;
    }
    public float getY() {
        return this.y;
    }
    public float getDepth() {
        return this.depth;
    }

    public boolean isAhead() {
        return this.getDepth() >= 0;
    }
    public boolean isBehind() {
        return this.getDepth() < 0;
    }
}
