package pw.rxj.iron_quarry.resource.config.client;

import lombok.EqualsAndHashCode;
import pw.rxj.iron_quarry.resource.config.AbstractInnerConfigHandler;
import pw.rxj.iron_quarry.resource.config.ConfigHandler;

@EqualsAndHashCode
public class BlockBreakingConfig {
    public float volume = 1.0F;
    public float distance = 64.0F;

    public static class Handler extends AbstractInnerConfigHandler {
        protected Handler(ConfigHandler handler) {
            super(handler);
        }
        public static Handler of(ConfigHandler handler) {
            return new Handler(handler);
        }

        public void setVolume(float volume) {
            this.client.blockBreaking.volume = volume;
        }
        public float getVolume() {
            return this.client.blockBreaking.volume;
        }
        public void setOptionVolume(int volume) {
            this.setVolume(volume / 100.0F);
        }
        public int getOptionVolume() {
            return (int) (this.getVolume() * 100);
        }

        public void setDistance(float distance) {
            this.client.blockBreaking.distance = distance;
        }
        public float getDistance() {
            return this.client.blockBreaking.distance;
        }
        public void setOptionDistance(int distance) {
            this.setDistance(distance);
        }
        public int getOptionDistance() {
            return (int) (this.getDistance());
        }
    }
}
