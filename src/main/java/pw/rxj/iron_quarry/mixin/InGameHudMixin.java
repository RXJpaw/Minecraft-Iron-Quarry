package pw.rxj.iron_quarry.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pw.rxj.iron_quarry.event.InGameHudRenderCallback;
import pw.rxj.iron_quarry.interfaces.IAlwaysRenderItemName;
import pw.rxj.iron_quarry.util.ZUtil;

@Mixin(Hud.class)
public abstract class InGameHudMixin {
    @Inject(method = "extractCameraOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/DeltaTracker;getGameTimeDeltaTicks()F"))
    public void renderMiscOverlays(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        float tickDelta = tickCounter.getGameTimeDeltaPartialTick(false);
        Matrix3x2fStack matrices = context.pose();
        matrices.pushMatrix();

        InGameHudRenderCallback.START.invoker().onStart(context, tickDelta);

        matrices.popMatrix();
    }

    @Shadow @Final private Minecraft minecraft;
    @Shadow private int toolHighlightTimer;

    @Inject(method = "tick()V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/player/LocalPlayer;", ordinal = 1, shift = At.Shift.BEFORE))
    public void tick(CallbackInfo ci) {
        //this.client.player was already checked in injected method.
        assert this.minecraft.player != null;

        ItemStack stack = this.minecraft.player.getInventory().getSelectedItem();

        if(ZUtil.getBlockOrItem(stack) instanceof IAlwaysRenderItemName alwaysRenderItemName) {
            if(alwaysRenderItemName.renderItemName(stack)) this.toolHighlightTimer = 40;
        }
    }
}