package pw.rxj.iron_quarry.util;

import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.OrderedText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.HashSet;

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

    public static double normDeg(double degrees) {
        return (degrees % 360.0d) / 360.0d;
    }
    public static float normDeg(float degrees) {
        return (degrees % 360.0f) / 360.0f;
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
    public static String toString(OrderedText orderedText) {
        StringBuilder builder = new StringBuilder();

        orderedText.accept((index, style, codePoint) -> {
            builder.append(Character.toChars(codePoint));
            return true;
        });

        return builder.toString();
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

    public static Vec3d toVec3d(BlockPos blockPos) {
        return new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public static Direction getFacingDirection(float yaw, float pitch) {
        if(pitch < -45.0F) {
            return Direction.UP;
        } else if(pitch > 45.0F) {
            return Direction.DOWN;
        } else {
            return Direction.fromRotation(yaw);
        }
    }

    public static BlockPos limitInsideWorldBounds(BlockPos blockPos, World world) {
        WorldBorder worldBorder = world.getWorldBorder();

        double x = Math.min(Math.max(blockPos.getX(), worldBorder.getBoundWest()), worldBorder.getBoundEast() - 1);
        double y = Math.min(Math.max(blockPos.getY(), world.getBottomY()), world.getTopY());
        double z = Math.min(Math.max(blockPos.getZ(), worldBorder.getBoundNorth()), worldBorder.getBoundSouth() - 1);

        return new BlockPos(x, y, z);
    }
    public static boolean isInsideWorldBounds(BlockPos blockPos, World world) {
        if(blockPos.getY() > world.getTopY()) return false;
        if(blockPos.getY() < world.getBottomY()) return false;

        return world.getWorldBorder().contains(blockPos);
    }

    public static HashSet<Integer> getNSpacedIndexes(int listSize, int n) {
        if(listSize <= 0 || n <= 0) return new HashSet<>();

        HashSet<Integer> indexSet = new HashSet<>();
        float step = (listSize - 1.0F) / (n - 1.0F);

        for (int i = 0; i < n; i++) {
            int index = Math.round(step * i);
            indexSet.add(index);
        }

        return indexSet;
    }
}
