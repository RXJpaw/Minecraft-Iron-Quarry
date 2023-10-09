package pw.rxj.iron_quarry.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SingleByteMap {
    public record ByteMapItem(int id, int first, int second) { }
    private final List<ByteMapItem> byteList = new ArrayList<>();
    private int countingId = 0;

    public void put(int first, int second) {
        for (int f = 0; f < first; f++) {
            for (int s = 0; s < second; s++) {
                byteList.add(new ByteMapItem(countingId, f, s));
            }
        }

        countingId++;
    }

    public SingleByteMap with(int first, int second) {
        this.put(first, second);

        return this;
    }

    public Optional<Byte> toByte(int id, int first, int second) {
        for (int i = 0; i < byteList.size(); i++) {
            ByteMapItem integerList = byteList.get(i);

            if(integerList.id() == id && integerList.first == first && integerList.second == second) return Optional.of((byte) (i - 128));
        }

        return Optional.empty();
    }

    public ByteMapItem get(byte index) {
        return byteList.get(index + 128);
    }
}
