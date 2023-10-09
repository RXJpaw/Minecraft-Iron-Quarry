package pw.rxj.iron_quarry.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.base.SimpleSidedEnergyContainer;

public class ComplexEnergyContainer extends SimpleSidedEnergyContainer {
    @Override
    public long getCapacity() {
        return 0;
    }

    @Override
    public long getMaxInsert(@Nullable Direction side) {
        return 0;
    }

    @Override
    public long getMaxExtract(@Nullable Direction side) {
        return 0;
    }

    public long getStored() {
        return this.amount;
    }
    public void setStored(long amount) {
        this.amount = Math.max(Math.min(amount, this.getCapacity()), 0);
        this.onFinalCommit();
    }

    public void useEnergy(long amount){
        if (this.getStored() > amount) {
            this.setStored(this.getStored() - amount);
        } else {
            this.setStored(0);
        }
    }

    public long getFreeSpace() {
        return this.getCapacity() - this.getStored();
    }
    public float getFillPercent() {
        return (float) this.getStored() / this.getCapacity();
    }

    public void read(NbtCompound data){
        this.amount = data.getLong("Stored");
    }
    public NbtCompound write(){
        NbtCompound data = new NbtCompound();

        data.putLong("Stored", this.amount);

        return data;
    }
}
