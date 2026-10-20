package pw.rxj.iron_quarry.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;
import pw.rxj.iron_quarry.Main;

public class RenderUtil {
    public static final int MAX_VIEW_DISTANCE = 32;

    private static class Lines {
        private static final RenderPipeline VISIBLE_LINES_PIPELINE = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(Main.MOD_ID, "pipeline/visible_lines"))
                .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
                .build());
        private static final RenderPipeline HIDDEN_LINES_PIPELINE = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(Main.MOD_ID, "pipeline/hidden_lines"))
                .withDepthTestFunction(DepthTestFunction.GREATER_DEPTH_TEST)
                .build());
        private static final RenderType VISIBLE_LINES = RenderType.create("iron_quarry_visible_lines", RenderSetup.builder(VISIBLE_LINES_PIPELINE).bufferSize(1536).createRenderSetup());
        private static final RenderType HIDDEN_LINES = RenderType.create("iron_quarry_hidden_lines", RenderSetup.builder(HIDDEN_LINES_PIPELINE).bufferSize(1536).createRenderSetup());
        private static final float LINE_WIDTH = 3.0F;
    }

    public static void runScissored(GuiGraphics context, int x, int y, int width, int height, Runnable runnable) {
        context.enableScissor(x, y, x + width, y + height);
        runnable.run();
        context.disableScissor();
    }

    public static void drawTexture(GuiGraphics context, Identifier texture, int x, int y, float u, float v, int width, int height) {
        drawTexture(context, texture, x, y, u, v, width, height, 256, 256);
    }
    public static void drawTexture(GuiGraphics context, Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        context.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, textureWidth, textureHeight);
    }
    public static void drawTexture(GuiGraphics context, Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int color) {
        context.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, textureWidth, textureHeight, color);
    }
    public static int whiteWithAlpha(double alpha) {
        int a = (int) Math.round(Mth.clamp(alpha, 0.0, 1.0) * 255.0);

        return (a << 24) | 0xFFFFFF;
    }

    public static Vec3 vec3dFrom(BlockPos blockPos) {
        return new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ()).add(0.5, 0.5, 0.5);
    }

    public static ScreenPos worldToScreen(Vec3 worldPos, Matrix4f positionMatrix, Matrix4f projectionMatrix) {
        Minecraft minecraftClient = Minecraft.getInstance();
        Camera camera = minecraftClient.gameRenderer.getMainCamera();
        Window window = minecraftClient.getWindow();

        Vector4f relativeWorldPos = new Vector4f(camera.position().reverse().add(worldPos).toVector3f(), 1.0F);

        relativeWorldPos.mul(positionMatrix);
        relativeWorldPos.mul(projectionMatrix);

        float depth = relativeWorldPos.w;
        if (depth != 0) relativeWorldPos.div(depth);

        float screenX = (float) window.getWidth() / window.getGuiScale() * (0.5F + relativeWorldPos.x * 0.5F);
        float screenY = (float) window.getHeight() / window.getGuiScale() * (0.5F - relativeWorldPos.y * 0.5F);

        return ScreenPos.from(screenX, screenY, depth);
    }

    public static Vec3 minVec3d(Vec3 vec3d, double max) {
        return new Vec3(Math.min(vec3d.x, max), Math.min(vec3d.y, max), Math.min(vec3d.z, max));
    }
    public static Vec3 maxVec3d(Vec3 vec3d, double min) {
        return new Vec3(Math.max(vec3d.x, min), Math.max(vec3d.y, min), Math.max(vec3d.z, min));
    }
    public static Vec3 minMaxVec3d(Vec3 vec3d, double max, double min) {
        return minVec3d(maxVec3d(vec3d, min), max);
    }
    public static boolean isOutsideRange(Vec3 pos, double range) {
        return pos.x < -range || pos.y < -range || pos.z < -range ||
               pos.x >  range || pos.y >  range || pos.z >  range;
    }

    public static void drawZBufferedCuboid(Cuboid cuboid, PoseStack matrices, int viewDistance, float red, float green, float blue, float alpha) {
        double squaredViewDistance = Math.pow(viewDistance, 2);

        Matrix4f positionMatrix = matrices.last().pose();
        Tesselator tessellator = Tesselator.getInstance();

        //Outlines
        List<SpriteVec2f> boxLines = cuboid.getLines();
        List<SpriteVec2f> splitBoxLines = boxLines.stream().map(vec -> vec.autoSplit(8.0F)).flatMap(List::stream).toList();
        List<SpriteVec2f> filteredSplitBoxLines = splitBoxLines.stream().filter(vec -> vec.squaredDistanceTo(new Vector3f()) <= squaredViewDistance).toList();

        //Visible Outlines
        drawLines(tessellator, Lines.VISIBLE_LINES, positionMatrix, matrices.last(), filteredSplitBoxLines, red, green, blue, alpha);

        //Hidden Outlines
        drawLines(tessellator, Lines.HIDDEN_LINES, positionMatrix, matrices.last(), filteredSplitBoxLines, red, green, blue, alpha * 0.2F);
    }

    private static void drawLines(Tesselator tessellator, RenderType renderLayer, Matrix4f positionMatrix, PoseStack.Pose entry, List<SpriteVec2f> lines, float red, float green, float blue, float alpha) {
        BufferBuilder buffer = tessellator.begin(renderLayer.mode(), renderLayer.format());

        for (SpriteVec2f line : lines) {
            Vector3f n = line.normalize();

            buffer.addVertex(positionMatrix, line.from.x, line.from.y, line.from.z).setColor(red, green, blue, alpha).setNormal(entry, n.x, n.y, n.z).setLineWidth(Lines.LINE_WIDTH);
            buffer.addVertex(positionMatrix, line.to.x, line.to.y, line.to.z).setColor(red, green, blue, alpha).setNormal(entry, n.x, n.y, n.z).setLineWidth(Lines.LINE_WIDTH);
        }

        MeshData builtBuffer = buffer.build();
        if(builtBuffer != null) renderLayer.draw(builtBuffer);
    }
}
