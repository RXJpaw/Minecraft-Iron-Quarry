package pw.rxj.iron_quarry.util;

import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.block.QuarryBlock;
import pw.rxj.iron_quarry.blockentity.QuarryBlockEntity;

import java.util.function.Supplier;

public class QuarryEnergyContainer extends ComplexEnergyContainer {
    private final @Nullable QuarryBlockEntity ref;
    private final Supplier<QuarryBlock> quarryBlock;

    private QuarryEnergyContainer(@Nullable QuarryBlockEntity ref, Supplier<QuarryBlock> quarryBlock) {
        this.ref = ref;
        this.quarryBlock = quarryBlock;
    }
    public static QuarryEnergyContainer noRef(QuarryBlock quarryBlock) {
        return new QuarryEnergyContainer(null, () -> quarryBlock);
    }
    public static QuarryEnergyContainer ofRef(QuarryBlockEntity ref) {
        return new QuarryEnergyContainer(ref, ref::getQuarryBlock);
    }

    @Override
    public long getCapacity() {
        QuarryBlock quarryBlock = this.quarryBlock.get();
        if(quarryBlock == null) return 0;

        return quarryBlock.getEnergyCapacity();
    }

    @Override
    public long getMaxInsert(@Nullable Direction side) {
        if(this.ref == null) return 0;

        return this.getCapacity();
    }

    @Override
    public void onFinalCommit() {
        if(this.ref == null) return;

        this.ref.setChanged();
    }
}
