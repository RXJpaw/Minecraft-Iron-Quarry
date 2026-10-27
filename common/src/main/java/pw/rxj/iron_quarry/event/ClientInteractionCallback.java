package pw.rxj.iron_quarry.event;

import net.minecraft.client.player.LocalPlayer;

public interface ClientInteractionCallback {
    Event<PreAttack> PRE_ATTACK = Event.createArrayBacked(PreAttack.class, (listeners) -> (player, clickCount) -> {
        for (PreAttack event : listeners) {
            if(event.onPreAttack(player, clickCount)) return true;
        }

        return false;
    });

    interface PreAttack {
        boolean onPreAttack(LocalPlayer player, int clickCount);
    }
}
