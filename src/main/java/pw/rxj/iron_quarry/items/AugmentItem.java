package pw.rxj.iron_quarry.items;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.recipe.Ingredient;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.tag.TagKey;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.interfaces.IDynamicItemName;
import pw.rxj.iron_quarry.interfaces.IHandledItemEntity;
import pw.rxj.iron_quarry.interfaces.IHandledSmithing;
import pw.rxj.iron_quarry.interfaces.IModelPredicateProvider;
import pw.rxj.iron_quarry.recipes.HandledSmithingRecipe;
import pw.rxj.iron_quarry.records.AugmentStack;
import pw.rxj.iron_quarry.types.AugmentType;
import pw.rxj.iron_quarry.types.DynamicText;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AugmentItem extends Item implements IHandledSmithing, IHandledItemEntity, IModelPredicateProvider, IDynamicItemName {
    public static int CAPACITY_UPGRADE_SLOTS = 7;

    public AugmentItem(Settings settings) {
        super(settings);
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
        return DynamicText.RAINBOW;
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
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        AugmentType augmentType = this.getType(stack);
        List<Item> upgrades = this.getUpgrades(stack);
        int capacity = this.getCapacity(stack);
        int stored = this.getAmount(stack);

        MutableText LORE_UNUSED = Text.translatable("item.iron_quarry.augment.lore.used", stored, capacity)
                .setStyle(Style.EMPTY.withColor(0xA8A8A8));

        tooltip.add(LORE_UNUSED);

        if(augmentType.isPresent()) {
            MutableText LORE_BENEFITS = Text.translatable("item.iron_quarry.augment.lore.benefit." + augmentType.getName(), ZUtil.expandableFixedFloat(stored * augmentType.getMultiplier()))
                    .setStyle(Style.EMPTY.withColor(0x688C54));
            MutableText LORE_DRAWBACK = Text.translatable("item.iron_quarry.augment.lore.drawback.energy", ZUtil.expandableFixedFloat(stored * augmentType.getInefficiency()))
                    .setStyle(Style.EMPTY.withColor(0x8C5454));

            tooltip.add(LORE_BENEFITS);
            tooltip.add(LORE_DRAWBACK);
        }

        if(Screen.hasShiftDown()) {
            List<Item> used_upgrades = new ArrayList<>();
            List<Item> unused_upgrades = new ArrayList<>();

            Registry.ITEM.iterateEntries(ZItemTags.AUGMENT_CAPACITY_ENHANCERS).forEach(itemRegistryEntry -> {
                Item item = itemRegistryEntry.value();

                boolean isItemUsed = upgrades.contains(item);
                if(isItemUsed) used_upgrades.add(item);
                else unused_upgrades.add(item);
            });

            MutableText LORE_INFO = Text.translatable("item.iron_quarry.augment.lore.capacity_upgrades", used_upgrades.size(), CAPACITY_UPGRADE_SLOTS)
                    .setStyle(Style.EMPTY.withColor(0x00AAAA).withUnderline(true));

            tooltip.add(Text.empty());
            tooltip.add(LORE_INFO);

            used_upgrades.forEach(item -> {
                MutableText itemName = Text.literal("> ").append(item.getName().copy());
                tooltip.add(itemName.setStyle(Style.EMPTY.withColor(0x54FCFC)));
            });
            unused_upgrades.forEach(item -> {
                MutableText itemName = Text.literal("> ").append(item.getName().copy());
                tooltip.add(itemName.setStyle(Style.EMPTY.withColor(0x545454)));
            });

            if(context.isAdvanced()) tooltip.add(Text.empty());
        } else {
            MutableText LORE_DETAILS = Text.translatable("item.iron_quarry.lore.details");

            tooltip.add(Text.empty());
            tooltip.add(LORE_DETAILS);
        }
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return super.getRarity(stack);
    }
    @Override
    public boolean hasGlint(ItemStack stack) {
        if(getAmount(stack) >= getType(stack).getCapacity(CAPACITY_UPGRADE_SLOTS)) return true;

        return super.hasGlint(stack);
    }

    @Override
    public ItemStack getSmithingOutput(HandledSmithingRecipe handler, Inventory inventory) {
        ItemStack base = inventory.getStack(0).copy();
        ItemStack addition = inventory.getStack(1).copy();
        AugmentStack augmentStack = AugmentStack.from(addition);

        if(augmentStack == null) {
            boolean result = this.putUpgrade(base, addition);
            return result ? base : ItemStack.EMPTY;
        } else {
            Inventory outcome = this.getSmithingOutcome(base, addition);
            return outcome.getStack(2);
        }
    }
    public Inventory getSmithingOutcome(ItemStack input, ItemStack addition) {
        ItemStack comparisonStack = input.copy();

        while(addition.getCount() > 0) {
            ItemStack stack = addition.copy();
            stack.setCount(1);

            AugmentStack augmentStack = AugmentStack.from(stack);
            if(augmentStack == null) return new SimpleInventory(input, addition, ItemStack.EMPTY);

            if(!this.canInsert(input, augmentStack)) break;
            this.addAmount(input, augmentStack.amount());
            this.setType(input, augmentStack.type());
            addition.decrement(1);
        }

        if(this.getAmount(comparisonStack) == this.getAmount(input)) {
            return new SimpleInventory(input, addition, ItemStack.EMPTY);
        } else {
            return new SimpleInventory(ItemStack.EMPTY, addition, input);
        }
    }
    @Override
    public ItemStack getSmithingOutputPreview(Ingredient base, Ingredient addition, ItemStack output) {
        if(output.getItem() instanceof AugmentItem augmentItem) {
            ItemStack stack = output.copy();

            if(addition.toJson().equals(Ingredient.fromTag(ZItemTags.AUGMENT_CAPACITY_ENHANCERS).toJson())) {
                augmentItem.putUpgrade(stack, Items.GOLD_BLOCK.getDefaultStack());

                return stack;
            } else {
                for (TagKey<Item> itemTagKey : ZItemTags.AUGMENT_ENHANCERS) {
                    if(!addition.toJson().equals(Ingredient.fromTag(itemTagKey).toJson())) continue;

                    AugmentStack augmentStack = AugmentStack.from(itemTagKey);
                    if(augmentStack == null) continue;

                    augmentItem.setType(stack, augmentStack.type());
                    augmentItem.setAmount(stack, augmentStack.amount());

                    return stack;
                }
            }
        }

        return IHandledSmithing.super.getSmithingOutputPreview(base, addition, output);
    }
    @Override
    public Boolean handleSmithingTakeOutput(PlayerEntity player, Inventory inputInv, CraftingResultInventory outputInv, ItemStack output, ScreenHandlerContext context) {
        ItemStack base = inputInv.getStack(0).copy();
        ItemStack addition = inputInv.getStack(1).copy();
        AugmentStack augmentStack = AugmentStack.from(addition);

        if(augmentStack == null) {
            return false;
        } else {
            output.onCraft(player.world, player, output.getCount());
            outputInv.unlockLastRecipe(player);

            Inventory outcome = this.getSmithingOutcome(base, addition);
            inputInv.setStack(0, outcome.getStack(0));
            inputInv.setStack(1, outcome.getStack(1));
            output.setNbt(outcome.getStack(2).getNbt());

            context.run((world, pos) -> world.syncWorldEvent(1044, pos, 0));
            return true;
        }
    }

    @Override
    public void handleItemEntity(ItemEntity itemEntity) {
        if(itemEntity == null) return;

        itemEntity.setInvulnerable(true);
    }

    public boolean canInsert(@NotNull ItemStack stack, AugmentStack augmentStack) {
        AugmentType stackType = this.getType(stack);
        int stackFreeSpace = this.getFreeSpace(stack);

        if(stackType.isEmpty()) return true;
        if(!stackType.equals(augmentStack.type())) return false;

        return augmentStack.amount() <= stackFreeSpace;
    }

    public AugmentType getType(@NotNull ItemStack stack) {
        NbtCompound nbtCompound = stack.getNbt();
        if(nbtCompound == null) return AugmentType.EMPTY;

        AugmentType type = AugmentType.from(nbtCompound.getString("Type"));
        if(type != null) return type;
        return AugmentType.EMPTY;
    }
    public void setType(@NotNull ItemStack stack, AugmentType type) {
        if (type == null) type = AugmentType.EMPTY;
        stack.getOrCreateNbt().putString("Type", type.getName());
    }

    public int getAmount(@NotNull ItemStack stack) {
        NbtCompound nbtCompound = stack.getNbt();
        if(nbtCompound == null) return 0;

        return nbtCompound.getInt("Amount");
    }
    public void setAmount(@NotNull ItemStack stack, int amount) {
        stack.getOrCreateNbt().putInt("Amount", amount);
    }
    public void addAmount(@NotNull ItemStack stack, int amount) {
        this.setAmount(stack, this.getAmount(stack) + amount);
    }

    public int getCapacity(@NotNull ItemStack stack) {
        List<Item> upgrades = this.getUpgrades(stack, ZItemTags.AUGMENT_CAPACITY_ENHANCERS);
        AugmentType type = this.getType(stack);

        return type.getCapacity(upgrades.size());
    }
    public int getFreeSpace(@NotNull ItemStack stack){
        return this.getCapacity(stack) - this.getAmount(stack);
    }

    public boolean putUpgrade(@NotNull ItemStack stack, ItemStack upgrade) {
        NbtCompound stackNbt = stack.copy().getOrCreateNbt();
        NbtList nbtList = stackNbt.getList("Upgrades", NbtElement.COMPOUND_TYPE);

        if(Ingredient.fromTag(ZItemTags.AUGMENT_CAPACITY_ENHANCERS).test(upgrade)) {
            List<Item> capacityUpgrades = this.getUpgrades(stack, ZItemTags.AUGMENT_CAPACITY_ENHANCERS);
            if(capacityUpgrades.size() >= CAPACITY_UPGRADE_SLOTS) return false;
        }

        String upgradeId = Registry.ITEM.getId(upgrade.getItem()).toString();

        for (NbtElement nbtElement : nbtList) {
            if(nbtElement instanceof NbtCompound nbtCompound) {
                if(nbtCompound.getString("id").equals(upgradeId)) return false;
            }
        }

        NbtCompound itemNbt = new NbtCompound();
        itemNbt.putString("id", upgradeId);
        nbtList.add(itemNbt);

        stackNbt.put("Upgrades", nbtList);
        stack.setNbt(stackNbt);

        return true;
    }

    public List<Item> getUpgrades(@NotNull ItemStack stack) {
        NbtCompound stackNbt = stack.copy().getOrCreateNbt();
        NbtList nbtList = stackNbt.getList("Upgrades", NbtElement.COMPOUND_TYPE);

        List<Item> upgradeList = new ArrayList<>();

        for (NbtElement nbtElement : nbtList) {
            if(nbtElement instanceof NbtCompound nbtCompound) {
                String id = nbtCompound.getString("id");
                if(id == null) continue;

                Identifier itemId = Identifier.tryParse(id);
                if(itemId == null) continue;

                Optional<Item> item = Registry.ITEM.getOrEmpty(itemId);
                item.ifPresent(upgradeList::add);
            }
        }

        return upgradeList;
    }
    public List<Item> getUpgrades(@NotNull ItemStack stack, TagKey<Item> itemTag) {
        List<Item> upgradeList = this.getUpgrades(stack);
        Ingredient ingredient = Ingredient.fromTag(itemTag);

        return upgradeList.stream().filter(item -> ingredient.test(item.getDefaultStack())).toList();
    }
}