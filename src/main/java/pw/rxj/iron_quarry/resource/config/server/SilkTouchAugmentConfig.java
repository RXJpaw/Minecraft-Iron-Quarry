package pw.rxj.iron_quarry.resource.config.server;

import lombok.EqualsAndHashCode;
import pw.rxj.iron_quarry.resource.config.AbstractInnerConfigHandler;
import pw.rxj.iron_quarry.resource.config.ConfigHandler;

@EqualsAndHashCode
public class SilkTouchAugmentConfig {
    public final boolean tradingEnabled = true;

    public static class Handler extends AbstractInnerConfigHandler {
        private Handler(ConfigHandler handler) {
            super(handler);
        }
        public static Handler of(ConfigHandler handler) {
            return new Handler(handler);
        }

        public boolean isTradingEnabled() {
            return this.server.silkTouchAugment.tradingEnabled;
        }
    }
}
