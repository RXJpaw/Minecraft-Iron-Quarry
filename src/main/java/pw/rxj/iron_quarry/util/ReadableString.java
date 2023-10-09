package pw.rxj.iron_quarry.util;

import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public class ReadableString {
    public static Optional<String> from(BlockPos blockPos) {
        if(blockPos == null) return Optional.empty();

        return String.format("%s, %s, %s", blockPos.getX(), blockPos.getY(), blockPos.getZ()).describeConstable();
    }
    public static Optional<Text> textFrom(BlockPos blockPos) {
        return from(blockPos).map(Text::of);
    }
}
