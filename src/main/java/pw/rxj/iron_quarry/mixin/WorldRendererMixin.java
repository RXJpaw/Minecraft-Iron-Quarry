package pw.rxj.iron_quarry.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pw.rxj.iron_quarry.event.WorldRenderCallback;

@Mixin(LevelRenderer.class)
public abstract class WorldRendererMixin {
    @Unique private Camera camera;
    @Unique private Matrix4f positionMatrix;
    @Unique private Matrix4f projectionMatrix;
    @Unique private DeltaTracker tickCounter;

    @Inject(method = "renderLevel", at = @At("HEAD"))
    public void render(GraphicsResourceAllocator allocator, DeltaTracker tickCounter, boolean renderBlockOutline, Camera camera, Matrix4f positionMatrix, Matrix4f projectionMatrix, Matrix4f cullingProjectionMatrix, GpuBufferSlice fogBuffer, Vector4f fogColor, boolean renderSky, CallbackInfo ci) {
        this.camera = camera;
        this.positionMatrix = positionMatrix;
        this.projectionMatrix = projectionMatrix;
        this.tickCounter = tickCounter;
    }

    @Inject(method = "method_62214", at = @At("RETURN"))
    public void renderMain(CallbackInfo ci) {
        if(this.camera == null) return;

        WorldRenderCallback.AFTER_TRANSLUCENT.invoker().afterTranslucent(new WorldRenderCallback.Context(this.camera, new PoseStack(), this.positionMatrix, this.projectionMatrix, this.tickCounter));
    }
}
