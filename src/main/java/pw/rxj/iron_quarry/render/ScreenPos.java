package pw.rxj.iron_quarry.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import net.minecraft.util.math.Vector4f;

public class ScreenPos {
    public final float x;
    public final float y;
    public final float depth;

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

    public boolean isAhead() {
        return this.depth > 0;
    }
    public boolean isBehind() {
        return this.depth <= 0;
    }

    public double distanceToCenter() {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        Window window = minecraftClient.getWindow();

        float x = window.getScaledWidth() / 2.0F;
        float y = window.getScaledHeight() / 2.0F;

        return this.distanceTo(ScreenPos.from(x, y, 1.0F));
    }
    public double distanceTo(ScreenPos other) {
        float x = this.x - other.x;
        float y = this.y - other.y;

        return Math.sqrt(x * x + y * y);
    }

    @Override
    public String toString() {
        return String.format("ScreenPos{x: %s, y: %s, W: %s}", this.x, this.y, this.depth);
    }
}
