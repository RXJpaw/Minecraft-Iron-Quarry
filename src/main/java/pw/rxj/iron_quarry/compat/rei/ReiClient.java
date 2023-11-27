package pw.rxj.iron_quarry.compat.rei;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
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
}
