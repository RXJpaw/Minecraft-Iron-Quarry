package pw.rxj.iron_quarry;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import pw.rxj.iron_quarry.platform.FabricEventBridge;
import pw.rxj.iron_quarry.recipe.HandledCraftingRecipe;
import pw.rxj.iron_quarry.recipe.HandledSmithingRecipe;
import pw.rxj.iron_quarry.resource.SilkTouchTradingCondition;

public class FabricMain implements ModInitializer {
	@Override
	public void onInitialize() {
		FabricEventBridge.register();

		Main.init();

		ResourceConditions.register(SilkTouchTradingCondition.TYPE);
		RecipeSynchronization.synchronizeRecipeSerializer(HandledCraftingRecipe.SERIALIZER);
		RecipeSynchronization.synchronizeRecipeSerializer(HandledSmithingRecipe.SERIALIZER);
	}
}
