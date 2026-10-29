package pw.rxj.iron_quarry.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

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

    static boolean isSectionTop(int y, int maxY) {
        return y == maxY || SectionPos.sectionRelative(y) == SectionPos.SECTION_MAX_INDEX;
    }
    static int belowSection(int y) {
        return SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(y)) - 1;
    }
}
