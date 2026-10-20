package pw.rxj.iron_quarry.render;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import pw.rxj.iron_quarry.event.WorldRenderCallback;

import java.awt.*;

public class WorldBlueprintMarkerRenderer extends BlueprintMarkerRenderer {
    private static final double MIN_MARKER_DEPTH = 1.0;

    @Override
    protected ScreenPos project(Vec3 worldPos) {
        return RenderUtil.cameraToScreen(worldPos, this.camera, this.projectionMatrix);
    }

    public void render(WorldRenderCallback.Context context) {
        Minecraft minecraftClient = Minecraft.getInstance();
        Font textRenderer = minecraftClient.font;
        MultiBufferSource.BufferSource bufferSource = minecraftClient.renderBuffers().bufferSource();

        for (ScreenPosData screenPosData : this.screenPosList) {
            ScreenPos screenPos = screenPosData.screenPos;
            if(screenPos.isBehind()) continue;
            double distance = screenPosData.distance;
            Vec2 texUV = screenPosData.texUV;
            float alpha = screenPosData.alpha;

            float scale = getScale(distance);
            PoseStack matrices = this.createMarkerMatrices(screenPosData);

            matrices.pushPose();

            matrices.scale(scale, scale, 1.0F);
            matrices.translate(-6.0F, -6.0F, 0.0F);

            RenderUtil.drawWorldTexture(matrices, BLUEPRINT_POSITIONS_TEXTURE, 0, 0, texUV.x, texUV.y, 13, 13, 36, 36, RenderUtil.whiteWithAlpha(alpha));

            matrices.popPose();

            //Add 1 to shift the center, because the texture had to be offset.
            if(screenPos.add(1, 1).distanceToCenter() <= 14 * scale) {
                matrices.pushPose();

                matrices.translate(0.0F, 9 * scale, 0.0F);

                MutableComponent text = Component.literal(String.format("%,.1fm", distance));
                int width = textRenderer.width(text);
                int height = 8;
                scale /= 1.5F;

                matrices.scale(scale, scale, 1.0F);
                matrices.translate(1 + width / -2.0F, 0.0F, 0.0F);

                RenderUtil.fillWorld(matrices, bufferSource, -2, -2, width + 1, height + 1, 1409286144);
                textRenderer.drawInBatch(text, 0, 0, new Color(1.0F, 1.0F, 1.0F, alpha).getRGB(), true, matrices.last().pose(), bufferSource, Font.DisplayMode.SEE_THROUGH, 0, LightTexture.FULL_BRIGHT);
                bufferSource.endBatch();

                matrices.popPose();
            }
        }
    }

    private PoseStack createMarkerMatrices(ScreenPosData screenPosData) {
        Minecraft minecraftClient = Minecraft.getInstance();
        Window window = minecraftClient.getWindow();

        double depth = screenPosData.screenPos.depth;
        double clampedDepth = Mth.clamp(depth, MIN_MARKER_DEPTH, minecraftClient.gameRenderer.getDepthFar() * 0.5);
        Vec3 markerPos = screenPosData.worldPos.subtract(this.camera.position()).scale(clampedDepth / depth);
        float pixelSize = (float) (window.getGuiScale() * 2.0 * clampedDepth / (window.getHeight() * this.projectionMatrix.m11()));

        PoseStack matrices = new PoseStack();
        matrices.translate(markerPos.x, markerPos.y, markerPos.z);
        matrices.mulPose(this.camera.rotation());
        matrices.scale(pixelSize, -pixelSize, pixelSize);

        return matrices;
    }
}
