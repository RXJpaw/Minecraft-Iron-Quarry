package pw.rxj.iron_quarry.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import pw.rxj.iron_quarry.gui.CustomTooltipComponent;

import java.util.List;

@Mixin(GuiGraphicsExtractor.class)
public abstract class NeoForgeTooltipRenderMixin {
    @ModifyVariable(method = "tooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;Lnet/minecraft/resources/Identifier;Lnet/minecraft/world/item/ItemStack;)V", at = @At(value = "HEAD"), argsOnly = true)
    private List<ClientTooltipComponent> renderTooltipCompat(List<ClientTooltipComponent> components) {
        return CustomTooltipComponent.moveToMarker(components);
    }
}
