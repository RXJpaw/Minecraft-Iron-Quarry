package pw.rxj.iron_quarry.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pw.rxj.iron_quarry.gui.CustomTooltipComponent;
import pw.rxj.iron_quarry.gui.CustomTooltipData;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

@Mixin(GuiGraphicsExtractor.class)
public abstract class TooltipRenderMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow protected abstract void setTooltipForNextFrameInternal(Font textRenderer, List<ClientTooltipComponent> components, int x, int y, ClientTooltipPositioner positioner, @Nullable Identifier texture, boolean focused);

    @Inject(method = "setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/Identifier;)V", at = @At(value = "HEAD"), cancellable = true)
    private void renderTooltip(Font textRenderer, List<Component> lines, Optional<TooltipComponent> data, int x, int y, @Nullable Identifier texture, CallbackInfo ci) {
        if(data.isPresent() && data.get() instanceof CustomTooltipData customTooltipData && customTooltipData.renderAtMarker()) {
            List<ClientTooltipComponent> list = lines.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).collect(Collectors.toList());

            for (int i = 0; i < lines.size(); i++) {
                if(lines.get(i).equals(CustomTooltipData.MARKER)) {
                    list.set(i, ClientTooltipComponent.create(customTooltipData)); break;
                }
            }

            this.setTooltipForNextFrameInternal(textRenderer, list, x, y, DefaultTooltipPositioner.INSTANCE, texture, false); ci.cancel();
        }
    }

    @ModifyVariable(method = "tooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;Lnet/minecraft/resources/Identifier;)V", at = @At(value = "HEAD"), argsOnly = true)
    private List<ClientTooltipComponent> renderTooltipCompat(List<ClientTooltipComponent> components) {
        if(minecraft == null) return components;

        return CustomTooltipComponent.moveToMarker(components);
    }
}
