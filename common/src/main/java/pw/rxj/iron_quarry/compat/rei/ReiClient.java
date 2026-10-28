package pw.rxj.iron_quarry.compat.rei;

import me.shedaniel.math.Rectangle;
import dev.architectury.event.EventResult;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.client.registry.transfer.simple.SimpleTransferHandler;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.crafting.RecipeHolder;
import pw.rxj.iron_quarry.platform.Platform;
import pw.rxj.iron_quarry.recipe.SmithingAugmentCapacityRecipe;
import pw.rxj.iron_quarry.screen.QuarryBlockScreen;
import pw.rxj.iron_quarry.util.TrackableZone;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReiClient implements REIClientPlugin {
    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        zones.register(QuarryBlockScreen.class, screen -> {
            List<Rectangle> rectangles = new ArrayList<>();

            for (TrackableZone.Zone zone : screen.getExclusionZones()) {
                rectangles.add(new Rectangle(zone.x, zone.y, zone.width, zone.height));
            }

            return rectangles;
        });
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerVisibilityPredicate((category, display) -> {
            if(display.getOutputEntries().size() != 1 || display.getOutputEntries().getFirst().size() <= 1) return EventResult.pass();

            Optional<Identifier> location = display.getDisplayLocation();
            if(location.isEmpty()) return EventResult.pass();

            RecipeHolder<?> recipeHolder = Platform.RECIPE.getSynchronizedRecipe(ResourceKey.create(Registries.RECIPE, location.get()));
            if(recipeHolder == null || !(recipeHolder.value() instanceof SmithingAugmentCapacityRecipe)) return EventResult.pass();

            return EventResult.interruptFalse();
        });
    }

    @Override
    public void registerTransferHandlers(TransferHandlerRegistry registry) {
        registry.register(HandledCraftingTransferHandler.of(SimpleTransferHandler.create(CraftingMenu.class, BuiltinPlugin.CRAFTING, new SimpleTransferHandler.IntRange(1, 10))));
    }
}
