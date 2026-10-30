package pw.rxj.iron_quarry.item;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;
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

public class AugmentItem extends Item implements IHandledSmithing, IHandledItemEntity, IModelPredicateProvider, IDynamicItemName, IModifyCreativeTabEntries {
    public static int CAPACITY_UPGRADE_SLOTS = 7;
    public static int UNIQUE_SET_AMOUNT = 1;

    private @NotNull AugmentType uniqueType = AugmentType.EMPTY;
    private @NotNull DynamicText dynamicName = DynamicText.EMPTY;

    public AugmentItem(Properties settings) {
        super(settings);
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
        List<Item> capacityUpgrades = this.getUpgrades(stack, ZItemTags.AUGMENT_CAPACITY_ENHANCERS);

        BuiltInRegistries.ITEM.getTagOrEmpty(ZItemTags.AUGMENT_CAPACITY_ENHANCERS).forEach(itemRegistryEntry -> {
            Item upgradeItem = itemRegistryEntry.value();

            inventory.add(new Pair<>(upgradeItem.getDefaultInstance(), !capacityUpgrades.contains(upgradeItem)));
        });

        return Optional.of(TooltipInventoryData.from(inventory));
    }

    @Override
    public Component getName(ItemStack stack) {
        Minecraft minecraftClient = Minecraft.getInstance();
        if(minecraftClient.player == null) return super.getName(stack);

        String translationKey = "item.iron_quarry.augment";

        AugmentType augmentType = this.getType(stack);
        translationKey += "." + augmentType.getName();

        return Component.translatable(translationKey);
    }

    @Override
    public DynamicText getDynamicItemName(ItemStack stack) {
        if(this.hasDynamicName()) return this.getDynamicName();

        if(getAmount(stack) >= getType(stack).getCapacity(CAPACITY_UPGRADE_SLOTS)) return DynamicText.RAINBOW;

        return DynamicText.EMPTY;
    }

