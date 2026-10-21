package pw.rxj.iron_quarry.mixin;

import com.mojang.renderpearl.api.commands.RenderPass;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pw.rxj.iron_quarry.event.WorldRenderCallback;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Inject(method = "executeClassicTransparency", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;executeTranslucentAfterTerrain(Lcom/mojang/renderpearl/api/commands/RenderPass;)V", shift = At.Shift.AFTER))
    private void beforeClassicClouds(ChunkSectionsToRender sectionsToRender, FeatureRenderDispatcher.PreparedFrame preparedFrame, RenderPass renderPass, CallbackInfo ci) {
        WorldRenderCallback.BEFORE_CLOUDS.invoker().beforeClouds(renderPass);
    }

    @Inject(method = "executeOit", at = @At(value = "HEAD"))
    private void beforeOit(ChunkSectionsToRender sectionsToRender, FeatureRenderDispatcher.PreparedFrame preparedFrame, CallbackInfo ci) {
        WorldRenderCallback.BEFORE_CLOUDS.invoker().beforeClouds(null);
    }
}
