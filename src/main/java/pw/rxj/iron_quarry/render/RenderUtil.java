package pw.rxj.iron_quarry.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.Window;
import net.minecraft.util.math.*;

import java.util.HashSet;

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

    public static Vec3d minVec3d(Vec3d vec3d, double max) {
        return new Vec3d(Math.min(vec3d.x, max), Math.min(vec3d.y, max), Math.min(vec3d.z, max));
    }
    public static Vec3d maxVec3d(Vec3d vec3d, double min) {
        return new Vec3d(Math.max(vec3d.x, min), Math.max(vec3d.y, min), Math.max(vec3d.z, min));
    }
    public static Vec3d minMaxVec3d(Vec3d vec3d, double max, double min) {
        return minVec3d(maxVec3d(vec3d, min), max);
    }
    public static HashSet<Direction> limitedDirections(Vec3d lowest, Vec3d limitedLowest, Vec3d highest, Vec3d limitedHighest) {
        HashSet<Direction> directions = new HashSet<>();

        if(lowest.x != limitedLowest.x) directions.add(Direction.WEST);
        if(lowest.y != limitedLowest.y) directions.add(Direction.DOWN);
        if(lowest.z != limitedLowest.z) directions.add(Direction.NORTH);
        if(highest.x != limitedHighest.x) directions.add(Direction.EAST);
        if(highest.y != limitedHighest.y) directions.add(Direction.UP);
        if(highest.z != limitedHighest.z) directions.add(Direction.SOUTH);

        return directions;
    }

    public static SpriteVec anchorsFrom(Direction direction, Vec3f lowest, Vec3f highest) {
        Vec3f anchor_tl = null;
        Vec3f anchor_bl = null;
        Vec3f anchor_br = null;
        Vec3f anchor_tr = null;

        float minX = lowest.getX();
        float minY = lowest.getY();
        float minZ = lowest.getZ();

        float maxX = highest.getX();
        float maxY = highest.getY();
        float maxZ = highest.getZ();

        switch (direction) {
            case UP -> {
                anchor_tl = new Vec3f(maxX, maxY, maxZ); //top left
                anchor_bl = new Vec3f(maxX, maxY, minZ); //bottom left
                anchor_br = new Vec3f(minX, maxY, minZ); //bottom right
                anchor_tr = new Vec3f(minX, maxY, maxZ); //top right
            }
            case NORTH -> {
                anchor_tl = new Vec3f(maxX, maxY, minZ);
                anchor_bl = new Vec3f(maxX, minY, minZ);
                anchor_br = new Vec3f(minX, minY, minZ);
                anchor_tr = new Vec3f(minX, maxY, minZ);
            }
            case WEST -> {
                anchor_tl = new Vec3f(minX, maxY, minZ);
                anchor_bl = new Vec3f(minX, minY, minZ);
                anchor_br = new Vec3f(minX, minY, maxZ);
                anchor_tr = new Vec3f(minX, maxY, maxZ);
            }
            case SOUTH -> {
                anchor_tl = new Vec3f(minX, maxY, maxZ);
                anchor_bl = new Vec3f(minX, minY, maxZ);
                anchor_br = new Vec3f(maxX, minY, maxZ);
                anchor_tr = new Vec3f(maxX, maxY, maxZ);
            }
            case EAST -> {
                anchor_tl = new Vec3f(maxX, maxY, maxZ);
                anchor_bl = new Vec3f(maxX, minY, maxZ);
                anchor_br = new Vec3f(maxX, minY, minZ);
                anchor_tr = new Vec3f(maxX, maxY, minZ);
            }
            case DOWN -> {
                anchor_tl = new Vec3f(minX, minY, maxZ);
                anchor_bl = new Vec3f(minX, minY, minZ);
                anchor_br = new Vec3f(maxX, minY, minZ);
                anchor_tr = new Vec3f(maxX, minY, maxZ);
            }
        }

        return SpriteVec.from(anchor_tl, anchor_bl, anchor_br, anchor_tr);
    }
}
