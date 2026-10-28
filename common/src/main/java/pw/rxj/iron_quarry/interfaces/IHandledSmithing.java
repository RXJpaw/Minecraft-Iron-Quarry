package pw.rxj.iron_quarry.interfaces;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;

public interface IHandledSmithing {
    default Boolean handleSmithingTakeOutput(Player player, Container inputInv, ResultContainer outputInv, ContainerLevelAccess context) {
        return false;
    }
}