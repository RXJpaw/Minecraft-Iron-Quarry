package pw.rxj.iron_quarry.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.event.InGameHudRenderCallback;
import pw.rxj.iron_quarry.item.BlueprintItem;
import pw.rxj.iron_quarry.item.DrillItem;
import pw.rxj.iron_quarry.item.QuarryMonitorItem;
import pw.rxj.iron_quarry.resource.ResourceReloadListener;
import pw.rxj.iron_quarry.resource.config.client.QuarryMonitorOverlayConfig;
import pw.rxj.iron_quarry.util.ZUtil;

public class QuarryMonitorOverlayRenderer {
    private static final QuarryMonitorOverlayConfig.Handler QUARRY_MONITOR_OVERLAY_CONFIG = Main.CONFIG.getQuarryMonitorOverlayConfig();
    public static final Identifier OVERLAY_TEXTURE = Identifier.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/quarry_monitor_overlay.png");

    private static void renderOnScreen(GuiGraphicsExtractor context, double tickDelta) {
        Minecraft minecraftClient = Minecraft.getInstance();
        if(minecraftClient.level == null) return;
        LocalPlayer player = minecraftClient.player;
        if(player == null) return;

        Inventory inventory = player.getInventory();

        QuarryMonitorItem quarryMonitorItem = null;
        ItemStack quarryMonitorStack = null;

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);

            if(ZUtil.getBlockOrItem(stack) instanceof QuarryMonitorItem item) {
                quarryMonitorStack = stack;
                quarryMonitorItem = item;
                break;
            }
        }

        if(quarryMonitorStack == null) return;
        if(!quarryMonitorItem.exists(quarryMonitorStack)) return;

        float energyPct = quarryMonitorItem.getCachedEnergyPct(quarryMonitorStack);
        float outputInvPct = quarryMonitorItem.getCachedOutputInvPct(quarryMonitorStack);
        ItemStack blueprintStack = quarryMonitorItem.getCachedBlueprintStack(quarryMonitorStack);
        ItemStack drillStack = quarryMonitorItem.getCachedDrillStack(quarryMonitorStack);

        int x = QUARRY_MONITOR_OVERLAY_CONFIG.getComputedWindowX(minecraftClient.getWindow(), 42);
        int y = QUARRY_MONITOR_OVERLAY_CONFIG.getComputedWindowY(minecraftClient.getWindow(), 45);

        //texture render

        Matrix3x2fStack matrices = context.pose();

        matrices.pushMatrix();
        matrices.translate(x, y);

        RenderUtil.drawTexture(context, OVERLAY_TEXTURE, 0, 0, 0, 0, 42, 45, 128, 128);

        if(energyPct > 0.0F) {
            int chargedPixels = Math.max(0, Math.min(39, (int) (energyPct * 39)));
            RenderUtil.drawTexture(context, OVERLAY_TEXTURE, 3, 3 + 39 - chargedPixels, 42, 3 + 39 - chargedPixels, 5, chargedPixels, 128, 128);
        }
        if(outputInvPct > 0.0F) {
            int chargedPixels = Math.max(0, Math.min(39, (int) (outputInvPct * 39)));
            RenderUtil.drawTexture(context, OVERLAY_TEXTURE, 34, 3 + 39 - chargedPixels, 47, 3 + 39 - chargedPixels, 5, chargedPixels, 128, 128);
        }

        matrices.popMatrix();

        //item render
        //Renders over subtitles. In versions prior to 1.20 this could be fixed by messing with the ModelViewStack,
        //but since 1.20 DrawContext#fill (f.e in BlueprintPreviewRenderer) manipulates the way
        //DrawContext#drawItem works, which could potentially mess with other mods.

        Font textRenderer = minecraftClient.font;

        if(blueprintStack.getItem() instanceof BlueprintItem) {
            context.item(blueprintStack, x + 13, y + 4);
            context.itemDecorations(textRenderer, blueprintStack, x + 13, y + 4);
        }
        if(drillStack.getItem() instanceof DrillItem) {
            context.item(drillStack, x + 13, y + 25);
            context.itemDecorations(textRenderer, drillStack, x + 13, y + 25);
        }
    }

    public static void register() {
        ResourceReloadListener.include(OVERLAY_TEXTURE);

        InGameHudRenderCallback.START.register(QuarryMonitorOverlayRenderer::renderOnScreen);
    }
}
