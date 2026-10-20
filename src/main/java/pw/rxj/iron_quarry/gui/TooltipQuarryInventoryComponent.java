package pw.rxj.iron_quarry.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import pw.rxj.iron_quarry.render.RenderUtil;
import pw.rxj.iron_quarry.screen.QuarryBlockScreen;
import pw.rxj.iron_quarry.util.ComplexInventory;

public class TooltipQuarryInventoryComponent implements CustomTooltipComponent {
    private static final Identifier AUGMENTATION_CONFIGURATION_TEXTURE = QuarryBlockScreen.AUGMENTATION_CONFIGURATION_TEXTURE;
    private static final Identifier QUARRY_SCREEN_BACKGROUND_TEXTURE = QuarryBlockScreen.INVENTORY_LOCATION;

    public final TooltipQuarryInventoryData quarryInventoryData;
    public final ItemStack blueprintStack;
    public final ItemStack drillStack;
    public final ComplexInventory MachineUpgradesInventory;
    public final int augmentLimit;

    private TooltipQuarryInventoryComponent(TooltipQuarryInventoryData tooltipData) {
        this.quarryInventoryData = tooltipData;
        this.blueprintStack = tooltipData.getBlueprintStack();
        this.drillStack = tooltipData.getDrillStack();
        this.MachineUpgradesInventory = tooltipData.getMachineUpgradesInventory();
        this.augmentLimit = tooltipData.getAugmentLimit();
    }

    @Override
    public CustomTooltipData getCustomTooltipData() {
        return this.quarryInventoryData;
    }

    public static TooltipQuarryInventoryComponent of(TooltipQuarryInventoryData tooltipData) {
        return new TooltipQuarryInventoryComponent(tooltipData);
    }

    @Override
    public int getHeight(Font textRenderer) {
        return 22;
    }

    @Override
    public int getWidth(Font textRenderer) {
        return 40 + 108;
    }

    @Override
    public void extractText(GuiGraphicsExtractor context, Font textRenderer, int x, int y) {

    }

    @Override
    public void extractImage(Font textRenderer, int x, int y, int width, int height, GuiGraphicsExtractor context) {
        RenderUtil.drawTexture(context, QUARRY_SCREEN_BACKGROUND_TEXTURE, x, y + 2, 79, 26, 18, 18, 256, 256);
        this.drawItemStack(x, y + 2, this.blueprintStack, textRenderer, context);

        RenderUtil.drawTexture(context, QUARRY_SCREEN_BACKGROUND_TEXTURE, x + 20, y + 2, 79, 47, 18, 18, 256, 256);
        this.drawItemStack(x + 20, y + 2, this.drillStack, textRenderer, context);

        for(int index = 0; index < 6; ++index) {
            int slotX = x + 40 + index * 18;
            int slotY = y + 2;

            if(index >= this.augmentLimit) {
                RenderUtil.drawTexture(context, AUGMENTATION_CONFIGURATION_TEXTURE, slotX, slotY, 100, 0, 18, 18, 256, 256);
            } else {
                RenderUtil.drawTexture(context, AUGMENTATION_CONFIGURATION_TEXTURE, slotX, slotY, 22, 25, 18, 18, 256, 256);
                this.drawAugmentSlot(slotX, slotY, index, textRenderer, context);
            }

        }
    }

    private void drawItemStack(int x, int y, ItemStack stack, Font textRenderer, GuiGraphicsExtractor context) {
        context.item(stack, x + 1, y + 1);
        context.itemDecorations(textRenderer, stack, x + 1, y + 1);
    }

    private void drawAugmentSlot(int x, int y, int index, Font textRenderer, GuiGraphicsExtractor context) {
        if(index >= this.MachineUpgradesInventory.getContainerSize()) return;

        ItemStack stack = this.MachineUpgradesInventory.getItem(index);
        this.drawItemStack(x, y, stack, textRenderer, context);
    }
}
