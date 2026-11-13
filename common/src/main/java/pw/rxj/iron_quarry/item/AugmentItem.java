package pw.rxj.iron_quarry.item;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Util;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;
import pw.rxj.iron_quarry.augment.ZAugments;
import pw.rxj.iron_quarry.data.AugmentCapacityUpgradesComponent;
import pw.rxj.iron_quarry.data.ZComponents;
import pw.rxj.iron_quarry.gui.CustomTooltipData;
import pw.rxj.iron_quarry.gui.TooltipInventoryData;
import pw.rxj.iron_quarry.interfaces.*;
import pw.rxj.iron_quarry.records.AugmentStack;
import pw.rxj.iron_quarry.types.AugmentType;
import pw.rxj.iron_quarry.types.DynamicText;
import pw.rxj.iron_quarry.util.ReadableString;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

public class AugmentItem extends Item implements IHandledSmithing, IHandledItemEntity, IModelPredicateProvider, IDynamicItemName, IModifyCreativeTabEntries {
    public static int CAPACITY_UPGRADE_SLOTS = ZItemTags.AUGMENT_CAPACITY_SLOT_ENHANCERS.size();
    public static int UNIQUE_SET_AMOUNT = 1;

    private @NotNull AugmentType uniqueType = AugmentType.EMPTY;
    private @NotNull DynamicText dynamicName = DynamicText.EMPTY;
    private final IntSupplier capacityUpgradeCost;

    public AugmentItem(Properties settings, IntSupplier capacityUpgradeCost) {
        super(settings);

        this.capacityUpgradeCost = capacityUpgradeCost;
    }

    public static boolean isOf(ItemStack stack){
        return ZUtil.getBlockOrItem(stack) instanceof AugmentItem;
    }
    public static boolean isNotOf(ItemStack stack){
        return !isOf(stack);
    }


    public AugmentItem dynamicName(DynamicText dynamicText) {
        this.dynamicName = dynamicText;

        return this;
    }
    public boolean hasDynamicName(){
        return this.dynamicName.isPresent();
    }
    public @NotNull DynamicText getDynamicName() {
        return this.dynamicName;
    }


    public AugmentItem unique(AugmentType augmentType) {
        this.uniqueType = augmentType;

        return this;
    }
    public boolean isUnique(){
        return !this.uniqueType.isEmpty();
    }
    public @NotNull AugmentType getUniqueType() {
        return this.uniqueType;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        if(!this.isTooltipExtended(stack)) return Optional.empty();

        List<Pair<ItemStack, Boolean>> inventory = new ArrayList<>();
        AugmentCapacityUpgradesComponent capacityUpgrades = this.getCapacityUpgrades(stack);

        for (int slot = 0; slot < CAPACITY_UPGRADE_SLOTS; slot++) {
            inventory.add(new Pair<>(this.getCapacitySlotStack(capacityUpgrades, slot), !capacityUpgrades.isFilled(slot)));
        }

        return Optional.of(TooltipInventoryData.from(inventory));
    }
    private ItemStack getCapacitySlotStack(AugmentCapacityUpgradesComponent capacityUpgrades, int slot) {
        Optional<Item> item = capacityUpgrades.getItem(slot);
        if(item.isPresent()) return item.get().getDefaultInstance();

        List<Item> slotItems = ZUtil.getItemsIn(ZItemTags.AUGMENT_CAPACITY_SLOT_ENHANCERS.get(slot));
        if(slotItems.isEmpty()) return ItemStack.EMPTY;

        int index = (int) (Util.getMillis() / 1000 % slotItems.size());

        return slotItems.get(index).getDefaultInstance();
    }

    @Override
    public Component getName(ItemStack stack) {
        String translationKey = "item.iron_quarry.augment";

        AugmentType augmentType = this.getType(stack);
        translationKey += "." + augmentType.getName();

        return Component.translatable(translationKey);
    }

    @Override
    public DynamicText getDynamicItemName(ItemStack stack) {
        if(this.hasDynamicName()) return this.getDynamicName();

        if(getAmount(stack) >= getType(stack).getAugment().getCapacity(CAPACITY_UPGRADE_SLOTS)) return DynamicText.RAINBOW;

        return DynamicText.EMPTY;
    }

