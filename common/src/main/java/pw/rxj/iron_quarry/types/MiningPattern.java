package pw.rxj.iron_quarry.types;

import net.minecraft.world.level.levelgen.structure.BoundingBox;
import pw.rxj.iron_quarry.util.ColumnMiningIterator;
import pw.rxj.iron_quarry.util.GilbertMiningIterator;
import pw.rxj.iron_quarry.util.IMiningIterator;
import pw.rxj.iron_quarry.util.SnakeMiningIterator;

public enum MiningPattern {
    COLUMN("column", (box, seed) -> ColumnMiningIterator.of(box)),
    SNAKE("snake", (box, seed) -> SnakeMiningIterator.of(box, seed)),
    GILBERT("gilbert", (box, seed) -> GilbertMiningIterator.of(box));

    private final String name;
    private final IMiningIterator.Factory factory;

    private MiningPattern(String name, IMiningIterator.Factory factory) {
        this.name = name;
        this.factory = factory;
    }

    public String getName() {
        return this.name;
    }

    public IMiningIterator create(BoundingBox box, long seed) {
        return this.factory.create(box, seed);
    }
}
