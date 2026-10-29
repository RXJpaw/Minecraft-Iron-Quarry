package pw.rxj.iron_quarry.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.IntPredicate;

public interface IMiningIterator extends Iterator<BlockPos> {
    IMiningIterator EMPTY = new IMiningIterator() {
        @Override
        public boolean hasNext() {
            return false;
        }
        @Override
        public BlockPos next() {
            throw new NoSuchElementException();
        }
        @Override
        public boolean skipAirSection(IntPredicate hasOnlyAir) {
            return false;
        }
    };

    boolean skipAirSection(IntPredicate hasOnlyAir);

    @FunctionalInterface
    interface Factory {
        IMiningIterator create(BoundingBox box, long seed);
    }

    static long seedOf(BlockPos quarryPos, ChunkPos chunkPos) {
        return RandomSupport.mixStafford13(quarryPos.asLong() ^ RandomSupport.mixStafford13(chunkPos.pack()));
    }
    static boolean isSectionTop(int y, int maxY) {
        return y == maxY || SectionPos.sectionRelative(y) == SectionPos.SECTION_MAX_INDEX;
    }
    static int belowSection(int y) {
        return SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(y)) - 1;
    }
}
