package pw.rxj.iron_quarry.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import pw.rxj.iron_quarry.augment.ZAugments;
import pw.rxj.iron_quarry.block.QuarryBlock;
import pw.rxj.iron_quarry.blockentity.QuarryBlockEntity;
import pw.rxj.iron_quarry.item.AugmentItem;
import pw.rxj.iron_quarry.item.DrillItem;
import pw.rxj.iron_quarry.screen.QuarryBlockScreenHandler;
import pw.rxj.iron_quarry.types.AugmentType;

import java.util.function.Consumer;

public class MachineUpgradesUtil {
    private boolean hasSilkTouch = false;
    private boolean hasChestLooting = false;
    private boolean hasLockedAugments = false;

    private float fortuneLevel = 0;
    private float speedMultiplier = 1;
    private float speedEnergyBonus = 0;
    private float slowness = 1;

    private MachineUpgradesUtil() { }
    private MachineUpgradesUtil(QuarryBlock quarryBlock, Container augmentInventory, ItemStack drillStack){
        for (int i = 0; i < augmentInventory.getContainerSize(); i++) {
            ItemStack stack = augmentInventory.getItem(i);

            if (stack.getItem() instanceof AugmentItem augmentItem) {
                if(i >= quarryBlock.getAugmentLimit()) {
                    this.hasLockedAugments = true; continue;
                }

                AugmentType augmentType = augmentItem.getType(stack);
                if(augmentType.getAugment().isDisabled()) continue;

                float progress = augmentType.getAugment().getProgress(augmentItem.getAmount(stack));

                switch (augmentType) {
                    case SPEED -> {
                        this.speedMultiplier += ZAugments.SPEED.getSpeedBonus(progress);
                        this.speedEnergyBonus += ZAugments.SPEED.getEnergyBonus(progress);
                    }
                    case FORTUNE -> {
                        this.fortuneLevel += ZAugments.FORTUNE.getLevel(progress);
                    }
                    case SILK_TOUCH -> {
                        this.hasSilkTouch = true;
                        this.slowness = ZAugments.SILK_TOUCH.getSlowness();
                    }
                    case CHEST_LOOTING -> this.hasChestLooting = true;
                }
            }
        }

        if(drillStack.getItem() instanceof DrillItem) {
            int efficiencyLevel = ZUtil.getCappedEnchantmentLevel(drillStack, Enchantments.EFFICIENCY);

            if(efficiencyLevel > 0) {
                float speedMultiplier = (float) Math.pow(1.3, efficiencyLevel) - 1.0F;

                this.speedMultiplier += speedMultiplier;
                this.speedEnergyBonus += speedMultiplier;
            }
        }
    }

    public static MachineUpgradesUtil empty() {
        return new MachineUpgradesUtil();
    }
    public static MachineUpgradesUtil from(QuarryBlockEntity blockEntity) {
        QuarryBlock quarryBlock = blockEntity.getQuarryBlock();
        if(quarryBlock == null) return empty();

        return new MachineUpgradesUtil(quarryBlock, blockEntity.MachineUpgradesInventory, blockEntity.DrillInventory.getItem(0));
    }
    public static MachineUpgradesUtil from(QuarryBlockScreenHandler screenHandler) {
        return new MachineUpgradesUtil(screenHandler.getQuarryBlock(), screenHandler.getMachineUpgradesInventory(), screenHandler.getDrillInventory().getItem(0));
    }
    public static MachineUpgradesUtil from(ItemStack quarryStack) {
        if(!(ZUtil.getBlockOrItem(quarryStack) instanceof QuarryBlock quarryBlock)) return empty();

        return new MachineUpgradesUtil(quarryBlock, quarryBlock.getAugmentInventory(quarryStack), quarryBlock.getDrillStack(quarryStack));
    }

    public boolean hasSilkTouch() { return this.hasSilkTouch; }
    public boolean hasChestLooting() { return this.hasChestLooting; }

    public float getFortuneLevel() {
        if(this.hasSilkTouch) return 0.0F;

        return Math.min(this.fortuneLevel, ZAugments.FORTUNE.getLevelLimit());
    }
    public float getFortuneEnergyMultiplier() {
        return 1 + this.getFortuneLevel() * ZAugments.FORTUNE.getEnergyPerLevel();
    }
    public float getSpeedMultiplier() {
        return Math.min(this.speedMultiplier / this.slowness, ZAugments.SPEED.getSpeedLimit());
    }
    public float getSpeedEnergyMultiplier() {
        float speedBonus = this.speedMultiplier - 1;
        if(speedBonus <= 0) return 1.0F;

        float cappedSpeedBonus = Math.min(this.speedMultiplier, ZAugments.SPEED.getSpeedLimit()) - 1;

        return 1 + this.speedEnergyBonus * cappedSpeedBonus / speedBonus;
    }

