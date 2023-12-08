package pw.rxj.iron_quarry.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import pw.rxj.iron_quarry.screen.QuarryBlockScreen;
import pw.rxj.iron_quarry.util.ComplexInventory;

public class TooltipAugmentInventoryComponent implements CustomTooltipComponent {
    private static final Identifier AUGMENTATION_CONFIGURATION_TEXTURE = QuarryBlockScreen.AUGMENTATION_CONFIGURATION_TEXTURE;

    public final TooltipAugmentInventoryData augmentInventoryData;
    public final ComplexInventory MachineUpgradesInventory;
    public final int augmentLimit;

    private TooltipAugmentInventoryComponent(TooltipAugmentInventoryData tooltipData) {
        this.augmentInventoryData = tooltipData;
        this.MachineUpgradesInventory = tooltipData.getMachineUpgradesInventory();
        this.augmentLimit = tooltipData.getAugmentLimit();
    }

    @Override
    public CustomTooltipData getCustomTooltipData() {
        return this.augmentInventoryData;
    }

    public static TooltipAugmentInventoryComponent of(TooltipAugmentInventoryData tooltipData) {
        return new TooltipAugmentInventoryComponent(tooltipData);
    }

    @Override
    public int getHeight() {
        return 22;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return 108;
    }

    @Override
    public void drawText(TextRenderer textRenderer, int x, int y, Matrix4f matrix, VertexConsumerProvider.Immediate vertexConsumers) {

    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer, int z) {
        for(int index = 0; index < 6; ++index) {
            int slotX = x + index * 18;
            int slotY = y + 2;

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, AUGMENTATION_CONFIGURATION_TEXTURE);

            if(index >= this.augmentLimit) {
                DrawableHelper.drawTexture(matrices, slotX, slotY, z, 100, 0, 18, 18, 256, 256);
            } else {
                DrawableHelper.drawTexture(matrices, slotX, slotY, z, 22, 25, 18, 18, 256, 256);
                this.drawSlot(slotX, slotY, index, textRenderer, matrices, itemRenderer, z);
            }

        }
    }

    private void drawSlot(int x, int y, int index, TextRenderer textRenderer, MatrixStack matrices, ItemRenderer itemRenderer, int z) {
        if(index >= this.MachineUpgradesInventory.size()) return;

        ItemStack itemStack = this.MachineUpgradesInventory.getStack(index);
        itemRenderer.renderInGuiWithOverrides(itemStack, x + 1, y + 1, index);
        itemRenderer.renderGuiItemOverlay(textRenderer, itemStack, x + 1, y + 1);
    }
}
