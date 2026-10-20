package pw.rxj.iron_quarry.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3x2fStack;

import java.awt.*;

public class HudBlueprintMarkerRenderer extends BlueprintMarkerRenderer {
    @Override
    protected ScreenPos project(Vec3 worldPos) {
        return RenderUtil.worldToScreen(worldPos, this.positionMatrix, this.projectionMatrix);
    }

    public void render(GuiGraphics context, double tickDelta) {
        Minecraft minecraftClient = Minecraft.getInstance();
        Font textRenderer = minecraftClient.font;
        Matrix3x2fStack matrices = context.pose();

        for (ScreenPosData screenPosData : this.screenPosList) {
            ScreenPos screenPos = screenPosData.screenPos;
            if(screenPos.isBehind()) continue;
            double distance = screenPosData.distance;
            Vec2 texUV = screenPosData.texUV;
            float alpha = screenPosData.alpha;

            float scale = getScale(distance);

            matrices.pushMatrix();

            matrices.translate(screenPos.x, screenPos.y);
            matrices.scale(scale, scale);
            matrices.translate(-6, -6);

            RenderUtil.drawTexture(context, BLUEPRINT_POSITIONS_TEXTURE, 0, 0, texUV.x, texUV.y, 13, 13, 36, 36, RenderUtil.whiteWithAlpha(alpha));

            matrices.popMatrix();

            //Add 1 to shift the center, because the texture had to be offset.
            if(screenPos.add(1, 1).distanceToCenter() <= 14 * scale) {
                matrices.pushMatrix();

                matrices.translate(screenPos.x, screenPos.y + 9 * scale);

                MutableComponent text = Component.literal(String.format("%,.1fm", distance));
                int width = textRenderer.width(text);
                int height = 8;
                scale /= 1.5F;

                matrices.scale(scale, scale);
                matrices.translate(1 + width / -2.0F, 0.0F);

                context.fill(-2, -2, width + 1, height + 1, 1409286144);
                context.drawString(textRenderer, text, 0, 0, new Color(1.0F, 1.0F, 1.0F, alpha).getRGB(), true);

                matrices.popMatrix();
            }
        }
    }
}
