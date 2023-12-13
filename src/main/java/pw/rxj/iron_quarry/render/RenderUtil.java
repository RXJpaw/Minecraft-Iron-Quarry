package pw.rxj.iron_quarry.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.Window;
import net.minecraft.util.math.*;

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

    public static Vec3d vec3dFrom(BlockPos blockPos) {
        return new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()).add(0.5, 0.5, 0.5);
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

    public static Vec3d minVec3d(Vec3d vec3d, double max) {
        return new Vec3d(Math.min(vec3d.x, max), Math.min(vec3d.y, max), Math.min(vec3d.z, max));
    }
    public static Vec3d maxVec3d(Vec3d vec3d, double min) {
        return new Vec3d(Math.max(vec3d.x, min), Math.max(vec3d.y, min), Math.max(vec3d.z, min));
    }
    public static Vec3d minMaxVec3d(Vec3d vec3d, double max, double min) {
        return minVec3d(maxVec3d(vec3d, min), max);
    }
    public static boolean isOutsideRange(Vec3d pos, double range) {
        return pos.x < -range || pos.y < -range || pos.z < -range ||
               pos.x >  range || pos.y >  range || pos.z >  range;
    }
}
