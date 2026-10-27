package pw.rxj.iron_quarry.event;

import net.minecraft.server.level.ServerLevel;

public interface ServerLevelCallback {
    Event<Load> LOAD = Event.createArrayBacked(Load.class, (listeners) -> (serverWorld) -> {
        for (Load event : listeners) {
            event.onLoad(serverWorld);
        }
    });
    Event<Unload> UNLOAD = Event.createArrayBacked(Unload.class, (listeners) -> (serverWorld) -> {
        for (Unload event : listeners) {
            event.onUnload(serverWorld);
        }
    });
    Event<StartTick> START_TICK = Event.createArrayBacked(StartTick.class, (listeners) -> (serverWorld) -> {
        for (StartTick event : listeners) {
            event.onStartTick(serverWorld);
        }
    });

    interface Load {
        void onLoad(ServerLevel serverWorld);
    }
    interface Unload {
        void onUnload(ServerLevel serverWorld);
    }
    interface StartTick {
        void onStartTick(ServerLevel serverWorld);
    }
}
