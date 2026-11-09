package pw.rxj.iron_quarry.util;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.block.QuarryBlock;
import pw.rxj.iron_quarry.blockentity.QuarryBlockEntity;
import pw.rxj.iron_quarry.item.ExpansionCardItem;
import pw.rxj.iron_quarry.item.QuarryMonitorItem;
import pw.rxj.iron_quarry.screen.QuarryBlockScreenHandler;
import pw.rxj.iron_quarry.types.MiningPattern;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;

public class MachineExpansionsUtil {
    private boolean isInterdimensional = false;
    private boolean hasLockedExpansions = false;
    private final EnumSet<MiningPattern> miningPatterns = EnumSet.noneOf(MiningPattern.class);
    private final @Nullable ResourceKey<Level> quarryWorldKey;

    private MachineExpansionsUtil() {
        this.quarryWorldKey = null;
    }
    private MachineExpansionsUtil(QuarryBlock quarryBlock, Container expansionInventory, @Nullable ResourceKey<Level> quarryWorldKey) {
        this.quarryWorldKey = quarryWorldKey;

        for (int i = 0; i < expansionInventory.getContainerSize(); i++) {
            ItemStack stack = expansionInventory.getItem(i);

            if (stack.getItem() instanceof ExpansionCardItem expansionCardItem) {
                if(i >= quarryBlock.getExpansionLimit()) {
                    this.hasLockedExpansions = true; continue;
                }

                if(expansionCardItem.getType().getExpansion().isDisabled()) continue;

                switch (expansionCardItem.getType()) {
                    case INTERDIMENSIONAL -> this.isInterdimensional = true;
                    case MINING_PATTERN -> this.miningPatterns.add(expansionCardItem.getMiningPattern());
                }
            }
        }
    }

    public static MachineExpansionsUtil empty() {
        return new MachineExpansionsUtil();
    }
    public static MachineExpansionsUtil from(QuarryBlockEntity blockEntity) {
        QuarryBlock quarryBlock = blockEntity.getQuarryBlock();
        if(quarryBlock == null) return empty();

        return new MachineExpansionsUtil(quarryBlock, blockEntity.ExpansionsInventory, dimensionOf(blockEntity.getLevel()));
    }
    public static MachineExpansionsUtil from(QuarryBlockScreenHandler screenHandler) {
        return new MachineExpansionsUtil(screenHandler.getQuarryBlock(), screenHandler.getExpansionsInventory(), dimensionOf(screenHandler.getWorld()));
    }
    public static MachineExpansionsUtil from(ItemStack quarryMonitorStack) {
        if(!(quarryMonitorStack.getItem() instanceof QuarryMonitorItem quarryMonitorItem)) return empty();

        ItemStack quarryStack = quarryMonitorItem.getCachedQuarryStack(quarryMonitorStack);
        if(!(ZUtil.getBlockOrItem(quarryStack) instanceof QuarryBlock quarryBlock)) return empty();

        return new MachineExpansionsUtil(quarryBlock, quarryBlock.getExpansionInventory(quarryStack), quarryMonitorItem.getWorldRegistryKey(quarryMonitorStack).orElse(null));
    }
    private static @Nullable ResourceKey<Level> dimensionOf(@Nullable Level world) {
        return world == null ? null : world.dimension();
    }

    public boolean isInterdimensional() {
        return this.isInterdimensional;
    }
    public boolean canMineIn(ResourceKey<Level> miningWorldKey) {
        return this.isInterdimensional() || miningWorldKey.equals(this.quarryWorldKey);
    }
    public MiningPattern getMiningPattern(ChunkPos chunkPos) {
        if(this.miningPatterns.isEmpty()) return MiningPattern.COLUMN;

        return List.copyOf(this.miningPatterns).get(Math.floorMod(chunkPos.x() + chunkPos.z(), this.miningPatterns.size()));
    }

    public boolean hasLockedExpansions() {
        return this.hasLockedExpansions;
    }
    public boolean hasConflicts() {
        return this.hasLockedExpansions();
    }
    public void appendConflictTooltip(Consumer<Component> textConsumer) {
        if(this.hasLockedExpansions()) {
            textConsumer.accept(ReadableString.translatable("item.iron_quarry.conflict.locked_expansion"));
        }
    }
}
