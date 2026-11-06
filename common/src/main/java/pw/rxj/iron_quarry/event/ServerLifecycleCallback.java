package pw.rxj.iron_quarry.event;

import net.minecraft.server.level.ServerPlayer;

public interface ServerLifecycleCallback {
    Event<SyncDataPackContents> SYNC_DATA_PACK_CONTENTS = Event.createArrayBacked(SyncDataPackContents.class, (listeners) -> (player) -> {
        for (SyncDataPackContents event : listeners) {
            event.onSyncDataPackContents(player);
        }
    });

    interface SyncDataPackContents {
        void onSyncDataPackContents(ServerPlayer player);
    }
}
