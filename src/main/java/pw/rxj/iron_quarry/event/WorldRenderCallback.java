package pw.rxj.iron_quarry.event;

import net.fabricmc.fabric.api.event.Event;
import com.mojang.renderpearl.api.commands.RenderPass;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.jetbrains.annotations.Nullable;

public interface WorldRenderCallback {
    Event<BeforeClouds> BEFORE_CLOUDS = EventFactory.createArrayBacked(BeforeClouds.class, (listeners) -> (renderPass) -> {
        for (BeforeClouds event : listeners) {
            event.beforeClouds(renderPass);
        }
    });
    Event<AfterLevel> AFTER_LEVEL = EventFactory.createArrayBacked(AfterLevel.class, (listeners) -> (cameraRenderState) -> {
        for (AfterLevel event : listeners) {
            event.afterLevel(cameraRenderState);
        }
    });

    interface BeforeClouds {
        void beforeClouds(@Nullable RenderPass renderPass);
    }
    interface AfterLevel {
        void afterLevel(CameraRenderState cameraRenderState);
    }
}
