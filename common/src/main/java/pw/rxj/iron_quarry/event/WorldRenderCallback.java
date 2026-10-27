package pw.rxj.iron_quarry.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public interface WorldRenderCallback {
    Event<EndMain> END_MAIN = Event.createArrayBacked(EndMain.class, (listeners) -> (cameraRenderState, matrices) -> {
        for (EndMain event : listeners) {
            event.endMain(cameraRenderState, matrices);
        }
    });
    Event<AfterLevel> AFTER_LEVEL = Event.createArrayBacked(AfterLevel.class, (listeners) -> (cameraRenderState) -> {
        for (AfterLevel event : listeners) {
            event.afterLevel(cameraRenderState);
        }
    });

    interface EndMain {
        void endMain(CameraRenderState cameraRenderState, PoseStack matrices);
    }
    interface AfterLevel {
        void afterLevel(CameraRenderState cameraRenderState);
    }
}
