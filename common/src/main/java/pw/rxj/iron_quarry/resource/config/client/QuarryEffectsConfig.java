package pw.rxj.iron_quarry.resource.config.client;

import pw.rxj.iron_quarry.resource.config.AbstractInnerConfigHandler;
import pw.rxj.iron_quarry.resource.config.ConfigHandler;
import pw.rxj.iron_quarry.resource.config.util.Comment;
import pw.rxj.iron_quarry.resource.config.util.Range;

public class QuarryEffectsConfig {
    @Comment("Block breaking sound volume.")
    @Range(min = 0, max = 1)
    public float miningVolume = 1.0F;
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

        public void setMiningVolume(float miningVolume) {
            this.getClient().quarryEffects.miningVolume = miningVolume;
        }
        public float getMiningVolume() {
            return this.getClient().quarryEffects.miningVolume;
        }
        public void setOptionMiningVolume(int miningVolume) {
            this.setMiningVolume(miningVolume / 100.0F);
        }
        public int getOptionMiningVolume() {
            return (int) (this.getMiningVolume() * 100);
        }

        public void setDistance(float distance) {
            this.getClient().quarryEffects.distance = distance;
        }
        public float getDistance() {
            return this.getClient().quarryEffects.distance;
        }
        public void setOptionDistance(int distance) {
            this.setDistance(distance);
        }
        public int getOptionDistance() {
            return (int) (this.getDistance());
        }
    }
}
