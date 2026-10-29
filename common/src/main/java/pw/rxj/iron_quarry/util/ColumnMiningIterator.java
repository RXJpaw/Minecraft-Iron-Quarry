package pw.rxj.iron_quarry.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.NoSuchElementException;
import java.util.function.IntPredicate;

public class ColumnMiningIterator implements IMiningIterator {
    private final int minX;
    private final int minY;
    private final int minZ;
    private final int maxY;
    private final int maxZ;
    private int x;
    private int y;
    private int z;

    private ColumnMiningIterator(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxY = maxY;
        this.maxZ = maxZ;

        this.x = maxX;
        this.y = maxY;
        this.z = maxZ;
    }
    public static ColumnMiningIterator of(BoundingBox box) {
        return new ColumnMiningIterator(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ());
    }

    @Override
    public boolean hasNext() {
        return this.x >= this.minX;
    }
    @Override
    public BlockPos next() {
        if(!this.hasNext()) throw new NoSuchElementException();

        BlockPos blockPos = new BlockPos(this.x, this.y, this.z);

        if(--this.y < this.minY) this.nextColumn();

        return blockPos;
    }
    @Override
    public boolean skipAirSection(IntPredicate hasOnlyAir) {
        if(!IMiningIterator.isSectionTop(this.y, this.maxY)) return false;
        if(!hasOnlyAir.test(this.y)) return false;

        this.y = IMiningIterator.belowSection(this.y);
        if(this.y < this.minY) this.nextColumn();

        return true;
    }

    private void nextColumn() {
        this.y = this.maxY;

        if(--this.z < this.minZ) {
            this.z = this.maxZ;
            this.x--;
        }
    }
}
