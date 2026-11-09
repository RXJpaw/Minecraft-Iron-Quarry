// SPDX-License-Identifier: BSD-2-Clause
// Copyright (c) 2018 Jakub Červený

package pw.rxj.iron_quarry.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.NoSuchElementException;
import java.util.function.IntPredicate;

public class GilbertMiningIterator implements IMiningIterator {
    private final int minX;
    private final int minZ;
    private final int[] cells;
    private final int minY;
    private final int maxY;
    private int y;
    private int step = 0;

    private GilbertMiningIterator(int minX, int minZ, int[] cells, int minY, int maxY) {
        this.minX = minX;
        this.minZ = minZ;
        this.cells = cells;
        this.minY = minY;
        this.maxY = maxY;

        this.y = maxY;
    }
    public static GilbertMiningIterator of(BoundingBox box) {
        int width = box.getXSpan();
        int depth = box.getZSpan();
        int[] cells = new int[width * depth];

        if(width >= depth) {
            generate(0, 0, width, 0, 0, depth, cells, 0);
        } else {
            generate(0, 0, 0, depth, width, 0, cells, 0);
        }

        return new GilbertMiningIterator(box.minX(), box.minZ(), cells, box.minY(), box.maxY());
    }

    private static int generate(int x, int z, int ax, int az, int bx, int bz, int[] cells, int count) {
        int w = Math.abs(ax + az);
        int h = Math.abs(bx + bz);

        int dax = Integer.signum(ax);
        int daz = Integer.signum(az);
        int dbx = Integer.signum(bx);
        int dbz = Integer.signum(bz);

        if(h == 1) {
            for (int i = 0; i < w; i++) {
                cells[count++] = x << 4 | z;
                x += dax;
                z += daz;
            }

            return count;
        }
        if(w == 1) {
            for (int i = 0; i < h; i++) {
                cells[count++] = x << 4 | z;
                x += dbx;
                z += dbz;
            }

            return count;
        }

        int ax2 = Math.floorDiv(ax, 2);
        int az2 = Math.floorDiv(az, 2);
        int bx2 = Math.floorDiv(bx, 2);
        int bz2 = Math.floorDiv(bz, 2);

        int w2 = Math.abs(ax2 + az2);
        int h2 = Math.abs(bx2 + bz2);

        if(2 * w > 3 * h) {
            if(w2 % 2 != 0 && w > 2) {
                ax2 += dax;
                az2 += daz;
            }

            count = generate(x, z, ax2, az2, bx, bz, cells, count);
            return generate(x + ax2, z + az2, ax - ax2, az - az2, bx, bz, cells, count);
        }

        if(h2 % 2 != 0 && h > 2) {
            bx2 += dbx;
            bz2 += dbz;
        }

        count = generate(x, z, bx2, bz2, ax2, az2, cells, count);
        count = generate(x + bx2, z + bz2, ax, az, bx - bx2, bz - bz2, cells, count);
        return generate(x + (ax - dax) + (bx2 - dbx), z + (az - daz) + (bz2 - dbz), -bx2, -bz2, -(ax - ax2), -(az - az2), cells, count);
    }

    @Override
    public boolean hasNext() {
        return this.y >= this.minY;
    }
    @Override
    public BlockPos next() {
        if(!this.hasNext()) throw new NoSuchElementException();

        boolean reversed = (this.maxY - this.y) % 2 == 1;
        int cell = this.cells[reversed ? this.cells.length - 1 - this.step : this.step];
        BlockPos blockPos = new BlockPos(this.minX + (cell >> 4), this.y, this.minZ + (cell & 15));

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
