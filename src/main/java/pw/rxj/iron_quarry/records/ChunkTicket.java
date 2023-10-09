package pw.rxj.iron_quarry.records;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public record ChunkTicket(ServerWorld serverWorld, ChunkPos chunkPos, BlockPos sourcePos) { }