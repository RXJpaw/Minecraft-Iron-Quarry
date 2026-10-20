package pw.rxj.iron_quarry.util;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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

    public void read(CompoundTag data){
        this.amount = data.getLongOr("Stored", 0L);
    }
    public void read(long amount){
        this.amount = amount;
    }
    public CompoundTag write(){
        CompoundTag data = new CompoundTag();

        data.putLong("Stored", this.amount);

        return data;
    }
}
