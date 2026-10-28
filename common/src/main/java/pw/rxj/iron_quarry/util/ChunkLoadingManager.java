package pw.rxj.iron_quarry.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import pw.rxj.iron_quarry.platform.Platform;

import java.util.List;

public class ChunkLoadingManager {
    private static final TicketType QUARRY_EXPRESS = new TicketType(20L, TicketType.FLAG_LOADING);
    private static boolean initialized = false;

    public static void keepLoaded(ServerLevel loadedWorld, List<ChunkPos> chunkPosList) {
        for (ChunkPos chunkPos : chunkPosList) {
            loadedWorld.getChunkSource().addTicketWithRadius(QUARRY_EXPRESS, chunkPos, 0);
        }
    }

    public static void register() {
        if(initialized) return; initialized = true;

        Platform.REGISTRY.register(Registries.TICKET_TYPE, Identifier.fromNamespaceAndPath("iron_quarry", "quarry_express"), () -> QUARRY_EXPRESS);
    }
}
