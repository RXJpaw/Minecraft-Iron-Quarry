package pw.rxj.iron_quarry.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.function.IntPredicate;

public class HilbertMiningIterator implements IMiningIterator {
    private static final int SIZE = 16;
    private static final int CELLS = SIZE * SIZE;
    private static final int[] CELL_BY_STEP = createCells();

    private final int baseX;
    private final int baseZ;
    private final int[] cells;
    private final int minY;
    private final int maxY;
    private int y;
    private int step = 0;

    private HilbertMiningIterator(int baseX, int baseZ, int[] cells, int minY, int maxY) {
        this.baseX = baseX;
        this.baseZ = baseZ;
        this.cells = cells;
        this.minY = minY;
        this.maxY = maxY;

        this.y = maxY;
    }
    public static HilbertMiningIterator of(BoundingBox box) {
        int baseX = box.minX() & -SIZE;
        int baseZ = box.minZ() & -SIZE;

        int[] cells = new int[CELLS];
        int count = 0;

        for (int cell : CELL_BY_STEP) {
            int x = baseX + cell / SIZE;
            int z = baseZ + cell % SIZE;
            if(x < box.minX() || x > box.maxX() || z < box.minZ() || z > box.maxZ()) continue;

            cells[count++] = cell;
        }

        return new HilbertMiningIterator(baseX, baseZ, Arrays.copyOf(cells, count), box.minY(), box.maxY());
    }

    private static int[] createCells() {
        int[] cells = new int[CELLS];

        for (int d = 0; d < CELLS; d++) {
            int x = 0;
            int z = 0;
            int t = d;

            for (int s = 1; s < SIZE; s *= 2) {
                int rx = 1 & (t / 2);
                int rz = 1 & (t ^ rx);

                if(rz == 0) {
                    if(rx == 1) {
                        x = s - 1 - x;
                        z = s - 1 - z;
                    }

                    int swap = x;
                    x = z;
                    z = swap;
                }

                x += s * rx;
                z += s * rz;
                t /= 4;
            }

            cells[d] = x * SIZE + z;
        }

        return cells;
    }

    @Override
    public boolean hasNext() {
        return this.cells.length > 0 && this.y >= this.minY;
    }
    @Override
    public BlockPos next() {
        if(!this.hasNext()) throw new NoSuchElementException();

        boolean reversed = (this.maxY - this.y) % 2 == 1;
        int cell = this.cells[reversed ? this.cells.length - 1 - this.step : this.step];
        BlockPos blockPos = new BlockPos(this.baseX + cell / SIZE, this.y, this.baseZ + cell % SIZE);

        if(++this.step >= this.cells.length) {
            this.step = 0;
            this.y--;
        }

        return blockPos;
    }
    @Override
    public boolean skipAirSection(IntPredicate hasOnlyAir) {
        if(this.step != 0 || !IMiningIterator.isSectionTop(this.y, this.maxY)) return false;
        if(!hasOnlyAir.test(this.y)) return false;

        this.y = IMiningIterator.belowSection(this.y);

        return true;
    }
}
