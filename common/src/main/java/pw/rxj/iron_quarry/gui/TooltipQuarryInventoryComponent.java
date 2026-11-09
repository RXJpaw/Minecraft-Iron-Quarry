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
    private static final Identifier EXPANSION_CONFIGURATION_TEXTURE = QuarryBlockScreen.EXPANSION_CONFIGURATION_TEXTURE;
    private static final Identifier QUARRY_SCREEN_BACKGROUND_TEXTURE = QuarryBlockScreen.INVENTORY_LOCATION;

    public final TooltipQuarryInventoryData quarryInventoryData;
    public final ItemStack blueprintStack;
    public final ItemStack drillStack;
    public final ComplexInventory augmentInventory;
    public final int augmentLimit;
    public final ComplexInventory expansionInventory;
    public final int expansionLimit;

    private TooltipQuarryInventoryComponent(TooltipQuarryInventoryData tooltipData) {
        this.quarryInventoryData = tooltipData;
        this.blueprintStack = tooltipData.getBlueprintStack();
        this.drillStack = tooltipData.getDrillStack();
        this.augmentInventory = tooltipData.getAugmentInventory();
        this.augmentLimit = tooltipData.getAugmentLimit();
        this.expansionInventory = tooltipData.getExpansionInventory();
        this.expansionLimit = tooltipData.getExpansionLimit();
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
        return 42;
    }

    @Override
    public int getWidth(Font textRenderer) {
        return 20 + 108;
    }

    @Override
    public void extractText(GuiGraphicsExtractor context, Font textRenderer, int x, int y) {

    }

    @Override
    public void extractImage(Font textRenderer, int x, int y, int width, int height, GuiGraphicsExtractor context) {
        RenderUtil.drawTexture(context, QUARRY_SCREEN_BACKGROUND_TEXTURE, x, y + 2, 79, 26, 18, 18, 256, 256);
        this.drawItemStack(x, y + 2, this.blueprintStack, textRenderer, context);

        RenderUtil.drawTexture(context, QUARRY_SCREEN_BACKGROUND_TEXTURE, x, y + 22, 79, 47, 18, 18, 256, 256);
        this.drawItemStack(x, y + 22, this.drillStack, textRenderer, context);

        this.drawSlotRow(x + 20, y + 2, AUGMENTATION_CONFIGURATION_TEXTURE, this.augmentInventory, this.augmentLimit, textRenderer, context);
        this.drawSlotRow(x + 20, y + 22, EXPANSION_CONFIGURATION_TEXTURE, this.expansionInventory, this.expansionLimit, textRenderer, context);
    }

    private void drawItemStack(int x, int y, ItemStack stack, Font textRenderer, GuiGraphicsExtractor context) {
        context.item(stack, x + 1, y + 1);
        context.itemDecorations(textRenderer, stack, x + 1, y + 1);
    }

    private void drawSlotRow(int x, int y, Identifier texture, ComplexInventory inventory, int limit, Font textRenderer, GuiGraphicsExtractor context) {
        for(int index = 0; index < 6; ++index) {
            int slotX = x + index * 18;

            if(index >= limit) {
                RenderUtil.drawTexture(context, texture, slotX, y, 100, 0, 18, 18, 256, 256);
            } else {
                RenderUtil.drawTexture(context, texture, slotX, y, 22, 25, 18, 18, 256, 256);
            }

            if(index < inventory.getContainerSize()) {
                this.drawItemStack(slotX, y, inventory.getItem(index), textRenderer, context);
            }
        }
    }
}
