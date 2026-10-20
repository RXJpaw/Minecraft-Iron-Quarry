package pw.rxj.iron_quarry.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.memory.ObjectAllocator;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pw.rxj.iron_quarry.event.WorldRenderCallback;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Unique private Camera camera;
    @Unique private Matrix4f positionMatrix;
    @Unique private Matrix4f projectionMatrix;
    @Unique private RenderTickCounter tickCounter;

    @Inject(method = "render", at = @At("HEAD"))
    public void render(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, Matrix4f positionMatrix, Matrix4f projectionMatrix, Matrix4f cullingProjectionMatrix, GpuBufferSlice fogBuffer, Vector4f fogColor, boolean renderSky, CallbackInfo ci) {
        this.camera = camera;
        this.positionMatrix = positionMatrix;
        this.projectionMatrix = projectionMatrix;
        this.tickCounter = tickCounter;
    }

    @Inject(method = "method_62214", at = @At("RETURN"))
    public void renderMain(CallbackInfo ci) {
        if(this.camera == null) return;

        WorldRenderCallback.AFTER_TRANSLUCENT.invoker().afterTranslucent(new WorldRenderCallback.Context(this.camera, new MatrixStack(), this.positionMatrix, this.projectionMatrix, this.tickCounter));
    }
}