    //conflicts
    public static final int CONFLICT_SPEED = 0;
    public static final int CONFLICT_FORTUNE = 1;
    public static final int CONFLICT_SILK_TOUCH = 2;
    public static final int CONFLICT_LOCKED_AUGMENT = 3;
    public static boolean hasConflicts(int flag) {
        return flag != 0;
    }
    public static boolean hasConflict(int flag, int conflict) {
        conflict = 1 << conflict;

        return (flag & conflict) == conflict;
    }

    public boolean[] getConflictList() {
        return new boolean[] { this.exceedsSpeedLimit(), this.exceedsFortuneLimit(), this.conflictsWithSilkTouch(), this.hasLockedAugments() };
    }
    public int getConflictAmount() {
        int amount = 0;

        for (boolean hasConflict : this.getConflictList()) {
            if(hasConflict) amount++;
        }

        return amount;
    }
    public int getConflictFlag() {
        boolean[] list = this.getConflictList();
        int flag = 0;

        if(list[CONFLICT_SPEED]) flag += 1 << CONFLICT_SPEED;
        if(list[CONFLICT_FORTUNE]) flag += 1 << CONFLICT_FORTUNE;
        if(list[CONFLICT_SILK_TOUCH]) flag += 1 << CONFLICT_SILK_TOUCH;
        if(list[CONFLICT_LOCKED_AUGMENT]) flag += 1 << CONFLICT_LOCKED_AUGMENT;

        return flag;
    }
    public boolean hasConflicts() {
        return hasConflicts(this.getConflictFlag());
    }

    public boolean exceedsSpeedLimit() {
        return this.speedMultiplier > ZAugments.SPEED.getSpeedLimit();
    }
    public boolean exceedsFortuneLimit() {
        return this.fortuneLevel > ZAugments.FORTUNE.getLevelLimit();
    }
    public boolean conflictsWithSilkTouch() {
        return this.hasSilkTouch && this.fortuneLevel > 0;
    }
    public boolean hasLockedAugments() {
        return this.hasLockedAugments;
    }

    public void appendStatsTooltip(QuarryBlock quarryBlock, Consumer<Component> textConsumer) {
        float energy_usage = quarryBlock.getBaseConsumption() * this.getSpeedEnergyMultiplier() * this.getFortuneEnergyMultiplier();
        float operations = (20.0F / quarryBlock.getTicksPerOperation()) * this.getSpeedMultiplier();

        SupplicableAlt<?> HasShiftDown = SupplicableAlt.when(Minecraft.getInstance().hasShiftDown());

        SupplicableAlt<String> PER_TYPE = HasShiftDown.<String>copy()
                .then(() -> "per_operation")
                .or(() -> "per_tick");
        SupplicableAlt<String> PER_UNIT = HasShiftDown.<String>copy()
                .then(() -> ZUtil.expandableFixedInt((int) (energy_usage * 20 / operations)))
                .or(() -> ReadableString.cIntFrom(energy_usage));

        MutableComponent LORE_USAGE_DETAIL = ReadableString.translatable("item.iron_quarry.lore.energy." + PER_TYPE.get(), PER_UNIT.get());
        MutableComponent LORE_USAGE = ReadableString.translatable("item.iron_quarry.quarry_block.lore.usage", LORE_USAGE_DETAIL);

        MutableComponent LORE_SPEED_DETAIL = ReadableString.translatable("item.iron_quarry.lore.operation.per_second", ZUtil.expandableFixedFloat(operations));
        MutableComponent LORE_SPEED = ReadableString.translatable("item.iron_quarry.quarry_block.lore.speed", LORE_SPEED_DETAIL);

        MutableComponent LORE_FORTUNE = ReadableString.translatable("item.iron_quarry.quarry_block.lore.fortune", ZUtil.expandableFixedFloat(this.getFortuneLevel()));

        textConsumer.accept(LORE_USAGE);
        textConsumer.accept(LORE_SPEED);
        textConsumer.accept(LORE_FORTUNE);
    }
    public void appendConflictTooltip(Consumer<Component> textConsumer) {
        int conflictFlag = this.getConflictFlag();

        if(hasConflict(conflictFlag, CONFLICT_SPEED)) {
            textConsumer.accept(ReadableString.translatable("item.iron_quarry.conflict.speed_limit", ZUtil.expandableFixedFloat(ZAugments.SPEED.getSpeedLimit())));
        }
        if(hasConflict(conflictFlag, CONFLICT_FORTUNE)) {
            textConsumer.accept(ReadableString.translatable("item.iron_quarry.conflict.fortune_limit", ZUtil.expandableFixedFloat(ZAugments.FORTUNE.getLevelLimit())));
        }
        if(hasConflict(conflictFlag, CONFLICT_SILK_TOUCH)) {
            textConsumer.accept(ReadableString.translatable("item.iron_quarry.conflict.silk_touch_fortune"));
        }
        if(hasConflict(conflictFlag, CONFLICT_LOCKED_AUGMENT)) {
            textConsumer.accept(ReadableString.translatable("item.iron_quarry.conflict.locked_augment"));
        }
    }
}