    @Override
    public float getModelPredicate(ItemStack stack, ClientLevel world, LivingEntity entity, int seed) {
        AugmentType type = getType(stack);

        for (int i = 0; i < CAPACITY_UPGRADE_SLOTS; i++) {
            if(getAmount(stack) > type.getAugment().getCapacity(i)) continue;

            return (type.getId() / 10.0F) + (0.01F * i);
        }

        return (type.getId() / 10.0F) + (0.01F * CAPACITY_UPGRADE_SLOTS);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag options) {
        AugmentType augmentType = this.getType(stack);
        int capacity = this.getCapacity(stack);
        int stored = this.getAmount(stack);

        if(augmentType.isPresent()) {
            if(!this.isUnique()) {
                MutableComponent LORE_USED = ReadableString.translatable("item.iron_quarry.augment.lore.used", stored, capacity);
                textConsumer.accept(LORE_USED);
            }

            augmentType.getAugment().appendEffectTooltip(stored, textConsumer);
        } else if(!this.isUnique()) {
            MutableComponent LORE_EMPTY = ReadableString.translatable("item.iron_quarry.lore.use_rei", stored, capacity);
            textConsumer.accept(LORE_EMPTY);
        }

        if(!this.isUnique()) {
            if(this.isTooltipExtended(stack)) {
                MutableComponent LORE_INFO = ReadableString.translatable("item.iron_quarry.augment.lore.capacity_upgrades", this.getCapacityUpgradeCount(stack), CAPACITY_UPGRADE_SLOTS);

                textConsumer.accept(ReadableString.space());
                textConsumer.accept(LORE_INFO);
                textConsumer.accept(CustomTooltipData.MARKER);
            } else {
                MutableComponent LORE_DETAILS = ReadableString.translatable("item.iron_quarry.lore.details");

                textConsumer.accept(ReadableString.space());
                textConsumer.accept(LORE_DETAILS);
            }
        }

        if(augmentType.getAugment().isDisabled()) {
            MutableComponent LORE_DISABLED = ReadableString.translatable("item.iron_quarry.lore.disabled");

            textConsumer.accept(ReadableString.space());
            textConsumer.accept(LORE_DISABLED);
        }
    }
    public boolean isTooltipExtended(ItemStack stack) {
        return Minecraft.getInstance().hasShiftDown() || this.getType(stack).isEmpty();
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        if(this.isUnique() || getAmount(stack) >= getType(stack).getAugment().getCapacity(CAPACITY_UPGRADE_SLOTS)) return true;

        return super.isFoil(stack);
    }

    public Container getSmithingOutcome(ItemStack template, ItemStack input, ItemStack addition) {
        AugmentingTemplateItem templateItem = AugmentingTemplateItem.instanceFrom(template);
        if(templateItem == null) return new SimpleContainer(template, input, addition, ItemStack.EMPTY);

        ItemStack comparisonStack = input.copy();
        while(addition.getCount() > 0) {
            ItemStack stack = addition.copy();
            stack.setCount(1);

            AugmentStack augmentStack = AugmentStack.from(stack);
            if(augmentStack == null) return new SimpleContainer(template, input, addition, ItemStack.EMPTY);
            int augmentStackAmount = augmentStack.amount();

            if(!templateItem.canUse(template, augmentStackAmount)) break;

            if(!this.canInsert(input, augmentStack)) break;
            this.addAmount(input, augmentStackAmount);
            this.setType(input, augmentStack.type());

            templateItem.use(template, augmentStackAmount);
            addition.shrink(1);
        }

        if(this.getAmount(comparisonStack) == this.getAmount(input)) {
            return new SimpleContainer(template, input, addition, ItemStack.EMPTY);
        } else {
            return new SimpleContainer(template, ItemStack.EMPTY, addition, input);
        }
    }
    @Override
    public Boolean handleSmithingTakeOutput(Player player, Container inputInv, ResultContainer outputInv, ContainerLevelAccess context) {
        ItemStack template = inputInv.getItem(0).copy();
        AugmentingTemplateItem templateItem = AugmentingTemplateItem.instanceFrom(template);
        if(templateItem == null) return false;

        ItemStack base = inputInv.getItem(1).copy();
        if(AugmentItem.isNotOf(base)) return false;

        ItemStack addition = inputInv.getItem(2).copy();
        AugmentStack augmentStack = AugmentStack.from(addition);

        ItemStack outputStack = outputInv.getItem(0);
        outputStack.onCraftedBy(player, outputStack.getCount());
        outputInv.awardUsedRecipes(player, List.of(template, base, addition));

        if(augmentStack == null) {
            templateItem.use(template, this.getCapacityUpgradeCost());
            templateItem.breakIfUsedUp(template, player.position(), player.level());
            inputInv.setItem(0, template);

            base.shrink(1);
            inputInv.setItem(1, base);

            addition.shrink(1);
            inputInv.setItem(2, addition);
        } else {
            Container outcome = this.getSmithingOutcome(template, base, addition);

            ItemStack outcomeTemplate = outcome.getItem(0);
            templateItem.breakIfUsedUp(outcomeTemplate, player.position(), player.level());

            inputInv.setItem(0, outcomeTemplate);
            inputInv.setItem(1, outcome.getItem(1));
            inputInv.setItem(2, outcome.getItem(2));
        }

        context.execute((world, pos) -> world.levelEvent(1044, pos, 0));
        return true;
    }

    @Override
    public void handleItemEntity(ItemEntity itemEntity) {
        if(itemEntity == null) return;

        itemEntity.setInvulnerable(true);
    }

