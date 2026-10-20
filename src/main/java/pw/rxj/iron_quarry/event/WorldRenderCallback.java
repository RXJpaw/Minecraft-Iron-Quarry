package pw.rxj.iron_quarry.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public interface WorldRenderCallback {
    Event<AfterTranslucent> AFTER_TRANSLUCENT = EventFactory.createArrayBacked(AfterTranslucent.class, (listeners) -> (context) -> {
        for (AfterTranslucent event : listeners) {
            event.afterTranslucent(context);
        }
    });

    interface AfterTranslucent {
        void afterTranslucent(Context context);
    }

    record Context(Camera camera, MatrixStack matrixStack, Matrix4f positionMatrix, Matrix4f projectionMatrix, RenderTickCounter tickCounter) { }
}
