package pw.rxj.iron_quarry.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pw.rxj.iron_quarry.interfaces.IEnergyContainer;

public class EnergyBarRenderer {
    public static void onRenderGuiItemOverlay(TextRenderer renderer, ItemStack stack, int x, int y, @Nullable String countLabel, CallbackInfo ci) {
        if (getItemBarStep(stack) > 0) {
            RenderSystem.disableDepthTest();
            RenderSystem.disableTexture();
            RenderSystem.disableBlend();

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();

            int itemBarStep = (int) Math.ceil(getItemBarStep(stack));
            int itemBarColor = 0xB76CEA;

            renderGuiQuad(bufferBuilder, x + 2, y + 13, 13, 2, 0, 0, 0, 255);
            renderGuiQuad(bufferBuilder, x + 2, y + 13, itemBarStep, 1, itemBarColor >> 16 & 255, itemBarColor >> 8 & 255, itemBarColor & 255, 255);

            RenderSystem.enableBlend();
            RenderSystem.enableTexture();
            RenderSystem.enableDepthTest();
        }
    }

    private static void renderGuiQuad(BufferBuilder buffer, int x, int y, int width, int height, int red, int green, int blue, int alpha) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(x + 0.0D , y + 0.0D  , 0.0D).color(red, green, blue, alpha).next();
        buffer.vertex(x + 0.0D , y + height, 0.0D).color(red, green, blue, alpha).next();
        buffer.vertex(x + width, y + height, 0.0D).color(red, green, blue, alpha).next();
        buffer.vertex(x + width, y + 0.0D  , 0.0D).color(red, green, blue, alpha).next();

        BufferRenderer.drawWithShader(buffer.end());
    }

    public static float getItemBarStep(ItemStack stack) {
        IEnergyContainer energyContainer = IEnergyContainer.instanceOf(stack);
        if(energyContainer == null) return 0;

        long stored = energyContainer.getEnergyStored(stack);
        long capacity = energyContainer.getEnergyCapacity();

        return (float) Math.max(Math.min((double) stored / (double) capacity * 13.0F, 13.0F), 0.0F);
    }
}
