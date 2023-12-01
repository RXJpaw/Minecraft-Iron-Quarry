package pw.rxj.iron_quarry.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.*;

public class ChunkLoadingManager {
    private static final ChunkTicketType<ChunkPos> QUARRY_EXPRESS = ChunkTicketType.create("iron_quarry:quarry_express", Comparator.comparingLong(ChunkPos::toLong), 20);
    private static final List<Ticket> LOADED_CHUNKS = new ArrayList<>();
    private static boolean initialized = false;

    public static void addTicket(ServerWorld loadedWorld, ChunkPos loadedChunkPos, ServerWorld sourceWorld, BlockPos sourceBlockPos) {
        Optional<Ticket> existingCheck = getTicket(loadedWorld, loadedChunkPos, sourceWorld, sourceBlockPos);
        if(existingCheck.isPresent()) return;

        Ticket ticket = Ticket.from(loadedWorld, loadedChunkPos, sourceWorld, sourceBlockPos);
        LOADED_CHUNKS.add(ticket);
        addTicket(ticket);
    }
    private static void addTicket(Ticket ticket) {
        ChunkPos chunkPos = ticket.getLoadedChunkPos();

        ticket.getLoadedWorld().getChunkManager().addTicket(QUARRY_EXPRESS, chunkPos, 0, chunkPos);
    }

    private static Optional<Ticket> getTicket(ServerWorld loadedWorld, ChunkPos loadedChunkPos, ServerWorld sourceWorld, BlockPos sourceBlockPos){
        return LOADED_CHUNKS.stream()
                .filter(ticket -> ticket.getLoadedWorld().equals(loadedWorld))
                .filter(ticket -> ticket.getLoadedChunkPos().equals(loadedChunkPos))
                .filter(ticket -> ticket.getSourceWorld().equals(sourceWorld))
                .filter(ticket -> ticket.getSourceBlockPos().equals(sourceBlockPos))
                .findFirst();
    }
    private static List<Ticket> getTickets(ServerWorld sourceWorld, BlockPos sourceBlockPos){
        return LOADED_CHUNKS.stream()
                .filter(ticket -> ticket.getSourceWorld().equals(sourceWorld))
                .filter(ticket -> ticket.getSourceBlockPos().equals(sourceBlockPos))
                .toList();
    }

    public static void removeTicket(ServerWorld loadedWorld, ChunkPos loadedChunkPos, ServerWorld sourceWorld, BlockPos sourceBlockPos) {
        getTicket(loadedWorld, loadedChunkPos, sourceWorld, sourceBlockPos).ifPresent(ChunkLoadingManager::removeTicket);
    }
    public static void removeTickets(ServerWorld sourceWorld, BlockPos sourceBlockPos) {
        getTickets(sourceWorld, sourceBlockPos).forEach(ChunkLoadingManager::removeTicket);
    }
    private static void removeTicket(Ticket ticket) {
        ticket.markRemoved();
    }

    public static boolean isTicketEmpty(ServerWorld loadedWorld, ChunkPos loadedChunkPos, ServerWorld sourceWorld, BlockPos sourceBlockPos) {
        return getTicket(loadedWorld, loadedChunkPos, sourceWorld, sourceBlockPos).isEmpty();
    }
    public static boolean isTicketPresent(ServerWorld loadedWorld, ChunkPos loadedChunkPos, ServerWorld sourceWorld, BlockPos sourceBlockPos) {
        return !isTicketEmpty(loadedWorld, loadedChunkPos, sourceWorld, sourceBlockPos);
    }

    public static boolean isEmpty(){
        return LOADED_CHUNKS.isEmpty();
    }
    public static boolean isPresent(){
        return !isEmpty();
    }
    public static int size() {
        return LOADED_CHUNKS.size();
    }

    public static void register() {
        if(initialized) return; initialized = true;

        ServerTickEvents.START_WORLD_TICK.register(serverWorld -> {
            if(!LOADED_CHUNKS.isEmpty()) serverWorld.resetIdleTimeout();
            Iterator<Ticket> iterator = LOADED_CHUNKS.iterator();

            while(iterator.hasNext()) {
                Ticket ticket = iterator.next();

                if(ticket.isMarkedForRemoval()) {
                    iterator.remove();
                } else {
                    addTicket(ticket);
                }
            }
        });

        ServerWorldEvents.UNLOAD.register((server, serverWorld) -> LOADED_CHUNKS.clear());
        ServerWorldEvents.LOAD.register((server, serverWorld) -> LOADED_CHUNKS.clear());
    }

    private static class Ticket {
        private final ServerWorld loadedWorld;
        private final ChunkPos loadedChunkPos;
        private final ServerWorld sourceWorld;
        private final BlockPos sourceChunkPos;
        private boolean markedForRemoval = false;

        private Ticket(ServerWorld loadedWorld, ChunkPos loadedChunkPos, ServerWorld sourceWorld, BlockPos sourceChunkPos) {
            this.loadedWorld = loadedWorld;
            this.loadedChunkPos = loadedChunkPos;
            this.sourceWorld = sourceWorld;
            this.sourceChunkPos = sourceChunkPos;
        }

        public static Ticket from(ServerWorld serverWorld, ChunkPos chunkPos, ServerWorld sourceWorld, BlockPos sourcePos) {
            return new Ticket(serverWorld, chunkPos, sourceWorld, sourcePos);
        }

        public ServerWorld getLoadedWorld() {
            return this.loadedWorld;
        }
        public ChunkPos getLoadedChunkPos() {
            return this.loadedChunkPos;
        }
        public ServerWorld getSourceWorld() {
            return this.sourceWorld;
        }
        public BlockPos getSourceBlockPos() {
            return this.sourceChunkPos;
        }
        public boolean isMarkedForRemoval() {
            return this.markedForRemoval;
        }

        public void markRemoved() {
            this.markedForRemoval = true;
        }
    }
}
