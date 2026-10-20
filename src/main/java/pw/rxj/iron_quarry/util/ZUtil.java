package pw.rxj.iron_quarry.util;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.LockCode;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public class ZUtil {
    public static ItemLike getBlockOrItem(ItemStack stack) {
        if(stack.getItem() instanceof BlockItem blockItem) {
            return blockItem.getBlock();
        }

        return stack.getItem();
    }

    public static String expandableFixedFloat(float input) {
        if(Minecraft.getInstance().hasShiftDown()) return ReadableString.getDecimalFormatter("#,##0.00").format(input);

        float inputAbs = Math.abs(input);
        String inputSign = input >= 0 ? "" : "-";

        String _10 = ReadableString.getDecimalFormatter("#,##0.00").format(inputAbs);
        if(_10.length() <= 4) return inputSign + _10;

        String _100 = ReadableString.getDecimalFormatter("#,##0.0").format(inputAbs);
        if(_100.length() <= 4) return inputSign + _100;

        return ReadableString.cIntFrom(input);
    }
    public static <T extends Number> String expandableFixedInt(T number) {
        if(Minecraft.getInstance().hasShiftDown()) return ReadableString.intFrom(number);

        return ReadableString.cIntFrom(number);
    }

    public static <E extends Enum<E>> @Nullable E safeEnumConstant(Class<E> clazz, int index) {
        E[] enumList = clazz.getEnumConstants();
        if(index >= enumList.length) return null;

        return enumList[index];
    }

    public static @Nullable BlockEntity getBlockEntity(LevelChunk worldChunk, BlockState blockState, BlockPos blockPos) {
        if(!blockState.hasBlockEntity()) return null;
        return worldChunk.getBlockEntity(blockPos);
    }

    public static @Nullable RandomizableContainerBlockEntity getUnlockedLootableContainer(@Nullable BlockEntity blockEntity) {
        if(blockEntity instanceof RandomizableContainerBlockEntity lootableBlockEntity) {
            Level world = lootableBlockEntity.getLevel();
            if(world == null) return null;

            CompoundTag nbtCompound = lootableBlockEntity.saveCustomOnly(world.registryAccess());
            if(nbtCompound.isEmpty()) return null;

            LockCode lock = LockCode.fromTag(TagValueInput.create(ProblemReporter.DISCARDING, world.registryAccess(), nbtCompound));
            if(!lock.equals(LockCode.NO_LOCK)) return null;

            return lootableBlockEntity;
        }

        return null;
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

    public static boolean equals(@Nullable ResourceKey<?> key1, @Nullable ResourceKey<?> key2) {
        Identifier registry1 = key1 == null ? null : key1.registry();
        Identifier registry2 = key2 == null ? null : key2.registry();
        Identifier value1 = key1 == null ? null : key1.identifier();
        Identifier value2 = key2 == null ? null : key2.identifier();

        return registry1 == registry2 && value1 == value2;
    }
    public static String toString(ResourceKey<?> registryKey) {
        String registry = registryKey.registry().toString();
        String value = registryKey.identifier().toString();

        return registry + "/" + value;
    }
    public static String toString(FormattedCharSequence orderedText) {
        StringBuilder builder = new StringBuilder();

        orderedText.accept((index, style, codePoint) -> {
            builder.append(Character.toChars(codePoint));
            return true;
        });

        return builder.toString();
    }
    public static <T> @Nullable ResourceKey<T> toRegistryKey(String string) {
        String[] split = string.split("/");
        if(split.length < 2) return null;

        Identifier registry = Identifier.tryParse(split[0]);
        if(registry == null) return null;
        Identifier value = Identifier.tryParse(split[1]);
        if(value == null) return null;

        return ResourceKey.create(ResourceKey.createRegistryKey(registry), value);
    }

    public static Vec3 toVec3d(BlockPos blockPos) {
        return new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public static Direction getFacingDirection(float yaw, float pitch) {
        if(pitch < -45.0F) {
            return Direction.UP;
        } else if(pitch > 45.0F) {
            return Direction.DOWN;
        } else {
            return Direction.fromYRot(yaw);
        }
    }

    public static BlockPos limitInsideWorldBounds(BlockPos blockPos, Level world) {
        WorldBorder worldBorder = world.getWorldBorder();

        double x = Math.min(Math.max(blockPos.getX(), worldBorder.getMinX()), worldBorder.getMaxX() - 1);
        double y = Math.min(Math.max(blockPos.getY(), world.getMinY()), world.getMaxY() + 1);
        double z = Math.min(Math.max(blockPos.getZ(), worldBorder.getMinZ()), worldBorder.getMaxZ() - 1);

        return BlockPos.containing(x, y, z);
    }
    public static boolean isInsideWorldBounds(BlockPos blockPos, Level world) {
        if(blockPos.getY() > world.getMaxY() + 1) return false;
        if(blockPos.getY() < world.getMinY()) return false;

        return world.getWorldBorder().isWithinBounds(blockPos);
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

    public static Holder<Enchantment> getEnchantmentEntry(HolderLookup.Provider registryLookup, ResourceKey<Enchantment> enchantment) {
        return registryLookup.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantment);
    }
    public static int getEnchantmentLevel(ItemStack stack, ResourceKey<Enchantment> enchantment) {
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : stack.getEnchantments().entrySet()) {
            if(entry.getKey().is(enchantment)) return entry.getIntValue();
        }

        return 0;
    }
    public static int getCappedEnchantmentLevel(ItemStack stack, ResourceKey<Enchantment> enchantment) {
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : stack.getEnchantments().entrySet()) {
            if(entry.getKey().is(enchantment)) return Math.min(entry.getIntValue(), entry.getKey().value().getMaxLevel());
        }

        return 0;
    }

    public static List<Item> getItemsIn(TagKey<Item> tag) {
        List<Item> items = new ArrayList<>();
        BuiltInRegistries.ITEM.getTagOrEmpty(tag).forEach(entry -> items.add(entry.value()));

        return items;
    }
}