    public boolean canInsert(@NotNull ItemStack stack, AugmentStack augmentStack) {
        if(this.isUnique()) return false;

        AugmentType stackType = this.getType(stack);
        int stackFreeSpace = this.getFreeSpace(stack);

        if(stackType.isEmpty()) return true;
        if(!stackType.equals(augmentStack.type())) return false;

        return augmentStack.amount() <= stackFreeSpace;
    }

    public AugmentType getType(@NotNull ItemStack stack) {
        if(this.isUnique()) return this.getUniqueType();

        return stack.getOrDefault(ZComponents.AUGMENT_TYPE, AugmentType.EMPTY);
    }
    public void setType(@NotNull ItemStack stack, AugmentType type) {
        if (type == null) type = AugmentType.EMPTY;

        stack.set(ZComponents.AUGMENT_TYPE, type);
    }

    public int getAmount(@NotNull ItemStack stack) {
        if(this.isUnique()) return UNIQUE_SET_AMOUNT;

        return stack.getOrDefault(ZComponents.AUGMENT_AMOUNT, 0);
    }
    public void setAmount(@NotNull ItemStack stack, int amount) {
        if(amount < 0) return;

        stack.set(ZComponents.AUGMENT_AMOUNT, amount);
    }
    public void addAmount(@NotNull ItemStack stack, int amount) {
        this.setAmount(stack, this.getAmount(stack) + amount);
    }

    public int getCapacityUpgradeCost() {
        return this.capacityUpgradeCost.getAsInt();
    }
    public int getCapacity(@NotNull ItemStack stack) {
        if(this.isUnique()) return UNIQUE_SET_AMOUNT;

        AugmentType type = this.getType(stack);

        return type.getAugment().getCapacity(this.getCapacityUpgradeCount(stack));
    }
    public int getCapacityUpgradeCount(@NotNull ItemStack stack) {
        return this.getCapacityUpgrades(stack).getFilledCount(CAPACITY_UPGRADE_SLOTS);
    }
    public int getFreeSpace(@NotNull ItemStack stack){
        return this.getCapacity(stack) - this.getAmount(stack);
    }

    public boolean putCapacityUpgrade(@NotNull ItemStack stack, ItemStack upgrade) {
        AugmentCapacityUpgradesComponent capacityUpgrades = this.getCapacityUpgrades(stack);

        for (int slot = 0; slot < CAPACITY_UPGRADE_SLOTS; slot++) {
            if(capacityUpgrades.isFilled(slot)) continue;
            if(!upgrade.is(ZItemTags.AUGMENT_CAPACITY_SLOT_ENHANCERS.get(slot))) continue;

            stack.set(ZComponents.AUGMENT_CAPACITY_UPGRADES, capacityUpgrades.with(slot, upgrade.getItem()));

            return true;
        }

        return false;
    }
    public AugmentCapacityUpgradesComponent getCapacityUpgrades(@NotNull ItemStack stack) {
        return stack.getOrDefault(ZComponents.AUGMENT_CAPACITY_UPGRADES, AugmentCapacityUpgradesComponent.EMPTY);
    }

    public ItemStack withAllCapacityEnhancers() {
        ItemStack stack = new ItemStack(this);
        AugmentCapacityUpgradesComponent capacityUpgrades = AugmentCapacityUpgradesComponent.EMPTY;

        for (int slot = 0; slot < CAPACITY_UPGRADE_SLOTS; slot++) {
            List<Item> slotItems = ZUtil.getItemsIn(ZItemTags.AUGMENT_CAPACITY_SLOT_ENHANCERS.get(slot));
            if(slotItems.isEmpty()) continue;

            capacityUpgrades = capacityUpgrades.with(slot, slotItems.getFirst());
        }

        stack.set(ZComponents.AUGMENT_CAPACITY_UPGRADES, capacityUpgrades);
        return stack;
    }
    public ItemStack withCapacity(AugmentType augmentType, int capacityUpgrades) {
        ItemStack stack = this.withAllCapacityEnhancers();

        this.setType(stack, augmentType);
        this.setAmount(stack, augmentType.getAugment().getCapacity(capacityUpgrades));

        return stack;
    }

    @Override
    public void modifyCreativeTabEntries(CreativeModeTab.Output entries) {
        if (this.getUniqueType().getAugment().isDisabled()) return;

        entries.accept(this);

        if(!this.isUnique()) {
            ItemStack allEnhancers = this.withAllCapacityEnhancers();
            int capacityUpgradeCount = this.getCapacityUpgradeCount(allEnhancers);
            entries.accept(allEnhancers);

            for (int i = 0; i <= capacityUpgradeCount; i++) {
                if(!ZAugments.SPEED.isDisabled()) entries.accept(this.withCapacity(AugmentType.SPEED, i));
                if(!ZAugments.FORTUNE.isDisabled()) entries.accept(this.withCapacity(AugmentType.FORTUNE, i));
            }
        }
    }
}
