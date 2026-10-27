package pw.rxj.iron_quarry.compat.rei;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.client.registry.transfer.simple.SimpleTransferHandler;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import net.minecraft.world.inventory.CraftingMenu;
import pw.rxj.iron_quarry.screen.QuarryBlockScreen;
import pw.rxj.iron_quarry.util.TrackableZone;

import java.util.ArrayList;
import java.util.List;

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
    public void registerTransferHandlers(TransferHandlerRegistry registry) {
        registry.register(HandledCraftingTransferHandler.of(SimpleTransferHandler.create(CraftingMenu.class, BuiltinPlugin.CRAFTING, new SimpleTransferHandler.IntRange(1, 10))));
    }
}
