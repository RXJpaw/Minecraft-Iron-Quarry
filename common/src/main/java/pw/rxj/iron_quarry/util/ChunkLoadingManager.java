package pw.rxj.iron_quarry.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import pw.rxj.iron_quarry.event.ServerLevelCallback;
import pw.rxj.iron_quarry.platform.Platform;

import java.util.*;

public class ChunkLoadingManager {
    private static final TicketType QUARRY_EXPRESS = new TicketType(20L, TicketType.FLAG_LOADING);
    private static final List<Ticket> LOADED_CHUNKS = new ArrayList<>();
    private static boolean initialized = false;

    public static void addTicket(ServerLevel loadedWorld, ChunkPos loadedChunkPos, ServerLevel sourceWorld, BlockPos sourceBlockPos) {
        Optional<Ticket> existingCheck = getTicket(loadedWorld, loadedChunkPos, sourceWorld, sourceBlockPos);
        if(existingCheck.isPresent()) return;

        Ticket ticket = Ticket.from(loadedWorld, loadedChunkPos, sourceWorld, sourceBlockPos);
        LOADED_CHUNKS.add(ticket);
        addTicket(ticket);
    }
    private static void addTicket(Ticket ticket) {
        ChunkPos chunkPos = ticket.getLoadedChunkPos();

        ticket.getLoadedWorld().getChunkSource().addTicketWithRadius(QUARRY_EXPRESS, chunkPos, 0);
    }

    private static Optional<Ticket> getTicket(ServerLevel loadedWorld, ChunkPos loadedChunkPos, ServerLevel sourceWorld, BlockPos sourceBlockPos){
        return LOADED_CHUNKS.stream()
                .filter(ticket -> ticket.getLoadedWorld().equals(loadedWorld))
                .filter(ticket -> ticket.getLoadedChunkPos().equals(loadedChunkPos))
                .filter(ticket -> ticket.getSourceWorld().equals(sourceWorld))
                .filter(ticket -> ticket.getSourceBlockPos().equals(sourceBlockPos))
                .findFirst();
    }
    public static List<Ticket> getTickets(ServerLevel sourceWorld, BlockPos sourceBlockPos){
        return LOADED_CHUNKS.stream()
                .filter(ticket -> ticket.getSourceWorld().equals(sourceWorld))
                .filter(ticket -> ticket.getSourceBlockPos().equals(sourceBlockPos))
                .toList();
    }

    public static void removeTicket(ServerLevel loadedWorld, ChunkPos loadedChunkPos, ServerLevel sourceWorld, BlockPos sourceBlockPos) {
        getTicket(loadedWorld, loadedChunkPos, sourceWorld, sourceBlockPos).ifPresent(ChunkLoadingManager::removeTicket);
    }
    public static void removeTickets(ServerLevel sourceWorld, BlockPos sourceBlockPos) {
        getTickets(sourceWorld, sourceBlockPos).forEach(ChunkLoadingManager::removeTicket);
    }
    private static void removeTicket(Ticket ticket) {
        ticket.markRemoved();
    }

    public static boolean isTicketEmpty(ServerLevel loadedWorld, ChunkPos loadedChunkPos, ServerLevel sourceWorld, BlockPos sourceBlockPos) {
        return getTicket(loadedWorld, loadedChunkPos, sourceWorld, sourceBlockPos).isEmpty();
    }
    public static boolean isTicketPresent(ServerLevel loadedWorld, ChunkPos loadedChunkPos, ServerLevel sourceWorld, BlockPos sourceBlockPos) {
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

        Platform.REGISTRY.register(Registries.TICKET_TYPE, Identifier.fromNamespaceAndPath("iron_quarry", "quarry_express"), () -> QUARRY_EXPRESS);

        ServerLevelCallback.START_TICK.register(serverWorld -> {
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

        ServerLevelCallback.UNLOAD.register(serverWorld -> LOADED_CHUNKS.clear());
        ServerLevelCallback.LOAD.register(serverWorld -> LOADED_CHUNKS.clear());
    }

    public static class Ticket {
        private final ServerLevel loadedWorld;
        private final ChunkPos loadedChunkPos;
        private final ServerLevel sourceWorld;
        private final BlockPos sourceChunkPos;
        private boolean markedForRemoval = false;

        private Ticket(ServerLevel loadedWorld, ChunkPos loadedChunkPos, ServerLevel sourceWorld, BlockPos sourceChunkPos) {
            this.loadedWorld = loadedWorld;
            this.loadedChunkPos = loadedChunkPos;
            this.sourceWorld = sourceWorld;
            this.sourceChunkPos = sourceChunkPos;
        }

        public static Ticket from(ServerLevel serverWorld, ChunkPos chunkPos, ServerLevel sourceWorld, BlockPos sourcePos) {
            return new Ticket(serverWorld, chunkPos, sourceWorld, sourcePos);
        }

        public ServerLevel getLoadedWorld() {
            return this.loadedWorld;
        }
        public ChunkPos getLoadedChunkPos() {
            return this.loadedChunkPos;
        }
        public ServerLevel getSourceWorld() {
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
