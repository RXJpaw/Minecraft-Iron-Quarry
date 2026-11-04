package pw.rxj.iron_quarry.render;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import pw.rxj.iron_quarry.item.QuarryMonitorItem;
import pw.rxj.iron_quarry.screen.QuarryBlockScreen;
import pw.rxj.iron_quarry.util.MachineWarningsUtil;
import pw.rxj.iron_quarry.util.ZUtil;

public class QuarryMonitorWarningRenderer {
    public static void renderGuiItemOverlay(GuiGraphicsExtractor context, ItemStack stack, int x, int y) {
        if(!(ZUtil.getBlockOrItem(stack) instanceof QuarryMonitorItem quarryMonitorItem)) return;
        if(!quarryMonitorItem.hasEverCachedQuarry(stack)) return;

        renderIcon(context, MachineWarningsUtil.from(stack), x + 13, y);
    }

    public static void renderIcon(GuiGraphicsExtractor context, MachineWarningsUtil warningsUtil, int x, int y) {
        if(!warningsUtil.hasWarnings()) return;

        RenderUtil.drawTexture(context, QuarryBlockScreen.INVENTORY_LOCATION, x, y, warningsUtil.hasCriticalWarnings() ? 185 : 182, 5, 3, 9);
    }
}
