package pw.rxj.iron_quarry.util;

import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;

public class ZUtil {
    public static Object getBlockOrItem(ItemStack stack) {
        if(stack.getItem() instanceof BlockItem blockItem) {
            return blockItem.getBlock();
        }

        return stack.getItem();
    }

    public static String expandableFixedFloat(float input) {
        if(Screen.hasShiftDown()) return new DecimalFormat("#,##0.00").format(input);

        if(input < 10 && input > -10) return new DecimalFormat("#,##0.00").format(input);
        if(input < 100 && input > -100) return new DecimalFormat("#,##0.0").format(input);

        return new DecimalFormat("#,##0").format(input);
    }

    /** Shoutout to "Powah! (Rearchitected)"-developer Technici4n for including a {@link net.minecraft.block.BlockEntityProvider} with every of their non-Block Entity blocks. */
    public static boolean isActualBlockEntity(World world, BlockPos blockPos) {
        return isActualBlockEntity(world, world.getBlockState(blockPos), blockPos);
    }
    public static boolean isActualBlockEntity(World world, BlockState blockState, BlockPos blockPos) {
        if(!blockState.hasBlockEntity()) return false;
        return world.getBlockEntity(blockPos) != null;
    }
    public static boolean isActualBlockEntity(WorldChunk worldChunk, BlockPos blockPos) {
        return isActualBlockEntity(worldChunk, worldChunk.getBlockState(blockPos), blockPos);
    }
    public static boolean isActualBlockEntity(WorldChunk worldChunk, BlockState blockState, BlockPos blockPos) {
        if(!blockState.hasBlockEntity()) return false;
        return worldChunk.getBlockEntity(blockPos) != null;
    }

    public static double bounceBack(double input, double range) {
        double doubleRange = range * 2;
        input = Math.abs(input);

        double position = input % doubleRange;
        return position >= range ? doubleRange - position : position;
    }

    public static String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) return input;

        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    public static boolean equals(@Nullable RegistryKey<?> key1, @Nullable RegistryKey<?> key2) {
        Identifier registry1 = key1 == null ? null : key1.getRegistry();
        Identifier registry2 = key2 == null ? null : key2.getRegistry();
        Identifier value1 = key1 == null ? null : key1.getValue();
        Identifier value2 = key2 == null ? null : key2.getValue();

        return registry1 == registry2 && value1 == value2;
    }
    public static String toString(RegistryKey<?> registryKey) {
        String registry = registryKey.getRegistry().toString();
        String value = registryKey.getValue().toString();

        return registry + "/" + value;
    }
    public static <T> @Nullable RegistryKey<T> toRegistryKey(String string) {
        String[] split = string.split("/");
        if(split.length < 2) return null;

        Identifier registry = Identifier.tryParse(split[0]);
        if(registry == null) return null;
        Identifier value = Identifier.tryParse(split[1]);
        if(value == null) return null;

        return RegistryKey.of(RegistryKey.ofRegistry(registry), value);
    }
}
