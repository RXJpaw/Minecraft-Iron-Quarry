package pw.rxj.iron_quarry.gui;

import com.mojang.datafixers.util.Pair;
import pw.rxj.iron_quarry.render.RenderUtil;
import pw.rxj.iron_quarry.Main;

import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class TooltipInventoryComponent implements CustomTooltipComponent {
    private static final Identifier CUSTOM_SLOTS_TEXTURE = Identifier.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/custom_slots.png");;

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
    public int getHeight(Font textRenderer) {
        return 22;
    }

    @Override
    public int getWidth(Font textRenderer) {
        return this.disableableInventory.size() * 18;
    }

    @Override
    public void extractText(GuiGraphicsExtractor context, Font textRenderer, int x, int y) {

    }

    @Override
    public void extractImage(Font textRenderer, int x, int y, int width, int height, GuiGraphicsExtractor context) {
        for(int index = 0; index < this.disableableInventory.size(); ++index) {
            int slotX = x + index * 18;
            int slotY = y + 2;

            RenderUtil.drawTexture(context, CUSTOM_SLOTS_TEXTURE, slotX, slotY, 0, 0, 18, 18, 36, 36);

            this.drawSlot(slotX, slotY, index, textRenderer, context);
        }
    }

    private void drawSlot(int x, int y, int index, Font textRenderer, GuiGraphicsExtractor context) {
        Pair<ItemStack, Boolean> slot = this.disableableInventory.get(index);
        ItemStack itemStack = slot.getFirst();
        Boolean disabled = slot.getSecond();

        context.item(itemStack, x + 1, y + 1, index);
        context.itemDecorations(textRenderer, itemStack, x + 1, y + 1);

        if(disabled) {
            context.fillGradient(x, y, x + 18, y + 18, 0xAA000000, 0xAA000000);
        }
    }
}
