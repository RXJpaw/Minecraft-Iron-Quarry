package pw.rxj.iron_quarry.resource.config.client;

import pw.rxj.iron_quarry.resource.config.AbstractInnerConfigHandler;
import pw.rxj.iron_quarry.resource.config.ConfigHandler;
import pw.rxj.iron_quarry.resource.config.util.Comment;
import pw.rxj.iron_quarry.resource.config.util.Options;
import pw.rxj.iron_quarry.types.MonitorDepthOffsetMode;

public class MonitorPreviewConfig {
    @Comment("""
    How far in front of the placed quarry the monitor draws its preview when you stand near it.
    "precise" (intended): draws the preview exactly on the placed quarry.
    "stable" (fallback): draws the preview a tiny bit closer to you.
    "auto": uses the fallback while a shader pack is in use.""")
    @Options(MonitorDepthOffsetMode.class)
    public String depthOffset = MonitorDepthOffsetMode.AUTO.getTranslationKey();

    public static class Handler extends AbstractInnerConfigHandler {
        protected Handler(ConfigHandler handler) {
            super(handler);
        }
        public static Handler of(ConfigHandler handler) {
            return new Handler(handler);
        }

        public void setDepthOffset(MonitorDepthOffsetMode mode) {
            this.setOptionDepthOffset(mode.getTranslationKey());
        }
        public MonitorDepthOffsetMode getDepthOffset() {
            return MonitorDepthOffsetMode.byTranslationKey(this.getOptionDepthOffset());
        }
        public void setOptionDepthOffset(String mode) {
            this.getClient().monitorPreview.depthOffset = mode;
        }
        public String getOptionDepthOffset() {
            return this.getClient().monitorPreview.depthOffset;
        }
        public String resolveOptionDepthOffset(String mode) {
            return MonitorDepthOffsetMode.byTranslationKey(mode).resolve().getTranslationKey();
        }
        public boolean isDepthOffsetStable() {
            return this.getDepthOffset().resolve() == MonitorDepthOffsetMode.STABLE;
        }
    }
}
