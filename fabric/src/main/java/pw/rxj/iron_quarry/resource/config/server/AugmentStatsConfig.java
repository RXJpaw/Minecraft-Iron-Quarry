package pw.rxj.iron_quarry.resource.config.server;

import lombok.EqualsAndHashCode;
import pw.rxj.iron_quarry.resource.config.AbstractInnerConfigHandler;
import pw.rxj.iron_quarry.resource.config.ConfigHandler;
import pw.rxj.iron_quarry.types.AugmentType;

@EqualsAndHashCode
public class AugmentStatsConfig {
    public final Entry speed = new Entry(AugmentType.SPEED);
    public final Entry fortune = new Entry(AugmentType.FORTUNE);
    public final Entry silkTouch = new Entry(AugmentType.SILK_TOUCH);
    public final Entry chestLooting = new Entry(AugmentType.CHEST_LOOTING);

    @EqualsAndHashCode
    public static class Entry {
        public final boolean disabled;
        public final int baseAmount;
        public final int maxEfficiency;
        public final float inefficiency;

        public Entry(AugmentType augmentType) {
            this.disabled = augmentType.isDisabled();
            this.baseAmount = augmentType.getBaseAmount();
            this.maxEfficiency = augmentType.getMaxEfficiency();
            this.inefficiency = augmentType.getInefficiency();
        }
    }

    public static class Handler extends AbstractInnerConfigHandler {
        private Handler(ConfigHandler handler) {
            super(handler);
        }
        public static Handler of(ConfigHandler handler) {
            return new Handler(handler);
        }

        public AugmentStatsConfig.Entry getSpeed() {
            return this.server.augmentStats.speed;
        }
        public AugmentStatsConfig.Entry getFortune() {
            return this.server.augmentStats.fortune;
        }
        public AugmentStatsConfig.Entry getSilkTouch() {
            return this.server.augmentStats.silkTouch;
        }
        public AugmentStatsConfig.Entry getChestLooting() {
            return this.server.augmentStats.chestLooting;
        }

        @Override
        public void applyChanges() {
            AugmentType.SPEED.override(this.getSpeed());
            AugmentType.FORTUNE.override(this.getFortune());
            AugmentType.SILK_TOUCH.override(this.getSilkTouch());
            AugmentType.CHEST_LOOTING.override(this.getChestLooting());
        }
    }
}
