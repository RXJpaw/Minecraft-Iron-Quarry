package pw.rxj.iron_quarry.util;

import net.minecraft.util.RandomSource;

public class FortuneUtil {
    public static int rollLevel(float level, RandomSource random) {
        int floorLevel = (int) Math.floor(level);

        return random.nextFloat() < level - floorLevel ? floorLevel + 1 : floorLevel;
    }
}
