package pw.rxj.iron_quarry.event;

import net.minecraft.server.MinecraftServer;

public interface ServerLifecycleCallback {
    Event<Starting> STARTING = Event.createArrayBacked(Starting.class, (listeners) -> (server) -> {
        for (Starting event : listeners) {
            event.onStarting(server);
        }
    });

    interface Starting {
        void onStarting(MinecraftServer server);
    }
}