    @Override
    public float getModelPredicate(ItemStack stack, ClientLevel world, LivingEntity entity, int seed) {
        AugmentType type = getType(stack);

        for (int i = 0; i < CAPACITY_UPGRADE_SLOTS; i++) {
            if(getAmount(stack) > type.getCapacity(i)) continue;

            return (type.getId() / 10.0F) + (0.01F * i);
        }

        return (type.getId() / 10.0F) + (0.01F * CAPACITY_UPGRADE_SLOTS);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag options) {
        AugmentType augmentType = this.getType(stack);
        int capacity = this.getCapacity(stack);
        int stored = this.getAmount(stack);
        float multiplied = augmentType.multiply(stored);

        if(augmentType.isPresent()) {
            if(!this.isUnique()) {
                MutableComponent LORE_UNUSED = ReadableString.translatable("item.iron_quarry.augment.lore.used", stored, capacity);
                textConsumer.accept(LORE_UNUSED);
            }

            MutableComponent LORE_BENEFITS = ReadableString.translatable("item.iron_quarry.augment.lore.benefit." + augmentType.getName(), ZUtil.expandableFixedFloat(multiplied));
            textConsumer.accept(LORE_BENEFITS);

            float anyIneff = augmentType.anyIneff(multiplied);
            String drawbackKey = augmentType.getDrawbackKey();
            String drawbackFillIn = drawbackKey.equals("energy") ? ZUtil.expandableFixedFloat(anyIneff) : ReadableString.minPrecisionFloat(anyIneff);

            MutableComponent LORE_DRAWBACK = ReadableString.translatable("item.iron_quarry.augment.lore.drawback." + augmentType.getDrawbackKey(), drawbackFillIn);
            textConsumer.accept(LORE_DRAWBACK);
        } else if(!this.isUnique()) {
            MutableComponent LORE_EMPTY = ReadableString.translatable("item.iron_quarry.lore.use_rei", stored, capacity);
            textConsumer.accept(LORE_EMPTY);
        }

        if(!this.isUnique()) {
            if(this.isTooltipExtended(stack)) {
                List<Item> used_upgrades = this.getUpgrades(stack, ZItemTags.AUGMENT_CAPACITY_ENHANCERS);

                MutableComponent LORE_INFO = ReadableString.translatable("item.iron_quarry.augment.lore.capacity_upgrades", used_upgrades.size(), CAPACITY_UPGRADE_SLOTS);

                textConsumer.accept(ReadableString.space());
                textConsumer.accept(LORE_INFO);
                textConsumer.accept(CustomTooltipData.MARKER);
            } else {
                MutableComponent LORE_DETAILS = ReadableString.translatable("item.iron_quarry.lore.details");

                textConsumer.accept(ReadableString.space());
                textConsumer.accept(LORE_DETAILS);
            }
        }

        if(augmentType.isDisabled()) {
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
        if(this.isUnique() || getAmount(stack) >= getType(stack).getCapacity(CAPACITY_UPGRADE_SLOTS)) return true;

        return super.isFoil(stack);
    }

    public Container getSmithingOutcome(ItemStack template, ItemStack input, ItemStack addition) {
        DamageableSmithingTemplateItem templateItem = DamageableSmithingTemplateItem.instanceFrom(template);
        if(templateItem == null) return new SimpleContainer(template, input, addition, ItemStack.EMPTY);

        ItemStack comparisonStack = input.copy();
        while(addition.getCount() > 0) {
            ItemStack stack = addition.copy();
            stack.setCount(1);

            AugmentStack augmentStack = AugmentStack.from(stack);
            if(augmentStack == null) return new SimpleContainer(template, input, addition, ItemStack.EMPTY);
            int augmentStackAmount = augmentStack.amount();

            if(templateItem.getCDurability(template) < augmentStackAmount) break;

            if(!this.canInsert(input, augmentStack)) break;
            this.addAmount(input, augmentStackAmount);
            this.setType(input, augmentStack.type());

            templateItem.cdamage(template, augmentStackAmount, null, null);
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
        DamageableSmithingTemplateItem templateItem = DamageableSmithingTemplateItem.instanceFrom(template);
        if(templateItem == null) return false;

        ItemStack base = inputInv.getItem(1).copy();
        if(AugmentItem.isNotOf(base)) return false;

        ItemStack addition = inputInv.getItem(2).copy();
        AugmentStack augmentStack = AugmentStack.from(addition);

        ItemStack outputStack = outputInv.getItem(0);
        outputStack.onCraftedBy(player, outputStack.getCount());
        outputInv.awardUsedRecipes(player, List.of(template, base, addition));

        if(augmentStack == null) {
            templateItem.cdamage(template, 1, null, null);
            templateItem.triggerBreakEventIfNeeded(template, player.position(), player.level());
            inputInv.setItem(0, template);

            base.shrink(1);
            inputInv.setItem(1, base);

            addition.shrink(1);
            inputInv.setItem(2, addition);
        } else {
            Container outcome = this.getSmithingOutcome(template, base, addition);

            ItemStack outcomeTemplate = outcome.getItem(0);
            templateItem.triggerBreakEventIfNeeded(outcomeTemplate, player.position(), player.level());

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

    public int getCapacity(@NotNull ItemStack stack) {
        if(this.isUnique()) return UNIQUE_SET_AMOUNT;

        List<Item> upgrades = this.getUpgrades(stack, ZItemTags.AUGMENT_CAPACITY_ENHANCERS);
        AugmentType type = this.getType(stack);

        return type.getCapacity(upgrades.size());
    }
    public int getFreeSpace(@NotNull ItemStack stack){
        return this.getCapacity(stack) - this.getAmount(stack);
    }

    public boolean putUpgrade(@NotNull ItemStack stack, ItemStack upgrade) {
        List<Identifier> itemIdList = new ArrayList<>(stack.getOrDefault(ZComponents.AUGMENT_UPGRADES, List.of()));

        if(upgrade.is(ZItemTags.AUGMENT_CAPACITY_ENHANCERS)) {
            List<Item> capacityUpgrades = this.getUpgrades(stack, ZItemTags.AUGMENT_CAPACITY_ENHANCERS);
            if(capacityUpgrades.size() >= CAPACITY_UPGRADE_SLOTS) return false;
        }

        Identifier upgradeId = BuiltInRegistries.ITEM.getKey(upgrade.getItem());

        for (Identifier itemId : itemIdList) {
            if(itemId.equals(upgradeId)) return false;
        }

        itemIdList.add(upgradeId);
        stack.set(ZComponents.AUGMENT_UPGRADES, List.copyOf(itemIdList));

        return true;
    }

    public List<Item> getUpgrades(@NotNull ItemStack stack) {
        List<Identifier> itemIdList = stack.getOrDefault(ZComponents.AUGMENT_UPGRADES, new ArrayList<>());
        List<Item> upgradeList = new ArrayList<>();

        for (Identifier id : itemIdList) {
            Optional<Item> item = BuiltInRegistries.ITEM.getOptional(id);
            item.ifPresent(upgradeList::add);
        }

        return upgradeList;
    }
    public List<Item> getUpgrades(@NotNull ItemStack stack, TagKey<Item> itemTag) {
        List<Item> upgradeList = this.getUpgrades(stack);

        return upgradeList.stream().filter(item -> item.getDefaultInstance().is(itemTag)).toList();
    }

    public ItemStack withAllCapacityEnhancers() {
        ItemStack stack = new ItemStack(this);

        for (Item enhancer : ZUtil.getItemsIn(ZItemTags.AUGMENT_CAPACITY_ENHANCERS)) {
            this.putUpgrade(stack, enhancer.getDefaultInstance());
        }

        return stack;
    }
    public ItemStack withCapacity(AugmentType augmentType, int capacityUpgrades) {
        ItemStack stack = this.withAllCapacityEnhancers();

        this.setType(stack, augmentType);
        this.setAmount(stack, augmentType.getCapacity(capacityUpgrades));

        return stack;
    }

    @Override
    public void modifyCreativeTabEntries(CreativeModeTab.Output entries) {
        if (this.getUniqueType().isDisabled()) return;

        entries.accept(this);

        if(!this.isUnique()) {
            entries.accept(this.withAllCapacityEnhancers());

            for (int i = 0; i < CAPACITY_UPGRADE_SLOTS + 1; i++) {
                if(!AugmentType.SPEED.isDisabled()) entries.accept(this.withCapacity(AugmentType.SPEED, i));
                if(!AugmentType.FORTUNE.isDisabled()) entries.accept(this.withCapacity(AugmentType.FORTUNE, i));
            }
        }
    }
}
