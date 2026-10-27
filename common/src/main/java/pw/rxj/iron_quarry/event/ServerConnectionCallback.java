package pw.rxj.iron_quarry.event;

import net.minecraft.server.level.ServerPlayer;

public interface ServerConnectionCallback {
    Event<Join> JOIN = Event.createArrayBacked(Join.class, (listeners) -> (player) -> {
        for (Join event : listeners) {
            event.onJoin(player);
        }
    });

    interface Join {
        void onJoin(ServerPlayer player);
    }
}
