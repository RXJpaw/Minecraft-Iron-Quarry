package pw.rxj.iron_quarry.util;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.block.QuarryBlock;
import pw.rxj.iron_quarry.blockentity.QuarryBlockEntity;
import pw.rxj.iron_quarry.data.QuarryBlockedDataComponent;
import pw.rxj.iron_quarry.data.QuarryOwnerComponent;
import pw.rxj.iron_quarry.item.DrillItem;
import pw.rxj.iron_quarry.item.QuarryMonitorItem;

import java.util.List;
import java.util.function.Consumer;

public class MachineWarningsUtil {
    public static final float ENERGY_WARNING_PCT = 0.3F;
    public static final float INV_WARNING_PCT = 0.7F;

    public static final int WARNING_QUARRY_UNKNOWN = 0;
    public static final int WARNING_OWNER_MISSING = 1;
    public static final int WARNING_MINING_DENIED = 2;
    public static final int WARNING_DRILL_EMPTY = 3;
    public static final int WARNING_ENERGY_DEPLETED = 4;
    public static final int WARNING_INV_FULL = 5;
    public static final int WARNING_ENERGY_30 = 6;
    public static final int WARNING_INV_70 = 7;

    public static final List<Integer> CRITICAL_FLAG_LIST = List.of(
            WARNING_QUARRY_UNKNOWN,
            WARNING_OWNER_MISSING,
            WARNING_MINING_DENIED,
            WARNING_DRILL_EMPTY,
            WARNING_ENERGY_DEPLETED,
            WARNING_INV_FULL
    );
    public static final int CRITICAL_FLAG = toFlag(CRITICAL_FLAG_LIST);

    private static final MachineWarningsUtil UNKNOWN = new MachineWarningsUtil(1 << WARNING_QUARRY_UNKNOWN, null, MachineUpgradesUtil.from(List.of()));

    private final int flag;
    private final @Nullable QuarryBlockedDataComponent blocked;
    private final MachineUpgradesUtil upgradesUtil;

    private MachineWarningsUtil(int flag, @Nullable QuarryBlockedDataComponent blocked, MachineUpgradesUtil upgradesUtil) {
        this.flag = flag;
        this.blocked = blocked;
        this.upgradesUtil = upgradesUtil;
    }
    private MachineWarningsUtil(QuarryBlock quarryBlock, long energyStored, ComplexInventory outputInventory, ItemStack drillStack, List<ItemStack> machineUpgradeStacks,
                                @Nullable QuarryOwnerComponent owner, @Nullable QuarryBlockedDataComponent blocked) {
        MachineUpgradesUtil upgradesUtil = MachineUpgradesUtil.from(machineUpgradeStacks);
        long energyRequired = quarryBlock.getEnergyConsumption(upgradesUtil);
        int occupiedSlots = outputInventory.occupiedSlots();
        int slots = outputInventory.getContainerSize();
        int flag = 0;

        if(owner == null) flag += 1 << WARNING_OWNER_MISSING;
        if(blocked != null) flag += 1 << WARNING_MINING_DENIED;
        if(!(drillStack.getItem() instanceof DrillItem)) flag += 1 << WARNING_DRILL_EMPTY;

        if(energyStored < energyRequired) {
            flag += 1 << WARNING_ENERGY_DEPLETED;
        } else if(energyStored <= quarryBlock.getEnergyCapacity() * ENERGY_WARNING_PCT) {
            flag += 1 << WARNING_ENERGY_30;
        }

        if(occupiedSlots >= slots) {
            flag += 1 << WARNING_INV_FULL;
        } else if(occupiedSlots >= slots * INV_WARNING_PCT) {
            flag += 1 << WARNING_INV_70;
        }

        this.flag = flag;
        this.blocked = blocked;
        this.upgradesUtil = upgradesUtil;
    }

    public static MachineWarningsUtil from(QuarryBlockEntity blockEntity) {
        QuarryBlock quarryBlock = blockEntity.getQuarryBlock();
        if(quarryBlock == null) return UNKNOWN;

        return new MachineWarningsUtil(quarryBlock, blockEntity.EnergyContainer.getStored(), blockEntity.OutputInventory, blockEntity.DrillInventory.getItem(0),
                blockEntity.getMachineUpgradeStacks(), blockEntity.getOwner(), blockEntity.getBlocked());
    }
    public static MachineWarningsUtil from(ItemStack quarryMonitorStack) {
        if(!(quarryMonitorStack.getItem() instanceof QuarryMonitorItem quarryMonitorItem)) return UNKNOWN;

        ItemStack quarryStack = quarryMonitorItem.getCachedQuarryStack(quarryMonitorStack);
        if(!(ZUtil.getBlockOrItem(quarryStack) instanceof QuarryBlock quarryBlock)) return UNKNOWN;

        return new MachineWarningsUtil(quarryBlock, quarryBlock.getEnergyStored(quarryStack), quarryBlock.getOutputInventory(quarryStack), quarryBlock.getDrillStack(quarryStack),
                quarryBlock.getMachineUpgradeStacks(quarryStack), quarryMonitorItem.getCachedOwner(quarryMonitorStack), quarryMonitorItem.getCachedBlockedData(quarryMonitorStack));
    }

    private static int toFlag(List<Integer> warningList) {
        int flag = 0;

        for (int warning : warningList) {
            flag += 1 << warning;
        }

        return flag;
    }

    public static boolean hasWarnings(int flag) {
        return flag != 0;
    }
    public static boolean hasWarning(int flag, int warning) {
        warning = 1 << warning;

        return (flag & warning) == warning;
    }
    public static boolean hasCriticalWarnings(int flag) {
        return (flag & CRITICAL_FLAG) != 0;
    }

    public int getWarningFlag() {
        return this.flag;
    }
    public boolean hasWarnings() {
        return hasWarnings(this.flag) || this.upgradesUtil.hasConflicts();
    }
    public boolean hasCriticalWarnings() {
        return hasCriticalWarnings(this.flag);
    }

    public void appendTooltip(Consumer<Component> textConsumer) {
        if(hasWarning(this.flag, WARNING_QUARRY_UNKNOWN)) {
            textConsumer.accept(ReadableString.translatable("item.iron_quarry.warning.quarry_status_unknown"));
        }
        if(hasWarning(this.flag, WARNING_OWNER_MISSING)) {
            textConsumer.accept(ReadableString.translatable("item.iron_quarry.warning.quarry_owner_missing"));
        }
        if(hasWarning(this.flag, WARNING_MINING_DENIED) && this.blocked != null) {
            this.blocked.appendTooltip(textConsumer);
        }
        if(hasWarning(this.flag, WARNING_DRILL_EMPTY)) {
            textConsumer.accept(ReadableString.translatable("item.iron_quarry.warning.quarry_drill_empty"));
        }
        if(hasWarning(this.flag, WARNING_ENERGY_DEPLETED)) {
            textConsumer.accept(ReadableString.translatable("item.iron_quarry.warning.quarry_energy_depleted"));
        }
        if(hasWarning(this.flag, WARNING_INV_FULL)) {
            textConsumer.accept(ReadableString.translatable("item.iron_quarry.warning.quarry_inv_full"));
        }
        if(hasWarning(this.flag, WARNING_ENERGY_30)) {
            textConsumer.accept(ReadableString.translatable("item.iron_quarry.warning.quarry_energy_30"));
        }
        if(hasWarning(this.flag, WARNING_INV_70)) {
            textConsumer.accept(ReadableString.translatable("item.iron_quarry.warning.quarry_inv_70"));
        }

        this.upgradesUtil.appendConflictTooltip(textConsumer);
    }
}
