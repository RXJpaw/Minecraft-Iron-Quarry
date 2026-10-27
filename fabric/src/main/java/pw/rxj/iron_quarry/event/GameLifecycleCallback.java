package pw.rxj.iron_quarry.event;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface GameLifecycleCallback {
    /**
     * It is not recommended registering into ITEM or other texture reliant registry types as they will be rendered as missing textures!
     * Use {@link ModInitializer#onInitialize()} instead.
     * <p>
     * @implNote Will fall back to {@link #IMMINENT_REGISTRY_FREEZE} on servers!
     */
    Event<ImminentFirstReload> IMMINENT_FIRST_RELOAD = EventFactory.createArrayBacked(ImminentFirstReload.class, (listeners) -> () -> {
        for (ImminentFirstReload event : listeners) {
            event.onImminentFirstReload();
        }
    });
    /**
     * It is not recommended registering into ITEM or other texture reliant registry types as they will be rendered as missing textures!
     * Use {@link ModInitializer#onInitialize()} instead.
     */
    Event<ImminentRegistryFreeze> IMMINENT_REGISTRY_FREEZE = EventFactory.createArrayBacked(ImminentRegistryFreeze.class, (listeners) -> () -> {
        for (ImminentRegistryFreeze event : listeners) {
            event.onImminentRegistryFreeze();
        }
    });

    interface ImminentFirstReload {
        void onImminentFirstReload();
    }
    interface ImminentRegistryFreeze {
        void onImminentRegistryFreeze();
    }
}
