package pw.rxj.iron_quarry.util;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class ComplexEnergyContainer {
    protected long amount = 0;
    private @Nullable Object platformStorage = null;

    public static ComplexEnergyContainer ofCapacity(long capacity) {
        return new ComplexEnergyContainer() {
            @Override
            public long getCapacity() {
                return capacity;
            }
        };
    }

    public long getCapacity() {
        return 0;
    }

    public long getMaxInsert(@Nullable Direction side) {
        return 0;
    }

    public long getMaxExtract(@Nullable Direction side) {
        return 0;
    }

    public void onFinalCommit() {
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

    @SuppressWarnings("unchecked")
    public synchronized <T> T getPlatformStorage(Function<ComplexEnergyContainer, T> factory) {
        if(this.platformStorage == null) this.platformStorage = factory.apply(this);

        return (T) this.platformStorage;
    }
}
