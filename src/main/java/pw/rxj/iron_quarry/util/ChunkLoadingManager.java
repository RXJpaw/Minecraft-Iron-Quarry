package pw.rxj.iron_quarry.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import pw.rxj.iron_quarry.records.ChunkTicket;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ChunkLoadingManager {
    private static final ChunkTicketType<ChunkPos> QUARRY_EXPRESS = ChunkTicketType.create("iron_quarry:quarry_express", Comparator.comparingLong(ChunkPos::toLong));
    private static final List<ChunkTicket> LOADED_CHUNKS = new ArrayList<>();
    private static boolean listenerRegistered = false;

    public static ChunkTicket addTicket(ServerWorld serverWorld, ChunkPos chunkPos, ServerWorld sourceWorld, BlockPos sourcePos) {
        Optional<ChunkTicket> existingCheck = getTicket(serverWorld, chunkPos, sourceWorld, sourcePos);
        if(existingCheck.isPresent()) return existingCheck.get();

        ChunkTicket chunkTicket = new ChunkTicket(serverWorld, chunkPos, sourceWorld, sourcePos);
        LOADED_CHUNKS.add(chunkTicket);
        addTicket(chunkTicket);

        return chunkTicket;
    }
    public static void addTicket(ChunkTicket chunkTicket) {
        ChunkPos chunkPos = chunkTicket.chunkPos();

        chunkTicket.serverWorld().getChunkManager().addTicket(QUARRY_EXPRESS, chunkPos, 1, chunkPos);
    }

    public static Optional<ChunkTicket> getTicket(ServerWorld serverWorld, ChunkPos chunkPos, ServerWorld sourceWorld, BlockPos sourcePos){
        return LOADED_CHUNKS.stream()
                .filter(chunkTicket -> chunkTicket.serverWorld().equals(serverWorld))
                .filter(chunkTicket -> chunkTicket.sourcePos().equals(sourcePos))
                .filter(chunkTicket -> chunkTicket.sourceWorld().equals(sourceWorld))
                .filter(chunkTicket -> chunkTicket.chunkPos().equals(chunkPos))
                .findFirst();
    }
    public static List<ChunkTicket> getTickets(ServerWorld sourceWorld, BlockPos sourcePos){
        return LOADED_CHUNKS.stream()
                .filter(chunkTicket -> chunkTicket.sourceWorld().equals(sourceWorld))
                .filter(chunkTicket -> chunkTicket.sourcePos().equals(sourcePos))
                .toList();
    }

    public static void removeTicket(ServerWorld serverWorld, ChunkPos chunkPos, ServerWorld sourceWorld, BlockPos sourcePos) {
        getTicket(serverWorld, chunkPos, sourceWorld, sourcePos).ifPresent(ChunkLoadingManager::removeTicket);
    }
    public static void removeTickets(ServerWorld sourceWorld, BlockPos sourcePos) {
        getTickets(sourceWorld, sourcePos).forEach(ChunkLoadingManager::removeTicket);
    }
    public static void removeTicket(ChunkTicket chunkTicket) {
        ChunkPos chunkPos = chunkTicket.chunkPos();

        chunkTicket.serverWorld().getChunkManager().removeTicket(QUARRY_EXPRESS, chunkPos, 1, chunkPos);

        LOADED_CHUNKS.remove(chunkTicket);
    }

    public static boolean isTicketPresent(ServerWorld serverWorld, ChunkPos chunkPos, ServerWorld sourceWorld, BlockPos sourcePos) {
        return getTicket(serverWorld, chunkPos, sourceWorld, sourcePos).isPresent();
    }
    public static boolean isTicketEmpty(ServerWorld serverWorld, ChunkPos chunkPos, ServerWorld sourceWorld, BlockPos sourcePos) {
        return getTicket(serverWorld, chunkPos, sourceWorld, sourcePos).isEmpty();
    }

    public static boolean isEmpty(){
        return LOADED_CHUNKS.isEmpty();
    }
    public static int size() {
        return LOADED_CHUNKS.size();
    }

    public static void register() {
        if(listenerRegistered) return;
        listenerRegistered = true;

        ServerTickEvents.START_WORLD_TICK.register(serverWorld -> {
            if(!LOADED_CHUNKS.isEmpty()) serverWorld.resetIdleTimeout();

            LOADED_CHUNKS.forEach(ChunkLoadingManager::addTicket);
        });

        ServerWorldEvents.UNLOAD.register((server, serverWorld) -> LOADED_CHUNKS.clear());
        ServerWorldEvents.LOAD.register((server, serverWorld) -> LOADED_CHUNKS.clear());
    }
}
