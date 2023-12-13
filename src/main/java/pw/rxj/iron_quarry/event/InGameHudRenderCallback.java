package pw.rxj.iron_quarry.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.util.math.MatrixStack;

public interface InGameHudRenderCallback {
    Event<Start> START = EventFactory.createArrayBacked(Start.class, (listeners) -> (matrices, tickDelta) -> {
        for (Start event : listeners) {
            event.onStart(matrices, tickDelta);
        }
    });

    interface Start {
        void onStart(MatrixStack matrices, double tickDelta);
    }
}