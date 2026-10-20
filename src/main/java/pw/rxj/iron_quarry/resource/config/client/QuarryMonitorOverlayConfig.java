package pw.rxj.iron_quarry.resource.config.client;

import com.mojang.blaze3d.platform.Window;
import lombok.EqualsAndHashCode;
import pw.rxj.iron_quarry.resource.config.AbstractInnerConfigHandler;
import pw.rxj.iron_quarry.resource.config.ConfigHandler;
import pw.rxj.iron_quarry.types.AbsAlignment;

@EqualsAndHashCode
public class QuarryMonitorOverlayConfig {
    public String alignment = AbsAlignment.BOTTOM_LEFT.getTranslationKey();
    public int x = 8;
    public int y = 8;

    public static class Handler extends AbstractInnerConfigHandler {
        protected Handler(ConfigHandler handler) {
            super(handler);
        }
        public static QuarryMonitorOverlayConfig.Handler of(ConfigHandler handler) {
            return new QuarryMonitorOverlayConfig.Handler(handler);
        }

        public void setAlignment(AbsAlignment alignment) {
            this.setOptionAlignment(alignment.getTranslationKey());
        }
        public AbsAlignment getAlignment() {
            return AbsAlignment.byTranslationKey(this.getOptionAlignment());
        }
        public void setOptionAlignment(String alignment) {
            this.client.quarryMonitorOverlay.alignment = alignment;
        }
        public String getOptionAlignment() {
            return this.client.quarryMonitorOverlay.alignment;
        }

        public void setX(int x) {
            this.client.quarryMonitorOverlay.x = x;
        }
        public int getX() {
            return this.client.quarryMonitorOverlay.x;
        }
        public int getComputedWindowX(Window window, int width) {
            return switch (this.getAlignment()) {
                case TOP_LEFT, BOTTOM_LEFT -> this.getX();
                case TOP_RIGHT, BOTTOM_RIGHT -> window.getGuiScaledWidth() - width - this.getX();
                default -> 0;
            };
        }

        public void setY(int y) {
            this.client.quarryMonitorOverlay.y = y;
        }
        public int getY() {
            return this.client.quarryMonitorOverlay.y;
        }
        public int getComputedWindowY(Window window, int height) {
            return switch (this.getAlignment()) {
                case TOP_LEFT, TOP_RIGHT -> this.getY();
                case BOTTOM_LEFT, BOTTOM_RIGHT -> window.getGuiScaledHeight() - height - this.getY();
                default -> 0;
            };
        }
    }
}
