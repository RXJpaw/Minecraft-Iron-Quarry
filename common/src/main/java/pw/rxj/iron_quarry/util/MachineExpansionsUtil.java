package pw.rxj.iron_quarry.util;

import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import pw.rxj.iron_quarry.block.QuarryBlock;
import pw.rxj.iron_quarry.blockentity.QuarryBlockEntity;
import pw.rxj.iron_quarry.item.ExpansionCardItem;
import pw.rxj.iron_quarry.item.QuarryMonitorItem;
import pw.rxj.iron_quarry.screen.QuarryBlockScreenHandler;

import java.util.function.Consumer;

public class MachineExpansionsUtil {
    private boolean isInterdimensional = false;
    private boolean hasLockedExpansions = false;

    private MachineExpansionsUtil() { }
    private MachineExpansionsUtil(QuarryBlock quarryBlock, Container expansionInventory) {
        for (int i = 0; i < expansionInventory.getContainerSize(); i++) {
            ItemStack stack = expansionInventory.getItem(i);

            if (stack.getItem() instanceof ExpansionCardItem expansionCardItem) {
                if(i >= quarryBlock.getExpansionLimit()) {
                    this.hasLockedExpansions = true; continue;
                }

                switch (expansionCardItem.getType()) {
                    case INTERDIMENSIONAL -> this.isInterdimensional = true;
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

        return new MachineExpansionsUtil(quarryBlock, blockEntity.ExpansionsInventory);
    }
    public static MachineExpansionsUtil from(QuarryBlockScreenHandler screenHandler) {
        return new MachineExpansionsUtil(screenHandler.getQuarryBlock(), screenHandler.getExpansionsInventory());
    }
    public static MachineExpansionsUtil from(ItemStack quarryMonitorStack) {
        if(!(quarryMonitorStack.getItem() instanceof QuarryMonitorItem quarryMonitorItem)) return empty();

        ItemStack quarryStack = quarryMonitorItem.getCachedQuarryStack(quarryMonitorStack);
        if(!(ZUtil.getBlockOrItem(quarryStack) instanceof QuarryBlock quarryBlock)) return empty();

        return new MachineExpansionsUtil(quarryBlock, quarryBlock.getExpansionInventory(quarryStack));
    }

    public boolean isInterdimensional() {
        return this.isInterdimensional;
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
