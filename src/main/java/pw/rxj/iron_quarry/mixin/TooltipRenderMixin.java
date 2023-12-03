package pw.rxj.iron_quarry.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.OrderedTextTooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipData;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pw.rxj.iron_quarry.gui.CustomTooltipComponent;
import pw.rxj.iron_quarry.gui.CustomTooltipData;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Mixin(Screen.class)
public abstract class TooltipRenderMixin {

    @Shadow @Nullable protected MinecraftClient client;

    @Shadow protected abstract void renderTooltipFromComponents(MatrixStack matrices, List<TooltipComponent> components, int x, int y);

    @Inject(method = "renderTooltip(Lnet/minecraft/client/util/math/MatrixStack;Ljava/util/List;Ljava/util/Optional;II)V", at = @At(value = "HEAD"), cancellable = true)
    private void renderTooltip(MatrixStack matrices, List<Text> lines, Optional<TooltipData> data, int x, int y, CallbackInfo ci) {
        if(data.isPresent() && data.get() instanceof CustomTooltipData customTooltipData && customTooltipData.renderAtMarker()) {
            List<TooltipComponent> list = lines.stream().map(Text::asOrderedText).map(TooltipComponent::of).collect(Collectors.toList());

            for (int i = 0; i < lines.size(); i++) {
                if(lines.get(i).equals(CustomTooltipData.MARKER)) {
                    list.set(i, TooltipComponent.of(customTooltipData)); break;
                }
            }

            renderTooltipFromComponents(matrices, list, x, y); ci.cancel();
        }
    }

    @Inject(method = "renderTooltipFromComponents", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z"))
    private void renderTooltipCompat(MatrixStack matrices, List<TooltipComponent> components, int x, int y, CallbackInfo ci) {
        if(client == null) return;

        if(components.size() > 1 && components.get(1) instanceof CustomTooltipComponent customTooltipComponent) {
            CustomTooltipData customTooltipData = customTooltipComponent.getCustomTooltipData();
            if(!customTooltipData.renderAtMarker()) return;

            TooltipComponent substituteComponent = components.remove(1);
            int replaceIndex = 0;

            for (int i = 0; i < components.size(); i++) {
                if(components.get(i) instanceof OrderedTextTooltipComponent textTooltipComponent) {
                    if(ZUtil.toString(textTooltipComponent.text).equals(CustomTooltipData.MARKER.getString())) {
                        replaceIndex = i; break;
                    }
                }
            }

            if(replaceIndex > 0) components.set(replaceIndex, substituteComponent);
        }
    }
}
