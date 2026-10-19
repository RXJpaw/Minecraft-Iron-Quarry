package pw.rxj.iron_quarry.render;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import pw.rxj.iron_quarry.interfaces.IEnergyContainer;

public class EnergyBarRenderer {
    public static void renderGuiItemOverlay(DrawContext context, ItemStack stack, int x, int y) {
        if (getItemBarStep(stack) > 0) {
            int step = (int) Math.ceil(getItemBarStep(stack));
            int color = 0xB76CEA;
            int fillX = x + 2;
            int fillY = y + 13;

            context.fill(RenderPipelines.GUI, fillX, fillY, fillX + 13,   fillY + 2, -16777216);
            context.fill(RenderPipelines.GUI, fillX, fillY, fillX + step, fillY + 1, color | -16777216);
        }
    }

    public static float getItemBarStep(ItemStack stack) {
        IEnergyContainer energyContainer = IEnergyContainer.instanceOf(stack);
        if(energyContainer == null) return 0;

        long stored = energyContainer.getEnergyStored(stack);
        long capacity = energyContainer.getEnergyCapacity();

        return (float) Math.max(Math.min((double) stored / (double) capacity * 13.0F, 13.0F), 0.0F);
    }
}
