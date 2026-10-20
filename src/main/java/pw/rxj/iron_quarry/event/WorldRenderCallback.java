package pw.rxj.iron_quarry.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
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

    record Context(Camera camera, PoseStack matrixStack, Matrix4f positionMatrix, Matrix4f projectionMatrix, DeltaTracker tickCounter) { }
}
