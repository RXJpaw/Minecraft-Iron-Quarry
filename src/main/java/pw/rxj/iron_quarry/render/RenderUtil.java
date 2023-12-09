package pw.rxj.iron_quarry.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.Window;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vector4f;

public class RenderUtil {
    public static void enableScaledScissor(int x, int y, int width, int height) {
        int scaleFactor = (int) MinecraftClient.getInstance().getWindow().getScaleFactor();

        RenderSystem.enableScissor(x * scaleFactor, MinecraftClient.getInstance().getWindow().getFramebufferHeight() - (y + height) * scaleFactor, width * scaleFactor, height * scaleFactor);
    }
    public static void disableScaledScissor() {
        RenderSystem.disableScissor();
    }
    public static void runScissored(int x, int y, int width, int height, Runnable runnable) {
        enableScaledScissor(x, y, width, height);
        runnable.run();
        disableScaledScissor();
    }

    public static ScreenPos worldToScreen(Vec3d worldPos, Matrix4f positionMatrix, Matrix4f projectionMatrix) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        Camera camera = minecraftClient.gameRenderer.getCamera();
        Window window = minecraftClient.getWindow();

        Vector4f relativeWorldPos = new Vector4f(new Vec3f(camera.getPos().negate().add(worldPos)));
        relativeWorldPos.transform(positionMatrix);
        relativeWorldPos.transform(projectionMatrix);

        var depth = relativeWorldPos.getW();
        if (depth != 0) relativeWorldPos.normalizeProjectiveCoordinates();

        float screenX = window.getScaledWidth() * (0.5F + relativeWorldPos.getX() * 0.5F);
        float screenY = window.getScaledHeight() * (0.5F - relativeWorldPos.getY() * 0.5F);

        return ScreenPos.from(screenX, screenY, depth);
    }
}
