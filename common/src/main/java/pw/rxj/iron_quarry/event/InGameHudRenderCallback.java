package pw.rxj.iron_quarry.event;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface InGameHudRenderCallback {
    Event<Start> START = Event.createArrayBacked(Start.class, (listeners) -> (context, tickDelta) -> {
        for (Start event : listeners) {
            event.onStart(context, tickDelta);
        }
    });

    interface Start {
        void onStart(GuiGraphicsExtractor context, double tickDelta);
    }
}