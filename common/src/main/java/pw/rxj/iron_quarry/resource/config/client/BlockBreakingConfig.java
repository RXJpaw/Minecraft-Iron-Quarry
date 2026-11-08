package pw.rxj.iron_quarry.resource.config.client;

import pw.rxj.iron_quarry.resource.config.AbstractInnerConfigHandler;
import pw.rxj.iron_quarry.resource.config.ConfigHandler;
import pw.rxj.iron_quarry.resource.config.util.Comment;
import pw.rxj.iron_quarry.resource.config.util.Range;

public class BlockBreakingConfig {
    @Comment("Block breaking sound volume.")
    @Range(min = 0, max = 1)
    public float volume = 1.0F;
    @Comment("Max. distance for particles & sounds.")
    @Range(min = 0, max = 64)
    public float distance = 64.0F;

    public static class Handler extends AbstractInnerConfigHandler {
        protected Handler(ConfigHandler handler) {
            super(handler);
        }
        public static Handler of(ConfigHandler handler) {
            return new Handler(handler);
        }

        public void setVolume(float volume) {
            this.getClient().blockBreaking.volume = volume;
        }
        public float getVolume() {
            return this.getClient().blockBreaking.volume;
        }
        public void setOptionVolume(int volume) {
            this.setVolume(volume / 100.0F);
        }
        public int getOptionVolume() {
            return (int) (this.getVolume() * 100);
        }

        public void setDistance(float distance) {
            this.getClient().blockBreaking.distance = distance;
        }
        public float getDistance() {
            return this.getClient().blockBreaking.distance;
        }
        public void setOptionDistance(int distance) {
            this.setDistance(distance);
        }
        public int getOptionDistance() {
            return (int) (this.getDistance());
        }
    }
}
