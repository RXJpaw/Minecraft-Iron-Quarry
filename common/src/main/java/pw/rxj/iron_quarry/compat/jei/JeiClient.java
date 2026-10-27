package pw.rxj.iron_quarry.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.Identifier;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.screen.QuarryBlockScreen;
import pw.rxj.iron_quarry.util.TrackableZone;

import java.util.ArrayList;
import java.util.List;

public class JeiClient implements IModPlugin {
    private static final Identifier ID = Identifier.fromNamespaceAndPath(Main.MOD_ID, "jei_client");

    @Override
    public Identifier getPluginUid() {
        return ID;
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
