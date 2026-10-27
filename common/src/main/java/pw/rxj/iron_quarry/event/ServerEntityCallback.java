package pw.rxj.iron_quarry.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public interface ServerEntityCallback {
    Event<Load> LOAD = Event.createArrayBacked(Load.class, (listeners) -> (entity, serverWorld) -> {
        for (Load event : listeners) {
            event.onLoad(entity, serverWorld);
        }
    });

    interface Load {
        void onLoad(Entity entity, ServerLevel serverWorld);
    }
}
