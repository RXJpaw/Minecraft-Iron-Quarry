package pw.rxj.iron_quarry.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.records.ChunkTicket;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ChunkLoadingManager {
    private static final ChunkTicketType<ChunkPos> QUARRY_EXPRESS = ChunkTicketType.create("iron_quarry:quarry_express", Comparator.comparingLong(ChunkPos::toLong));
    private static final List<ChunkTicket> loadedChunks = new ArrayList<>();
    private static boolean listenerRegistered = false;

    public static ChunkTicket addTicket(ServerWorld serverWorld, ChunkPos chunkPos, BlockPos sourcePos) {
        Optional<ChunkTicket> existingCheck = getTicket(serverWorld, chunkPos, sourcePos);
        if(existingCheck.isPresent()) return existingCheck.get();

        ChunkTicket chunkTicket = new ChunkTicket(serverWorld, chunkPos, sourcePos);
        loadedChunks.add(chunkTicket);
        addTicket(chunkTicket);

        return chunkTicket;
    }
    public static void addTicket(ChunkTicket chunkTicket) {
        ChunkPos chunkPos = chunkTicket.chunkPos();

        chunkTicket.serverWorld().getChunkManager().addTicket(QUARRY_EXPRESS, chunkPos, 1, chunkPos);
    }

    public static Optional<ChunkTicket> getTicket(ServerWorld serverWorld, ChunkPos chunkPos, BlockPos sourcePos){
        return loadedChunks.stream()
                .filter(chunkTicket -> chunkTicket.serverWorld().equals(serverWorld))
                .filter(chunkTicket -> chunkTicket.sourcePos().equals(sourcePos))
                .filter(chunkTicket -> chunkTicket.chunkPos().equals(chunkPos))
                .findFirst();
    }
    public static List<ChunkTicket> getTickets(ServerWorld serverWorld, BlockPos sourcePos){
        return loadedChunks.stream()
                .filter(chunkTicket -> chunkTicket.serverWorld().equals(serverWorld))
                .filter(chunkTicket -> chunkTicket.sourcePos().equals(sourcePos))
                .toList();
    }

    public static void removeTicket(ServerWorld serverWorld, ChunkPos chunkPos, BlockPos sourcePos) {
        getTicket(serverWorld, chunkPos, sourcePos).ifPresent(ChunkLoadingManager::removeTicket);
    }
    public static void removeTickets(ServerWorld serverWorld, BlockPos sourcePos) {
        getTickets(serverWorld, sourcePos).forEach(ChunkLoadingManager::removeTicket);
    }
    public static void removeTicket(ChunkTicket chunkTicket) {
        ChunkPos chunkPos = chunkTicket.chunkPos();

        chunkTicket.serverWorld().getChunkManager().removeTicket(QUARRY_EXPRESS, chunkPos, 1, chunkPos);

        loadedChunks.remove(chunkTicket);
    }

    public static boolean isTicketPresent(ServerWorld serverWorld, ChunkPos chunkPos, BlockPos sourcePos) {
        return getTicket(serverWorld, chunkPos, sourcePos).isPresent();
    }
    public static boolean isTicketEmpty(ServerWorld serverWorld, ChunkPos chunkPos, BlockPos sourcePos) {
        return getTicket(serverWorld, chunkPos, sourcePos).isEmpty();
    }

    public static boolean isEmpty(){
        return loadedChunks.isEmpty();
    }
    public static int size() {
        return loadedChunks.size();
    }

    public static void register() {
        if(listenerRegistered) return;
        listenerRegistered = true;

        ServerTickEvents.START_WORLD_TICK.register(serverWorld -> {
            if(!loadedChunks.isEmpty()) serverWorld.resetIdleTimeout();

            loadedChunks.forEach(ChunkLoadingManager::addTicket);
        });

        ServerWorldEvents.UNLOAD.register((server, serverWorld) -> loadedChunks.clear());
        ServerWorldEvents.LOAD.register((server, serverWorld) -> loadedChunks.clear());
    }
}
