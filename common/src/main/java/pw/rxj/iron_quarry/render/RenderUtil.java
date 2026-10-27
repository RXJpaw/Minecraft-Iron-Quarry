package pw.rxj.iron_quarry.render;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import pw.rxj.iron_quarry.Main;

public class RenderUtil {
    public static final int MAX_VIEW_DISTANCE = 32;

    private static class Lines {
        private static final RenderPipeline VISIBLE_LINES_PIPELINE = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(Main.MOD_ID, "pipeline/visible_lines"))
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN, true))
                .build());
        private static final RenderPipeline HIDDEN_LINES_PIPELINE = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(Main.MOD_ID, "pipeline/hidden_lines"))
                .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN, true))
                .build());
        private static final RenderType VISIBLE_LINES = RenderType.create("iron_quarry_visible_lines", RenderSetup.builder(VISIBLE_LINES_PIPELINE).bufferSize(1536).createRenderSetup());
        private static final RenderType HIDDEN_LINES = RenderType.create("iron_quarry_hidden_lines", RenderSetup.builder(HIDDEN_LINES_PIPELINE).bufferSize(1536).createRenderSetup());
        private static final float LINE_WIDTH = 3.0F;
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
                    .bufferSize(256)
                    .createRenderSetup()));
        }
    }

    private static class Previews {
        private static final RenderPipeline OCCLUDED_PREVIEW_PIPELINE = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(Main.MOD_ID, "pipeline/occluded_preview"))
                .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN, false))
                .withCull(true)
                .build());
        private static final RenderPipeline OVERLAY_PREVIEW_PIPELINE = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(Main.MOD_ID, "pipeline/overlay_preview"))
                .withCull(true)
                .build());
        private static final RenderType OCCLUDED_PREVIEW = RenderType.create("iron_quarry_occluded_preview", RenderSetup.builder(OCCLUDED_PREVIEW_PIPELINE)
                .withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS, () -> RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST, true))
                .bufferSize(1536)
                .createRenderSetup());
        private static final RenderType OVERLAY_PREVIEW = RenderType.create("iron_quarry_overlay_preview", RenderSetup.builder(OVERLAY_PREVIEW_PIPELINE)
                .withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS, () -> RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST, true))
                .bufferSize(1536)
                .createRenderSetup());
    }

    public static List<RenderPipeline> getLinePipelines() {
        return List.of(Lines.VISIBLE_LINES_PIPELINE, Lines.HIDDEN_LINES_PIPELINE);
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
    public static void drawSprite(GuiGraphicsExtractor context, Identifier sprite, int x, int y, int width, int height) {
        context.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height);
    }
    public static int whiteWithAlpha(double alpha) {
        int a = (int) Math.round(Mth.clamp(alpha, 0.0, 1.0) * 255.0);

        return (a << 24) | 0xFFFFFF;
    }

    public static Vec3 vec3dFrom(BlockPos blockPos) {
        return new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ()).add(0.5, 0.5, 0.5);
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

    public static void drawZBufferedCuboid(Cuboid cuboid, PoseStack matrices, @Nullable GpuBufferSlice fog, int viewDistance, float red, float green, float blue, float alpha) {
        double squaredViewDistance = Math.pow(viewDistance, 2);

        Matrix4f positionMatrix = matrices.last().pose();
        Tesselator tessellator = Tesselator.getInstance();

        //Outlines
        List<SpriteVec2f> boxLines = cuboid.getLines();
        List<SpriteVec2f> splitBoxLines = boxLines.stream().map(vec -> vec.autoSplit(8.0F)).flatMap(List::stream).toList();
        List<SpriteVec2f> filteredSplitBoxLines = splitBoxLines.stream().filter(vec -> vec.squaredDistanceTo(new Vector3f()) <= squaredViewDistance).toList();

        GpuBufferSlice shaderFog = RenderSystem.getShaderFog();
        if(fog != null) RenderSystem.setShaderFog(fog);

        try {
            //Visible Outlines
            drawLines(tessellator, Lines.VISIBLE_LINES, positionMatrix, matrices.last(), filteredSplitBoxLines, red, green, blue, alpha);

            //Hidden Outlines
            drawLines(tessellator, Lines.HIDDEN_LINES, positionMatrix, matrices.last(), filteredSplitBoxLines, red, green, blue, alpha * 0.2F);
        } finally {
            RenderSystem.setShaderFog(shaderFog);
        }
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

    public static void drawWorldTexture(PoseStack matrices, Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int color) {
        RenderType renderLayer = Markers.of(texture);
        Matrix4f positionMatrix = matrices.last().pose();

        float minU = u / textureWidth;
        float maxU = (u + width) / textureWidth;
        float minV = v / textureHeight;
        float maxV = (v + height) / textureHeight;

        BufferBuilder buffer = Tesselator.getInstance().begin(renderLayer.mode(), renderLayer.format());

        buffer.addVertex(positionMatrix, x, y, 0.0F).setUv(minU, minV).setColor(color);
        buffer.addVertex(positionMatrix, x, y + height, 0.0F).setUv(minU, maxV).setColor(color);
        buffer.addVertex(positionMatrix, x + width, y + height, 0.0F).setUv(maxU, maxV).setColor(color);
        buffer.addVertex(positionMatrix, x + width, y, 0.0F).setUv(maxU, minV).setColor(color);

        MeshData builtBuffer = buffer.build();
        if(builtBuffer != null) renderLayer.draw(builtBuffer);
    }

    public interface PreviewQuad {
        float x(int vertex);
        float y(int vertex);
        float z(int vertex);
        float u(int vertex);
        float v(int vertex);
        int color(int vertex);
        boolean diffuseShade();
        Direction lightFace();

        static PreviewQuad of(BakedQuad quad) {
            return new PreviewQuad() {
                @Override
                public float x(int vertex) {
                    return quad.position(vertex).x();
                }
                @Override
                public float y(int vertex) {
                    return quad.position(vertex).y();
                }
                @Override
                public float z(int vertex) {
                    return quad.position(vertex).z();
                }
                @Override
                public float u(int vertex) {
                    return UVPair.unpackU(quad.packedUV(vertex));
                }
                @Override
                public float v(int vertex) {
                    return UVPair.unpackV(quad.packedUV(vertex));
                }
                @Override
                public int color(int vertex) {
                    return -1;
                }
                @Override
                public boolean diffuseShade() {
                    return quad.materialInfo().shade();
                }
                @Override
                public Direction lightFace() {
                    return quad.direction();
                }
            };
        }
    }

    public static void drawPreviewMesh(PoseStack matrices, Iterable<? extends PreviewQuad> mesh, CardinalLighting cardinalLighting, float alpha, boolean occludedOnly) {
        RenderType renderLayer = occludedOnly ? Previews.OCCLUDED_PREVIEW : Previews.OVERLAY_PREVIEW;
        Matrix4f positionMatrix = matrices.last().pose();

        BufferBuilder buffer = Tesselator.getInstance().begin(renderLayer.mode(), renderLayer.format());

        mesh.forEach(quad -> {
            float shade = quad.diffuseShade() ? cardinalLighting.byFace(quad.lightFace()) : 1.0F;

            for (int vertex = 0; vertex < 4; vertex++) {
                buffer.addVertex(positionMatrix, quad.x(vertex), quad.y(vertex), quad.z(vertex)).setUv(quad.u(vertex), quad.v(vertex)).setColor(ARGB.multiplyAlpha(ARGB.scaleRGB(quad.color(vertex), shade), alpha));
            }
        });

        MeshData builtBuffer = buffer.build();
        if(builtBuffer != null) renderLayer.draw(builtBuffer);
    }

    public static void fillWorld(PoseStack matrices, MultiBufferSource.BufferSource bufferSource, int x1, int y1, int x2, int y2, int color) {
        RenderType renderLayer = RenderTypes.textBackgroundSeeThrough();
        Matrix4f positionMatrix = matrices.last().pose();
        VertexConsumer buffer = bufferSource.getBuffer(renderLayer);

        buffer.addVertex(positionMatrix, x1, y1, 0.0F).setColor(color).setLight(LightCoordsUtil.FULL_BRIGHT);
        buffer.addVertex(positionMatrix, x1, y2, 0.0F).setColor(color).setLight(LightCoordsUtil.FULL_BRIGHT);
        buffer.addVertex(positionMatrix, x2, y2, 0.0F).setColor(color).setLight(LightCoordsUtil.FULL_BRIGHT);
        buffer.addVertex(positionMatrix, x2, y1, 0.0F).setColor(color).setLight(LightCoordsUtil.FULL_BRIGHT);

        bufferSource.endBatch(renderLayer);
    }
}
