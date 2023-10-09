package pw.rxj.iron_quarry.util;

import net.minecraft.nbt.NbtCompound;
import pw.rxj.iron_quarry.records.IndexedValue;
import pw.rxj.iron_quarry.types.Face;
import pw.rxj.iron_quarry.types.IoState;

import java.util.*;

public class MachineConfiguration {
    private final HashMap<Face, IoState> SidedIoConfig;

    public MachineConfiguration() {
        SidedIoConfig = new HashMap<>();
    }

    public void setIoState(Face face, IoState ioState){
        SidedIoConfig.put(face, ioState);
        onIoUpdated(face, ioState);
        markDirty();
    }
    public IoState getIoState(Face face) {
        IoState ioState = SidedIoConfig.get(face);

        return ioState == null ? IoState.BLOCKED : ioState;
    }
    public Object getLinkedIo(IoState ioState) {
        IndexedValue<Object> ioStateCheck = this.getUsedIoStates().get(ioState);
        if(ioStateCheck == null) return null;

        return ioStateCheck.value();
    }
    public Object getLinkedIo(Face face) {
        return this.getLinkedIo(this.getIoState(face));
    }

    /**
     * This method must be overridden to provide custom implementation.
     * Throws nothing for this to be used as empty state on initialisation.
     */
    public IndexedMap<IoState, Object> getUsedIoStates(){
        return new IndexedMap<>();
    }


    private IndexedValue<List<IoState>> getAvailableIoStatesAndIndex(Face face){
        IndexedMap<IoState, Object> usedIoStates = this.getUsedIoStates();
        if(usedIoStates.size() <= 1) return null;

        IoState ioState = this.getIoState(face);
        IndexedValue<Object> indexedValue = usedIoStates.get(ioState);
        if(indexedValue == null) return null;

        List<IoState> availableIoStates = usedIoStates.keys();
        int ioStateIndex = indexedValue.index();

        return new IndexedValue<>(ioStateIndex, availableIoStates);
    }
    public IoState getNextIoState(Face face){
        IndexedValue<List<IoState>> indexedValue = this.getAvailableIoStatesAndIndex(face);
        if(indexedValue == null) return IoState.BLOCKED;

        List<IoState> availableIoStates = indexedValue.value();
        int ioStateIndex = indexedValue.index();

        return ioStateIndex + 1 == availableIoStates.size() ? availableIoStates.get(0) : availableIoStates.get(ioStateIndex + 1);
    }
    public IoState getPreviousIoState(Face face){
        IndexedValue<List<IoState>> indexedValue = this.getAvailableIoStatesAndIndex(face);
        if(indexedValue == null) return IoState.BLOCKED;

        List<IoState> availableIoStates = indexedValue.value();
        int ioStateIndex = indexedValue.index();

        return ioStateIndex == 0 ? availableIoStates.get(availableIoStates.size() - 1) : availableIoStates.get(ioStateIndex - 1);
    }

    public NbtCompound write() {
        NbtCompound config = new NbtCompound();
        NbtCompound sidedIo = new NbtCompound();

        SidedIoConfig.forEach((face, ioState) -> {
            sidedIo.putString(face.getName(), ioState.getName());
        });

        config.put("SidedIo", sidedIo);

        return config;
    }

    public void read(NbtCompound config) {
        NbtCompound sidedIoConfig = config.getCompound("SidedIo");

        sidedIoConfig.getKeys().forEach((key) -> {
            Face face = Face.from(key);
            if(face == null) return;

            String ioName = sidedIoConfig.getString(key);
            IoState ioState = IoState.from(ioName);
            if(ioState == null) return;

            SidedIoConfig.put(face, ioState);
            onIoUpdated(face, ioState);
        });
    }

    public void markDirty(){ }
    public void onIoUpdated(Face face, IoState ioState) { }
}
