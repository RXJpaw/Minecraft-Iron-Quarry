package pw.rxj.iron_quarry.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.component.DataComponentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.data.ZComponents;
import pw.rxj.iron_quarry.records.IndexedValue;
import pw.rxj.iron_quarry.types.Face;
import pw.rxj.iron_quarry.types.IoState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MachineConfiguration {
    private final HashMap<Face, IoState> SidedIoConfig;

    public MachineConfiguration() {
        this.SidedIoConfig = new HashMap<>();
    }
    public static MachineConfiguration empty() {
        return new MachineConfiguration();
    }

    public void setIoState(Face face, IoState ioState){
        if(ioState.equals(IoState.BLOCKED)) {
            SidedIoConfig.remove(face);
        } else {
            this.SidedIoConfig.put(face, ioState);
        }

        onIoUpdated(face, ioState);
        markDirty();
    }
    public IoState getIoState(Face face) {
        IoState ioState = this.SidedIoConfig.get(face);

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

    public void markDirty(){ }
    public void onIoUpdated(Face face, IoState ioState) { }

   //nbt

    public NbtCompound write() {
        NbtCompound config = new NbtCompound();
        NbtCompound sidedIo = new NbtCompound();

        for(Map.Entry<Face, IoState> entry : SidedIoConfig.entrySet()){
            Face face = entry.getKey();
            IoState ioState = entry.getValue();

            sidedIo.putString(face.getName(), ioState.getName());
        }

        config.put("SidedIo", sidedIo);

        return config;
    }
    public void read(NbtCompound config) {
        NbtCompound sidedIoConfig = config.getCompound("SidedIo");

        this.SidedIoConfig.clear();

        for (String key : sidedIoConfig.getKeys()) {
            Face face = Face.from(key);
            if(face == null) continue;

            String ioName = sidedIoConfig.getString(key);
            IoState ioState = IoState.from(ioName);
            if(ioState == null) continue;

            SidedIoConfig.put(face, ioState);
            onIoUpdated(face, ioState);
        }
    }

    //components

    public static final PacketCodec<RegistryByteBuf, MachineConfiguration> PACKET_CODEC = PacketCodec.of(MachineConfiguration::write, MachineConfiguration::new);
    public static final Codec<MachineConfiguration> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(Codec.INT.listOf().listOf().fieldOf("sided_io").forGetter(MachineConfiguration::toIntList))
                    .apply(instance, MachineConfiguration::new)
    );
    public static final DataComponentType<MachineConfiguration> COMPONENT = ZComponents.register("machine_configuration", (builder) -> builder
            .codec(CODEC)
            .packetCodec(PACKET_CODEC)
            .cache());

    private MachineConfiguration(RegistryByteBuf buf) {
        this();

        byte ioStates = buf.readByte();
        while (ioStates-- > 0) {
            this.SidedIoConfig.put(
                    Face.from(buf.readByte()),
                    IoState.from(buf.readByte())
            );
        }
    }
    private void write(RegistryByteBuf buf) {
        buf.writeByte(this.SidedIoConfig.size());

        for(Map.Entry<Face, IoState> pair : this.SidedIoConfig.entrySet()){
            buf.writeByte(pair.getKey().getId());
            buf.writeByte(pair.getValue().getId());
        }
    }

    private MachineConfiguration(List<List<Integer>> intList) {
        this();

        for(List<Integer> pairList : intList){
            this.SidedIoConfig.put(
                    Face.from(pairList.get(0)),
                    IoState.from(pairList.get(1)));
        }
    }
    private List<List<Integer>> toIntList() {
        return this.SidedIoConfig.entrySet().stream()
                .map(pair -> List.of(
                        pair.getKey().getId(),
                        pair.getValue().getId()))
                .collect(Collectors.toList());
    }

    public void read(@Nullable MachineConfiguration machineConfiguration) {
        if(machineConfiguration == null) return;

        this.SidedIoConfig.clear();
        this.SidedIoConfig.putAll(machineConfiguration.SidedIoConfig);
    }
}
