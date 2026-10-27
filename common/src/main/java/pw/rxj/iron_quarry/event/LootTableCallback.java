package pw.rxj.iron_quarry.event;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootPool;

import java.util.function.Consumer;

public interface LootTableCallback {
    Event<Modify> MODIFY = Event.createArrayBacked(Modify.class, (listeners) -> (lootTableId, poolAdder) -> {
        for (Modify event : listeners) {
            event.onModify(lootTableId, poolAdder);
        }
    });

    interface Modify {
        void onModify(Identifier lootTableId, Consumer<LootPool.Builder> poolAdder);
    }
}
