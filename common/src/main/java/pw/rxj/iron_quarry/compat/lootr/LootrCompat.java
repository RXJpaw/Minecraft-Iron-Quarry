package pw.rxj.iron_quarry.compat.lootr;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.compat.Compat;

import java.util.function.Supplier;

public class LootrCompat {
    public static @Nullable Container getLootableContainer(RandomizableContainerBlockEntity blockEntity, Supplier<ServerPlayer> ownerSupplier) {
        try {
            return LootrAccess.getLootableContainer(blockEntity, ownerSupplier);
        } catch (LinkageError error) {
            disable(error);
            return blockEntity;
        }
    }

    public static void markOpened(BlockEntity blockEntity, Supplier<ServerPlayer> ownerSupplier) {
        try {
            LootrAccess.markOpened(blockEntity, ownerSupplier);
        } catch (LinkageError error) {
            disable(error);
        }
    }

    private static void disable(LinkageError error) {
        Compat.LOOTR_LOADED = false;
        Main.LOGGER.warn("The installed version of Lootr appears to be incompatible; Lootr integration has been disabled until the next restart!", error);
    }
}
