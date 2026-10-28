package pw.rxj.iron_quarry.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.recipe.category.extensions.vanilla.smithing.IExtendableSmithingRecipeCategory;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.item.BlueprintItem;
import pw.rxj.iron_quarry.item.ZItems;
import pw.rxj.iron_quarry.recipe.SmithingAugmentCapacityRecipe;
import pw.rxj.iron_quarry.recipe.SmithingAugmentRecipe;
import pw.rxj.iron_quarry.recipe.SmithingBlueprintSealRecipe;
import pw.rxj.iron_quarry.screen.QuarryBlockScreen;
import pw.rxj.iron_quarry.util.TrackableZone;

import java.util.ArrayList;
import java.util.List;

public class JeiClient implements IModPlugin {
    private static final Identifier ID = Identifier.fromNamespaceAndPath(Main.MOD_ID, "jei_client");
    private static final Identifier BLUEPRINT_UNSEALING_ID = Identifier.fromNamespaceAndPath(Main.MOD_ID, "blueprint_unsealing");

    @Override
    public Identifier getPluginUid() {
        return ID;
    }

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        IExtendableSmithingRecipeCategory smithingCategory = registration.getSmithingCategory();
        HandledSmithingCategoryExtension extension = new HandledSmithingCategoryExtension();

        smithingCategory.addExtension(SmithingAugmentRecipe.class, extension);
        smithingCategory.addExtension(SmithingAugmentCapacityRecipe.class, extension);
        smithingCategory.addExtension(SmithingBlueprintSealRecipe.class, extension);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        BlueprintItem blueprintItem = ZItems.BLUEPRINT.getItem();
        ItemStack sealedBlueprint = new ItemStack(blueprintItem);
        blueprintItem.setSealed(sealedBlueprint, true);

        IVanillaRecipeFactory factory = registration.getVanillaRecipeFactory();
        registration.addRecipes(RecipeTypes.GRINDSTONE, List.of(
                factory.createGrindstoneRecipe(List.of(sealedBlueprint), List.of(), List.of(blueprintItem.getGrindingOutput(sealedBlueprint, ItemStack.EMPTY)), 0, 0, BLUEPRINT_UNSEALING_ID)
        ));

        registration.addItemStackInfo(sealedBlueprint, Component.translatable("item.iron_quarry.blueprint.info.unsealing"));
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(QuarryBlockScreen.class, new IGuiContainerHandler<QuarryBlockScreen>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(QuarryBlockScreen screen) {
                List<Rect2i> rectangles = new ArrayList<>();

                for (TrackableZone.Zone zone : screen.getExclusionZones()) {
                    rectangles.add(new Rect2i(zone.x, zone.y, zone.width, zone.height));
                }

                return rectangles;
            }
        });
    }
}
