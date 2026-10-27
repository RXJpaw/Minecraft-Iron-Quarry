package pw.rxj.iron_quarry.compat.rei;

import me.shedaniel.rei.api.client.ClientHelper;
import me.shedaniel.rei.api.client.registry.transfer.simple.SimpleTransferHandler;
import me.shedaniel.rei.api.common.entry.InputIngredient;
import me.shedaniel.rei.api.common.transfer.info.stack.SlotAccessor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.platform.Platform;
import pw.rxj.iron_quarry.recipe.HandledCraftingRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HandledCraftingTransferHandler implements SimpleTransferHandler {
    private static final int CENTER_DISPLAY_INDEX = 4;

    private final SimpleTransferHandler parent;

    private HandledCraftingTransferHandler(SimpleTransferHandler parent) {
        this.parent = parent;
    }
    public static HandledCraftingTransferHandler of(SimpleTransferHandler parent) {
        return new HandledCraftingTransferHandler(parent);
    }

    @Override
    public double getPriority() {
        return 1.0D;
    }

    @Override
    public ApplicabilityResult checkApplicable(Context context) {
        ApplicabilityResult applicabilityResult = this.parent.checkApplicable(context);
        if(!applicabilityResult.isApplicable()) return applicabilityResult;
        if(!ClientHelper.getInstance().canUseMovePackets()) return ApplicabilityResult.createNotApplicable();

        HandledCraftingRecipe recipe = getHandledCraftingRecipe(context);
        if(recipe == null || recipe.getWidth() != 3 || recipe.getHeight() != 3) return ApplicabilityResult.createNotApplicable();

        return applicabilityResult;
    }

    @Override
    public Iterable<SlotAccessor> getInputSlots(Context context) {
        return this.parent.getInputSlots(context);
    }

    @Override
    public Iterable<SlotAccessor> getInventorySlots(Context context) {
        return this.parent.getInventorySlots(context);
    }

    @Override
    public List<InputIngredient<ItemStack>> getInputsIndexed(Context context) {
        List<InputIngredient<ItemStack>> inputs = new ArrayList<>();

        for (InputIngredient<ItemStack> input : this.parent.getInputsIndexed(context)) {
            inputs.add(input.getDisplayIndex() == CENTER_DISPLAY_INDEX ? this.withInventoryStacks(context, input) : input);
        }

        return inputs;
    }

    private InputIngredient<ItemStack> withInventoryStacks(Context context, InputIngredient<ItemStack> input) {
        List<ItemStack> stacks = new ArrayList<>(input.get());

        for (SlotAccessor slot : this.getInventorySlots(context)) {
            ItemStack inventoryStack = slot.getItemStack();
            if(inventoryStack.isEmpty() || inventoryStack.getComponentsPatch().isEmpty()) continue;

            if(input.get().stream().anyMatch(stack -> stack.is(inventoryStack.getItem()))) {
                stacks.add(inventoryStack.copyWithCount(1));
            }
        }

        return InputIngredient.of(input.getIndex(), input.getDisplayIndex(), stacks);
    }

    private static @Nullable HandledCraftingRecipe getHandledCraftingRecipe(Context context) {
        ClientLevel world = context.getMinecraft().level;
        if(world == null || context.getDisplay() == null) return null;

        Optional<Identifier> location = context.getDisplay().getDisplayLocation();
        if(location.isEmpty()) return null;

        RecipeHolder<?> recipeHolder = Platform.RECIPE.getSynchronizedRecipe(ResourceKey.create(Registries.RECIPE, location.get()));
        if(recipeHolder == null || !(recipeHolder.value() instanceof HandledCraftingRecipe recipe)) return null;

        return recipe;
    }
}
