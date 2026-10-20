package pw.rxj.iron_quarry.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public interface WorldRenderCallback {
    Event<AfterLevel> AFTER_LEVEL = EventFactory.createArrayBacked(AfterLevel.class, (listeners) -> (cameraRenderState) -> {
        for (AfterLevel event : listeners) {
            event.afterLevel(cameraRenderState);
        }
    });

    interface AfterLevel {
        void afterLevel(CameraRenderState cameraRenderState);
    }
}
