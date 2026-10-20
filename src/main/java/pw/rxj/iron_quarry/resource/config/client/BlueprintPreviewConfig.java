package pw.rxj.iron_quarry.resource.config.client;

import lombok.EqualsAndHashCode;
import pw.rxj.iron_quarry.resource.config.AbstractInnerConfigHandler;
import pw.rxj.iron_quarry.resource.config.ConfigHandler;
import pw.rxj.iron_quarry.types.MarkerRenderMode;

@EqualsAndHashCode
public class BlueprintPreviewConfig {
    public String markerRenderMode = MarkerRenderMode.WORLD.getTranslationKey();

    public static class Handler extends AbstractInnerConfigHandler {
        protected Handler(ConfigHandler handler) {
            super(handler);
        }
        public static BlueprintPreviewConfig.Handler of(ConfigHandler handler) {
            return new BlueprintPreviewConfig.Handler(handler);
        }

        public void setMarkerRenderMode(MarkerRenderMode markerRenderMode) {
            this.setOptionMarkerRenderMode(markerRenderMode.getTranslationKey());
        }
        public MarkerRenderMode getMarkerRenderMode() {
            return MarkerRenderMode.byTranslationKey(this.getOptionMarkerRenderMode());
        }
        public void setOptionMarkerRenderMode(String markerRenderMode) {
            this.client.blueprintPreview.markerRenderMode = markerRenderMode;
        }
        public String getOptionMarkerRenderMode() {
            return this.client.blueprintPreview.markerRenderMode;
        }
    }
}
