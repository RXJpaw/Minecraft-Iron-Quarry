package pw.rxj.iron_quarry.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.GameRenderState;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pw.rxj.iron_quarry.event.WorldRenderCallback;
import pw.rxj.iron_quarry.render.RenderUtil;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow @Final private GameRenderState gameRenderState;

    @ModifyArg(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ProjectionMatrixBuffer;getBuffer(Lorg/joml/Matrix4f;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"))
    private Matrix4f renderLevel(Matrix4f projectionMatrix) {
        this.gameRenderState.levelRenderState.cameraRenderState.setData(RenderUtil.RenderStates.PROJECTION_MATRIX, new Matrix4f(projectionMatrix));

        return projectionMatrix;
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/renderer/state/level/CameraRenderState;Lorg/joml/Matrix4fc;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;ZLnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;)V", shift = At.Shift.AFTER))
    private void afterRenderLevel(DeltaTracker tickCounter, CallbackInfo ci) {
        WorldRenderCallback.AFTER_LEVEL.invoker().afterLevel(this.gameRenderState.levelRenderState.cameraRenderState);
    }
}
