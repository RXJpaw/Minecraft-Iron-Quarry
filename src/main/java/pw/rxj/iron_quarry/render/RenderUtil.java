package pw.rxj.iron_quarry.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class RenderUtil {
    public static final int MAX_VIEW_DISTANCE = 32;

    public static void runScissored(DrawContext context, int x, int y, int width, int height, Runnable runnable) {
        context.enableScissor(x, y, x + width, y + height);
        runnable.run();
        context.disableScissor();
    }

    public static void drawTexture(DrawContext context, Identifier texture, int x, int y, float u, float v, int width, int height) {
        drawTexture(context, texture, x, y, u, v, width, height, 256, 256);
    }
    public static void drawTexture(DrawContext context, Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        context.drawTexture(RenderLayer::getGuiTextured, texture, x, y, u, v, width, height, textureWidth, textureHeight);
    }
    public static void drawTexture(DrawContext context, Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int color) {
        context.drawTexture(RenderLayer::getGuiTextured, texture, x, y, u, v, width, height, textureWidth, textureHeight, color);
    }
    public static int whiteWithAlpha(double alpha) {
        int a = (int) Math.round(MathHelper.clamp(alpha, 0.0, 1.0) * 255.0);

        return (a << 24) | 0xFFFFFF;
    }

    public static Vec3d vec3dFrom(BlockPos blockPos) {
        return new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()).add(0.5, 0.5, 0.5);
    }

    public static ScreenPos worldToScreen(Vec3d worldPos, Matrix4f positionMatrix, Matrix4f projectionMatrix) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        Camera camera = minecraftClient.gameRenderer.getCamera();
        Window window = minecraftClient.getWindow();

        Vector4f relativeWorldPos = new Vector4f(camera.getPos().negate().add(worldPos).toVector3f(), 1.0F);

        relativeWorldPos.mul(positionMatrix);
        relativeWorldPos.mul(projectionMatrix);

        float depth = relativeWorldPos.w;
        if (depth != 0) relativeWorldPos.div(depth);

        float screenX = window.getScaledWidth() * (0.5F + relativeWorldPos.x * 0.5F);
        float screenY = window.getScaledHeight() * (0.5F - relativeWorldPos.y * 0.5F);

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

    public static void drawZBufferedCuboid(Cuboid cuboid, MatrixStack matrices, int viewDistance) {
        double squaredViewDistance = Math.pow(viewDistance, 2);

        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();

        RenderSystem.defaultBlendFunc();

        //Outlines
        RenderSystem.setShader(ShaderProgramKeys.RENDERTYPE_LINES);
        RenderSystem.lineWidth(3.0F);
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();

        List<SpriteVec2f> boxLines = cuboid.getLines();
        List<SpriteVec2f> splitBoxLines = boxLines.stream().map(vec -> vec.autoSplit(8.0F)).flatMap(List::stream).toList();
        List<SpriteVec2f> filteredSplitBoxLines = splitBoxLines.stream().filter(vec -> vec.squaredDistanceTo(new Vector3f()) <= squaredViewDistance).toList();

        //Visible Outlines
        RenderSystem.depthFunc(GL11.GL_LESS);
        drawLines(tessellator, positionMatrix, matrices.peek(), filteredSplitBoxLines, 1.0F);

        //Hidden Outlines
        RenderSystem.depthFunc(GL11.GL_GREATER);
        drawLines(tessellator, positionMatrix, matrices.peek(), filteredSplitBoxLines, 0.2F);

        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.lineWidth(1.0F);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
    }

    private static void drawLines(Tessellator tessellator, Matrix4f positionMatrix, MatrixStack.Entry entry, List<SpriteVec2f> lines, float alpha) {
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);

        for (SpriteVec2f line : lines) {
            Vector3f n = line.normalize();

            buffer.vertex(positionMatrix, line.from.x, line.from.y, line.from.z).color(1.0F, 1.0F, 1.0F, alpha).normal(entry, n.x, n.y, n.z);
            buffer.vertex(positionMatrix, line.to.x, line.to.y, line.to.z).color(1.0F, 1.0F, 1.0F, alpha).normal(entry, n.x, n.y, n.z);
        }

        BuiltBuffer builtBuffer = buffer.endNullable();
        if(builtBuffer != null) BufferRenderer.drawWithGlobalProgram(builtBuffer);
    }
}
