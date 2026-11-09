package pw.rxj.iron_quarry.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.SplittableRandom;
import java.util.function.IntPredicate;

public class SnakeMiningIterator implements IMiningIterator {
    private static final int INITIAL_MOVES_PER_CELL = 32;
    private static final int LAYER_MOVES_PER_CELL = 1;

    private final SplittableRandom random;
    private final int minX;
    private final int minZ;
    private final int depth;
    private final int minY;
    private final int maxY;
    private final int[][] neighbors;
    private final int[] path;
    private final int[] indexOf;
    private int y;
    private int step = 0;
    private boolean pendingLayer = false;

    private SnakeMiningIterator(BoundingBox box, long seed) {
        this.random = new SplittableRandom(seed);
        this.minX = box.minX();
        this.minZ = box.minZ();
        this.depth = box.getZSpan();
        this.minY = box.minY();
        this.maxY = box.maxY();

        this.y = this.maxY;

        int width = box.getXSpan();
        int cells = width * this.depth;

        this.neighbors = new int[cells][];
        this.path = new int[cells];
        this.indexOf = new int[cells];

        this.fillNeighbors(width);
        this.fillSerpentinePath(width);
        this.shuffle(cells * INITIAL_MOVES_PER_CELL);
    }
    public static SnakeMiningIterator of(BoundingBox box, long seed) {
        return new SnakeMiningIterator(box, seed);
    }

    private int cell(int x, int z) {
        return x * this.depth + z;
    }
    private void fillNeighbors(int width) {
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < this.depth; z++) {
                int[] candidates = new int[4];
                int count = 0;

                if(x > 0) candidates[count++] = this.cell(x - 1, z);
                if(x < width - 1) candidates[count++] = this.cell(x + 1, z);
                if(z > 0) candidates[count++] = this.cell(x, z - 1);
                if(z < this.depth - 1) candidates[count++] = this.cell(x, z + 1);

                this.neighbors[this.cell(x, z)] = Arrays.copyOf(candidates, count);
            }
        }
    }
    private void fillSerpentinePath(int width) {
        int i = 0;
        for (int x = 0; x < width; x++) {
            for (int k = 0; k < this.depth; k++) {
                int z = x % 2 == 0 ? k : this.depth - 1 - k;

                this.path[i] = this.cell(x, z);
                this.indexOf[this.path[i]] = i;
                i++;
            }
        }
    }
    private void shuffle(int moves) {
        if(!this.canBackbite()) return;

        for (int move = 0; move < moves; move++) {
            if(this.random.nextBoolean()) {
                this.backbiteTail();
            } else {
                this.backbiteHead();
            }
        }
    }

    private boolean canBackbite() {
        return this.path.length >= 3;
    }

    private void backbiteTail() {
        int last = this.path.length - 1;
        int[] candidates = this.neighbors[this.path[last]];
        int index = this.indexOf[candidates[this.random.nextInt(candidates.length)]];
        if(index == last - 1) return;

        this.reverse(index + 1, last);
    }
    private void backbiteHead() {
        int[] candidates = this.neighbors[this.path[0]];
        int index = this.indexOf[candidates[this.random.nextInt(candidates.length)]];
        if(index == 1) return;

        this.reverse(0, index - 1);
    }
    private void reverse(int from, int to) {
        while(from < to) {
            int swap = this.path[from];
            this.path[from] = this.path[to];
            this.path[to] = swap;

            this.indexOf[this.path[from]] = from;
            this.indexOf[this.path[to]] = to;

            from++;
            to--;
        }
    }

    private void nextLayer() {
        this.reverse(0, this.path.length - 1);
        if(!this.canBackbite()) return;

        for (int move = 0; move < this.path.length * LAYER_MOVES_PER_CELL; move++) {
            this.backbiteTail();
        }
    }

    @Override
    public boolean hasNext() {
        return this.y >= this.minY;
    }
    @Override
    public BlockPos next() {
        if(!this.hasNext()) throw new NoSuchElementException();

        if(this.pendingLayer) {
            this.pendingLayer = false;
            this.nextLayer();
        }

        int cell = this.path[this.step];
        BlockPos blockPos = new BlockPos(this.minX + cell / this.depth, this.y, this.minZ + cell % this.depth);

        if(++this.step >= this.path.length) {
            this.step = 0;
            this.y--;
            this.pendingLayer = true;
        }

        return blockPos;
    }
    @Override
    public boolean skipAirSection(IntPredicate hasOnlyAir) {
        if(this.step != 0 || !IMiningIterator.isSectionTop(this.y, this.maxY)) return false;
        if(!hasOnlyAir.test(this.y)) return false;

        this.y = IMiningIterator.belowSection(this.y);
        this.pendingLayer = true;

        return true;
    }
}
