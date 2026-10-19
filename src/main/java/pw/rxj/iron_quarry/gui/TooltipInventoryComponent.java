package pw.rxj.iron_quarry.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import oshi.util.tuples.Pair;
import pw.rxj.iron_quarry.render.RenderUtil;
import pw.rxj.iron_quarry.Main;

import java.util.List;

public class TooltipInventoryComponent implements CustomTooltipComponent {
    private static final Identifier CUSTOM_SLOTS_TEXTURE = Identifier.of(Main.MOD_ID, "textures/gui/custom_slots.png");;

    public final TooltipInventoryData augmentInventoryData;
    public final List<Pair<ItemStack, Boolean>> disableableInventory;

    private TooltipInventoryComponent(TooltipInventoryData tooltipData) {
        this.augmentInventoryData = tooltipData;
        this.disableableInventory = tooltipData.getDisableableInventory();
    }

    @Override
    public CustomTooltipData getCustomTooltipData() {
        return this.augmentInventoryData;
    }

    public static TooltipInventoryComponent of(TooltipInventoryData tooltipData) {
        return new TooltipInventoryComponent(tooltipData);
    }

    @Override
    public int getHeight(TextRenderer textRenderer) {
        return 22;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return this.disableableInventory.size() * 18;
    }

    @Override
    public void drawText(DrawContext context, TextRenderer textRenderer, int x, int y) {

    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, int width, int height, DrawContext context) {
        for(int index = 0; index < this.disableableInventory.size(); ++index) {
            int slotX = x + index * 18;
            int slotY = y + 2;

            RenderUtil.drawTexture(context, CUSTOM_SLOTS_TEXTURE, slotX, slotY, 0, 0, 18, 18, 36, 36);

            this.drawSlot(slotX, slotY, index, textRenderer, context);
        }
    }

    private void drawSlot(int x, int y, int index, TextRenderer textRenderer, DrawContext context) {
        Pair<ItemStack, Boolean> slot = this.disableableInventory.get(index);
        ItemStack itemStack = slot.getA();
        Boolean disabled = slot.getB();

        context.drawItem(itemStack, x + 1, y + 1, index);
        context.drawStackOverlay(textRenderer, itemStack, x + 1, y + 1);

        if(disabled) {
            context.fillGradient(x, y, x + 18, y + 18, 0xAA000000, 0xAA000000);
        }
    }
}
