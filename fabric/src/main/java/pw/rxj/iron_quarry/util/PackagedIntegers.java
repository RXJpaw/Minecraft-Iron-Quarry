package pw.rxj.iron_quarry.util;

import org.w3c.dom.ranges.RangeException;

public class PackagedIntegers {
    public static final int MAX_SINGLE_VALUE = 255;
    public static final int MIN_SINGLE_VALUE = 0;


    private static int _write(int... integers) {
        long packedData = 0;

        for (int i = 0; i < integers.length; i++) {
            if(integers[i] > MAX_SINGLE_VALUE || integers[i] < MIN_SINGLE_VALUE) throw new RangeException((short) 0, "Value out of range.");

            packedData |= (long) integers[i] << (i * 8);
        }



        return (int) (packedData);
    }

    public static int write(int int1, int int2){
        return _write(int1, int2);
    }
    public static int write(int int1, int int2, int int3){
        return _write(int1, int2, int3);
    }
    public static int write(int int1, int int2, int int3, int int4){
        return _write(int1, int2, int3, int4);
    }

    public static int[] read(int packedInteger){
        int[] unpackedData = new int[4];

        long packedData = (long) packedInteger;

        int index = 0;
        while (packedData != 0) {
            long part = packedData & MAX_SINGLE_VALUE;
            unpackedData[index] = (int) part;
            packedData >>= 8;
            index++;
        }

        return unpackedData;
    }
}
