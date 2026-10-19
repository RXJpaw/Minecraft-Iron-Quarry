package pw.rxj.iron_quarry.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.recipe.input.SmithingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.data.ZComponents;
import pw.rxj.iron_quarry.gui.CustomTooltipData;
import pw.rxj.iron_quarry.gui.TooltipInventoryData;
import pw.rxj.iron_quarry.interfaces.*;
import pw.rxj.iron_quarry.recipe.HandledSmithingRecipe;
import pw.rxj.iron_quarry.records.AugmentStack;
import pw.rxj.iron_quarry.types.AugmentType;
import pw.rxj.iron_quarry.types.DynamicText;
import pw.rxj.iron_quarry.util.ReadableString;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class AugmentItem extends Item implements IHandledSmithing, IHandledItemEntity, IModelPredicateProvider, IDynamicItemName, IModifyItemGroupEntries {
    public static int CAPACITY_UPGRADE_SLOTS = 7;
    public static int UNIQUE_SET_AMOUNT = 1;

    private @NotNull AugmentType uniqueType = AugmentType.EMPTY;
    private @NotNull DynamicText dynamicName = DynamicText.EMPTY;

    public AugmentItem(Settings settings) {
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
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        if(!this.isTooltipExtended(stack)) return Optional.empty();

        List<Pair<ItemStack, Boolean>> inventory = new ArrayList<>();
        List<Item> capacityUpgrades = this.getUpgrades(stack, ZItemTags.AUGMENT_CAPACITY_ENHANCERS);

        Registries.ITEM.iterateEntries(ZItemTags.AUGMENT_CAPACITY_ENHANCERS).forEach(itemRegistryEntry -> {
            Item upgradeItem = itemRegistryEntry.value();

            inventory.add(new Pair<>(upgradeItem.getDefaultStack(), !capacityUpgrades.contains(upgradeItem)));
        });

        return Optional.of(TooltipInventoryData.from(inventory));
    }

    @Override
    public Text getName(ItemStack stack) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        if(minecraftClient.player == null) return getName();

        String translationKey = "item.iron_quarry.augment";

        AugmentType augmentType = this.getType(stack);
        translationKey += "." + augmentType.getName();

        return Text.translatable(translationKey);
    }

    @Override
    public DynamicText getDynamicItemName(ItemStack stack) {
        if(this.hasDynamicName()) return this.getDynamicName();

        if(getAmount(stack) >= getType(stack).getCapacity(CAPACITY_UPGRADE_SLOTS)) return DynamicText.RAINBOW;

        return DynamicText.EMPTY;
    }

    @Override
    public float getModelPredicate(ItemStack stack, ClientWorld world, LivingEntity entity, int seed) {
        AugmentType type = getType(stack);

        for (int i = 0; i < CAPACITY_UPGRADE_SLOTS; i++) {
            if(getAmount(stack) > type.getCapacity(i)) continue;

            return (type.getId() / 10.0F) + (0.01F * i);
        }

        return (type.getId() / 10.0F) + (0.01F * CAPACITY_UPGRADE_SLOTS);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType options) {
        AugmentType augmentType = this.getType(stack);
        int capacity = this.getCapacity(stack);
        int stored = this.getAmount(stack);
        float multiplied = augmentType.multiply(stored);

        if(augmentType.isPresent()) {
            if(!this.isUnique()) {
                MutableText LORE_UNUSED = ReadableString.translatable("item.iron_quarry.augment.lore.used", stored, capacity);
                textConsumer.accept(LORE_UNUSED);
            }

            MutableText LORE_BENEFITS = ReadableString.translatable("item.iron_quarry.augment.lore.benefit." + augmentType.getName(), ZUtil.expandableFixedFloat(multiplied));
            textConsumer.accept(LORE_BENEFITS);

            float anyIneff = augmentType.anyIneff(multiplied);
            String drawbackKey = augmentType.getDrawbackKey();
            String drawbackFillIn = drawbackKey.equals("energy") ? ZUtil.expandableFixedFloat(anyIneff) : ReadableString.minPrecisionFloat(anyIneff);

            MutableText LORE_DRAWBACK = ReadableString.translatable("item.iron_quarry.augment.lore.drawback." + augmentType.getDrawbackKey(), drawbackFillIn);
            textConsumer.accept(LORE_DRAWBACK);
        } else if(!this.isUnique()) {
            MutableText LORE_EMPTY = ReadableString.translatable("item.iron_quarry.lore.use_rei", stored, capacity);
            textConsumer.accept(LORE_EMPTY);
        }

        if(!this.isUnique()) {
            if(this.isTooltipExtended(stack)) {
                List<Item> used_upgrades = this.getUpgrades(stack, ZItemTags.AUGMENT_CAPACITY_ENHANCERS);

                MutableText LORE_INFO = ReadableString.translatable("item.iron_quarry.augment.lore.capacity_upgrades", used_upgrades.size(), CAPACITY_UPGRADE_SLOTS);

                textConsumer.accept(ReadableString.space());
                textConsumer.accept(LORE_INFO);
                textConsumer.accept(CustomTooltipData.MARKER);
            } else {
                MutableText LORE_DETAILS = ReadableString.translatable("item.iron_quarry.lore.details");

                textConsumer.accept(ReadableString.space());
                textConsumer.accept(LORE_DETAILS);
            }
        }

        if(augmentType.isDisabled()) {
            MutableText LORE_DISABLED = ReadableString.translatable("item.iron_quarry.lore.disabled");

            textConsumer.accept(ReadableString.space());
            textConsumer.accept(LORE_DISABLED);
        }
    }
    public boolean isTooltipExtended(ItemStack stack) {
        return Screen.hasShiftDown() || this.getType(stack).isEmpty() || ZItemTags.AUGMENT_CAPACITY_ENHANCERS.equals(this.getSmithingPreviewAdditionTag(stack));
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        if(this.isUnique() || getAmount(stack) >= getType(stack).getCapacity(CAPACITY_UPGRADE_SLOTS)) return true;

        return super.hasGlint(stack);
    }

    @Override
    public ItemStack getSmithingOutput(HandledSmithingRecipe handler, SmithingRecipeInput input, RegistryWrapper.WrapperLookup registryLookup) {
        ItemStack template = input.getStackInSlot(0).copy();
        if(DamageableSmithingTemplateItem.isNotOf(template)) return ItemStack.EMPTY;

        ItemStack base = input.getStackInSlot(1).copy();
        if(AugmentItem.isNotOf(base)) return ItemStack.EMPTY;

        ItemStack addition = input.getStackInSlot(2).copy();
        AugmentStack augmentStack = AugmentStack.from(addition.copy());

        if(augmentStack == null) {
            boolean result = this.putUpgrade(base, addition);
            return result ? base : ItemStack.EMPTY;
        } else {
            Inventory outcome = this.getSmithingOutcome(template, base, addition);
            return outcome.getStack(3);
        }
    }
    public Inventory getSmithingOutcome(ItemStack template, ItemStack input, ItemStack addition) {
        DamageableSmithingTemplateItem templateItem = DamageableSmithingTemplateItem.instanceFrom(template);
        if(templateItem == null) return new SimpleInventory(template, input, addition, ItemStack.EMPTY);

        ItemStack comparisonStack = input.copy();
        while(addition.getCount() > 0) {
            ItemStack stack = addition.copy();
            stack.setCount(1);

            AugmentStack augmentStack = AugmentStack.from(stack);
            if(augmentStack == null) return new SimpleInventory(template, input, addition, ItemStack.EMPTY);
            int augmentStackAmount = augmentStack.amount();

            if(templateItem.getCDurability(template) < augmentStackAmount) break;

            if(!this.canInsert(input, augmentStack)) break;
            this.addAmount(input, augmentStackAmount);
            this.setType(input, augmentStack.type());

            templateItem.cdamage(template, augmentStackAmount, null, null);
            addition.decrement(1);
        }

        if(this.getAmount(comparisonStack) == this.getAmount(input)) {
            return new SimpleInventory(template, input, addition, ItemStack.EMPTY);
        } else {
            return new SimpleInventory(template, ItemStack.EMPTY, addition, input);
        }
    }
    @Override
    public ItemStack getSmithingOutputPreview(Optional<Ingredient> base, Optional<Ingredient> addition, @Nullable TagKey<Item> additionTag, ItemStack output) {
        if(output.getItem() instanceof AugmentItem augmentItem && additionTag != null) {
            ItemStack stack = output.copy();

            if(additionTag.equals(ZItemTags.AUGMENT_CAPACITY_ENHANCERS)) {
                augmentItem.makeSmithingPreview(stack, ZItemTags.AUGMENT_CAPACITY_ENHANCERS);

                return stack;
            } else {
                for (TagKey<Item> itemTagKey : ZItemTags.AUGMENT_ENHANCERS) {
                    if(!additionTag.equals(itemTagKey)) continue;

                    AugmentStack augmentStack = AugmentStack.from(itemTagKey);
                    if(augmentStack == null) continue;

                    augmentItem.setType(stack, augmentStack.type());
                    augmentItem.setAmount(stack, augmentStack.amount());

                    return stack;
                }
            }
        }

        return IHandledSmithing.super.getSmithingOutputPreview(base, addition, additionTag, output);
    }
    @Override
    public Boolean handleSmithingTakeOutput(PlayerEntity player, Inventory inputInv, CraftingResultInventory outputInv, ScreenHandlerContext context) {
        ItemStack template = inputInv.getStack(0).copy();
        DamageableSmithingTemplateItem templateItem = DamageableSmithingTemplateItem.instanceFrom(template);
        if(templateItem == null) return false;

        ItemStack base = inputInv.getStack(1).copy();
        if(AugmentItem.isNotOf(base)) return false;

        ItemStack addition = inputInv.getStack(2).copy();
        AugmentStack augmentStack = AugmentStack.from(addition);

        ItemStack outputStack = outputInv.getStack(0);
        outputStack.onCraftByPlayer(player, outputStack.getCount());
        outputInv.unlockLastRecipe(player, List.of(template, base, addition));

        if(augmentStack == null) {
            templateItem.cdamage(template, 1, null, null);
            templateItem.triggerBreakEventIfNeeded(template, player.getPos(), player.getWorld());
            inputInv.setStack(0, template);

            base.decrement(1);
            inputInv.setStack(1, base);

            addition.decrement(1);
            inputInv.setStack(2, addition);
        } else {
            Inventory outcome = this.getSmithingOutcome(template, base, addition);

            ItemStack outcomeTemplate = outcome.getStack(0);
            templateItem.triggerBreakEventIfNeeded(outcomeTemplate, player.getPos(), player.getWorld());

            inputInv.setStack(0, outcomeTemplate);
            inputInv.setStack(1, outcome.getStack(1));
            inputInv.setStack(2, outcome.getStack(2));
        }

        context.run((world, pos) -> world.syncWorldEvent(1044, pos, 0));
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

        if(upgrade.isIn(ZItemTags.AUGMENT_CAPACITY_ENHANCERS)) {
            List<Item> capacityUpgrades = this.getUpgrades(stack, ZItemTags.AUGMENT_CAPACITY_ENHANCERS);
            if(capacityUpgrades.size() >= CAPACITY_UPGRADE_SLOTS) return false;
        }

        Identifier upgradeId = Registries.ITEM.getId(upgrade.getItem());

        for (Identifier itemId : itemIdList) {
            if(itemId.equals(upgradeId)) return false;
        }

        itemIdList.add(upgradeId);
        stack.set(ZComponents.AUGMENT_UPGRADES, List.copyOf(itemIdList));

        return true;
    }

    public List<Item> getUpgrades(@NotNull ItemStack stack) {
        if(this.isSmithingPreview(stack)) {
            TagKey<Item> addition = this.getSmithingPreviewAdditionTag(stack);
            if(addition == null) return List.of();

            List<Item> matchingItems = ZUtil.getItemsIn(addition);
            if(matchingItems.isEmpty()) return List.of();

            long time = System.currentTimeMillis() / 1000;
            int stackIndex = (int) (time % matchingItems.size());

            return List.of(matchingItems.get(stackIndex));
        }

        List<Identifier> itemIdList = stack.getOrDefault(ZComponents.AUGMENT_UPGRADES, new ArrayList<>());
        List<Item> upgradeList = new ArrayList<>();

        for (Identifier id : itemIdList) {
            Optional<Item> item = Registries.ITEM.getOptionalValue(id);
            item.ifPresent(upgradeList::add);
        }

        return upgradeList;
    }
    public List<Item> getUpgrades(@NotNull ItemStack stack, TagKey<Item> itemTag) {
        List<Item> upgradeList = this.getUpgrades(stack);

        return upgradeList.stream().filter(item -> item.getDefaultStack().isIn(itemTag)).toList();
    }

    public ItemStack withAllCapacityEnhancers() {
        ItemStack stack = new ItemStack(this);

        for (Item enhancer : ZUtil.getItemsIn(ZItemTags.AUGMENT_CAPACITY_ENHANCERS)) {
            this.putUpgrade(stack, enhancer.getDefaultStack());
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
    public void onModifyItemGroupEntry(FabricItemGroupEntries entries) {
        if (this.getUniqueType().isDisabled()) return;

        entries.add(this);

        if(!this.isUnique()) {
            entries.add(this.withAllCapacityEnhancers());

            for (int i = 0; i < CAPACITY_UPGRADE_SLOTS + 1; i++) {
                if(!AugmentType.SPEED.isDisabled()) entries.add(this.withCapacity(AugmentType.SPEED, i));
                if(!AugmentType.FORTUNE.isDisabled()) entries.add(this.withCapacity(AugmentType.FORTUNE, i));
            }
        }
    }
}
