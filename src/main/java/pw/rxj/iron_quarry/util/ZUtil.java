package pw.rxj.iron_quarry.util;

import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

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
}
