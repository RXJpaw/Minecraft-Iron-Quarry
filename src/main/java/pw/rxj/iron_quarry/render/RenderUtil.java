package pw.rxj.iron_quarry.render;

import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import pw.rxj.iron_quarry.Main;

public class RenderUtil {
    public static final int MAX_VIEW_DISTANCE = 32;

    private static class Lines {
        private static final RenderPipeline VISIBLE_LINES_PIPELINE = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(Main.MOD_ID, "pipeline/visible_lines"))
                .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN, true))
                .build());
        private static final RenderPipeline HIDDEN_LINES_PIPELINE = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(Main.MOD_ID, "pipeline/hidden_lines"))
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN, false))
                .build());
        private static final RenderType VISIBLE_LINES = RenderType.create("iron_quarry_visible_lines", RenderSetup.builder(VISIBLE_LINES_PIPELINE).createRenderSetup());
        private static final RenderType HIDDEN_LINES = RenderType.create("iron_quarry_hidden_lines", RenderSetup.builder(HIDDEN_LINES_PIPELINE).createRenderSetup());
        private static final float LINE_WIDTH = 3.0F;
        private static final float MIN_DEPTH = 0.05F;
    }

    private static class Markers {
        private static final RenderPipeline WORLD_MARKER_PIPELINE = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(Main.MOD_ID, "pipeline/world_marker"))
                .withCull(false)
                .build());
        private static final Map<Identifier, RenderType> WORLD_MARKERS = new HashMap<>();

        private static RenderType of(Identifier texture) {
            return WORLD_MARKERS.computeIfAbsent(texture, id -> RenderType.create("iron_quarry_world_marker", RenderSetup.builder(WORLD_MARKER_PIPELINE)
                    .withTexture("Sampler0", id, () -> RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST))
                    .createRenderSetup()));
        }
    }

    public static class RenderStates {
        public static final RenderStateDataKey<Matrix4f> PROJECTION_MATRIX = RenderStateDataKey.create(() -> Main.MOD_ID + ":projection_matrix");
    }

    public static void runScissored(GuiGraphicsExtractor context, int x, int y, int width, int height, Runnable runnable) {
        context.enableScissor(x, y, x + width, y + height);
        runnable.run();
        context.disableScissor();
    }

    public static void drawTexture(GuiGraphicsExtractor context, Identifier texture, int x, int y, float u, float v, int width, int height) {
        drawTexture(context, texture, x, y, u, v, width, height, 256, 256);
    }
    public static void drawTexture(GuiGraphicsExtractor context, Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        context.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, textureWidth, textureHeight);
    }
    public static void drawTexture(GuiGraphicsExtractor context, Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int color) {
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
        Camera camera = minecraftClient.gameRenderer.mainCamera();
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

    public static ScreenPos cameraToScreen(Vec3 worldPos, Camera camera, Matrix4f projectionMatrix) {
        Minecraft minecraftClient = Minecraft.getInstance();
        Window window = minecraftClient.getWindow();

        Vec3 relativeWorldPos = worldPos.subtract(camera.position());
        Vector3fc forward = camera.forwardVector();
        Vector3fc left = camera.leftVector();
        Vector3fc up = camera.upVector();

        float depth = (float) (relativeWorldPos.x * forward.x() + relativeWorldPos.y * forward.y() + relativeWorldPos.z * forward.z());
        float x = (float) -(relativeWorldPos.x * left.x() + relativeWorldPos.y * left.y() + relativeWorldPos.z * left.z());
        float y = (float) (relativeWorldPos.x * up.x() + relativeWorldPos.y * up.y() + relativeWorldPos.z * up.z());

        if (depth != 0) {
            x /= depth;
            y /= depth;
        }

        float screenX = (float) window.getWidth() / window.getGuiScale() * (0.5F + x * projectionMatrix.m00() * 0.5F);
        float screenY = (float) window.getHeight() / window.getGuiScale() * (0.5F - y * projectionMatrix.m11() * 0.5F);

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

    public static void drawZBufferedCuboid(OrderedSubmitNodeCollector submitNodeCollector, Cuboid cuboid, PoseStack matrices, Vector3fc cameraForward, int viewDistance, float red, float green, float blue, float alpha) {
        double squaredViewDistance = Math.pow(viewDistance, 2);

        //Outlines
        List<SpriteVec2f> boxLines = cuboid.getLines();
        List<SpriteVec2f> splitBoxLines = boxLines.stream().map(vec -> vec.autoSplit(8.0F)).flatMap(List::stream).toList();
        List<SpriteVec2f> filteredSplitBoxLines = splitBoxLines.stream().filter(vec -> vec.squaredDistanceTo(new Vector3f()) <= squaredViewDistance)
                .map(vec -> clipBehindCamera(vec, matrices.last().pose(), cameraForward)).filter(Objects::nonNull).toList();

        //Visible Outlines
        drawLines(submitNodeCollector, Lines.VISIBLE_LINES, matrices, filteredSplitBoxLines, red, green, blue, alpha);

        //Hidden Outlines
        drawLines(submitNodeCollector, Lines.HIDDEN_LINES, matrices, filteredSplitBoxLines, red, green, blue, alpha * 0.2F);
    }

    private static @Nullable SpriteVec2f clipBehindCamera(SpriteVec2f line, Matrix4f positionMatrix, Vector3fc cameraForward) {
        float fromDepth = positionMatrix.transformPosition(new Vector3f(line.from)).dot(cameraForward);
        float toDepth = positionMatrix.transformPosition(new Vector3f(line.to)).dot(cameraForward);
        if(fromDepth < Lines.MIN_DEPTH && toDepth < Lines.MIN_DEPTH) return null;

        Vector3f from = new Vector3f(line.from);
        Vector3f to = new Vector3f(line.to);
        if(fromDepth < Lines.MIN_DEPTH) from.lerp(line.to, (Lines.MIN_DEPTH - fromDepth) / (toDepth - fromDepth));
        if(toDepth < Lines.MIN_DEPTH) to.lerp(line.from, (Lines.MIN_DEPTH - toDepth) / (fromDepth - toDepth));

        SpriteVec2f clippedLine = SpriteVec2f.from(from, to);

        return toDepth < fromDepth ? clippedLine.swap() : clippedLine;
    }

    private static void drawLines(OrderedSubmitNodeCollector submitNodeCollector, RenderType renderLayer, PoseStack matrices, List<SpriteVec2f> lines, float red, float green, float blue, float alpha) {
        if(lines.isEmpty()) return;

        submitNodeCollector.submitCustomGeometry(matrices, renderLayer, (entry, buffer) -> {
            Matrix4f positionMatrix = entry.pose();

            for (SpriteVec2f line : lines) {
                Vector3f n = line.normalize();

                buffer.addVertex(positionMatrix, line.from.x, line.from.y, line.from.z).setColor(red, green, blue, alpha).setNormal(entry, n.x, n.y, n.z).setLineWidth(Lines.LINE_WIDTH);
                buffer.addVertex(positionMatrix, line.to.x, line.to.y, line.to.z).setColor(red, green, blue, alpha).setNormal(entry, n.x, n.y, n.z).setLineWidth(Lines.LINE_WIDTH);
            }
        });
    }

    public static void drawWorldTexture(OrderedSubmitNodeCollector submitNodeCollector, PoseStack matrices, Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int color) {
        float minU = u / textureWidth;
        float maxU = (u + width) / textureWidth;
        float minV = v / textureHeight;
        float maxV = (v + height) / textureHeight;

        submitNodeCollector.submitCustomGeometry(matrices, Markers.of(texture), (entry, buffer) -> {
            Matrix4f positionMatrix = entry.pose();

            buffer.addVertex(positionMatrix, x, y, 0.0F).setUv(minU, minV).setColor(color);
            buffer.addVertex(positionMatrix, x, y + height, 0.0F).setUv(minU, maxV).setColor(color);
            buffer.addVertex(positionMatrix, x + width, y + height, 0.0F).setUv(maxU, maxV).setColor(color);
            buffer.addVertex(positionMatrix, x + width, y, 0.0F).setUv(maxU, minV).setColor(color);
        });
    }

    public static void fillWorld(OrderedSubmitNodeCollector submitNodeCollector, PoseStack matrices, int x1, int y1, int x2, int y2, int color) {
        submitNodeCollector.submitCustomGeometry(matrices, RenderTypes.textBackgroundSeeThrough(), (entry, buffer) -> {
            Matrix4f positionMatrix = entry.pose();

            buffer.addVertex(positionMatrix, x1, y1, 0.0F).setColor(color).setLight(LightCoordsUtil.FULL_BRIGHT);
            buffer.addVertex(positionMatrix, x1, y2, 0.0F).setColor(color).setLight(LightCoordsUtil.FULL_BRIGHT);
            buffer.addVertex(positionMatrix, x2, y2, 0.0F).setColor(color).setLight(LightCoordsUtil.FULL_BRIGHT);
            buffer.addVertex(positionMatrix, x2, y1, 0.0F).setColor(color).setLight(LightCoordsUtil.FULL_BRIGHT);
        });
    }
}
