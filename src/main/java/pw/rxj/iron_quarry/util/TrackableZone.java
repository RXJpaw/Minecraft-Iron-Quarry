package pw.rxj.iron_quarry.util;

import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public class TrackableZone {
    public static class Zone {
        public int x;
        public int y;
        public int width;
        public int height;

        private Zone(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public static Zone from(int x, int y, int width, int height) {
            return new Zone(x, y, width, height);
        }
        public static Zone empty() {
            return new Zone(0, 0, 0, 0);
        }
    }

    private @Nullable TriConsumer<Zone, Integer, Integer> mouseOverConsumer = null;
    private @Nullable BiConsumer<Zone, Float> tickDeltaConsumer = null;
    private float maxTicks;
    private float ticks;

    public @NotNull Zone zone = Zone.empty();
    private int mouseX;
    private int mouseY;

    private TrackableZone() {

    }

    public static TrackableZone empty() { return new TrackableZone(); }
    public static TrackableZone bake(Zone zone, int mouseX, int mouseY) {
        return TrackableZone.empty().consume(zone, mouseX, mouseY);
    }

    public TrackableZone onMouseOver(TriConsumer<Zone, Integer, Integer> consumer) {
        this.mouseOverConsumer = consumer;

        return this;
    }
    public TrackableZone onTickDelta(float maxTicks, BiConsumer<Zone, Float> consumer) {
        this.tickDeltaConsumer = consumer;
        this.maxTicks = maxTicks;

        return this;
    }

    public boolean isMouseOver() {
        return isMouseOver(zone.x, zone.y, zone.width, zone.height, mouseX, mouseY);
    }
    public static boolean isMouseOver(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    public boolean consumeTickDelta(float tickDelta) {
        if(this.isMouseOver()) {
            this.ticks = Math.min(this.ticks + tickDelta, maxTicks);

            if(this.mouseOverConsumer != null) this.mouseOverConsumer.accept(this.zone, this.mouseX, this.mouseY);
        } else {
            this.ticks = Math.max(this.ticks - tickDelta, 0);;
        }

        if(this.tickDeltaConsumer != null) this.tickDeltaConsumer.accept(this.zone, this.ticks);

        return this.ticks > 0;
    }

    public float getTicks(){
        return this.ticks;
    }
    public float getMaxTicks(){
        return this.maxTicks;
    }
    public boolean isUsed(){
        return this.ticks > 0;
    }
    public boolean isUnused(){
        return !this.isUsed();
    }

    public TrackableZone consume(Zone zone, int mouseX, int mouseY) {
        this.zone = zone;
        this.mouseX = mouseX;
        this.mouseY = mouseY;

        return this;
    }
    public TrackableZone consume(Zone zone) {
        this.zone = zone;

        return this;
    }
}
