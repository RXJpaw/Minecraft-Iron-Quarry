package pw.rxj.iron_quarry.event;

public interface ClientConnectionCallback {
    Event<Disconnect> DISCONNECT = Event.createArrayBacked(Disconnect.class, (listeners) -> () -> {
        for (Disconnect event : listeners) {
            event.onDisconnect();
        }
    });

    interface Disconnect {
        void onDisconnect();
    }
}
