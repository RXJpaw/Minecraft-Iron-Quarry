package pw.rxj.iron_quarry.event;

public interface GameLifecycleCallback {
    /**
     * It is not recommended registering into ITEM or other texture reliant registry types as they will be rendered as missing textures!
     * Use {@link ModInitializer#onInitialize()} instead.
     * <p>
     * @implNote Will fall back to {@link #IMMINENT_REGISTRY_FREEZE} on servers!
     */
    Event<ImminentFirstReload> IMMINENT_FIRST_RELOAD = Event.createArrayBacked(ImminentFirstReload.class, (listeners) -> () -> {
        for (ImminentFirstReload event : listeners) {
            event.onImminentFirstReload();
        }
    });
    /**
     * It is not recommended registering into ITEM or other texture reliant registry types as they will be rendered as missing textures!
     * Use {@link ModInitializer#onInitialize()} instead.
     */
    Event<ImminentRegistryFreeze> IMMINENT_REGISTRY_FREEZE = Event.createArrayBacked(ImminentRegistryFreeze.class, (listeners) -> () -> {
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
