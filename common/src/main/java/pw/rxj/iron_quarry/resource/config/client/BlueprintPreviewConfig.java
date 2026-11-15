package pw.rxj.iron_quarry.resource.config.client;

import pw.rxj.iron_quarry.resource.config.AbstractInnerConfigHandler;
import pw.rxj.iron_quarry.resource.config.ConfigHandler;
import pw.rxj.iron_quarry.resource.config.util.Comment;
import pw.rxj.iron_quarry.types.BlueprintHiddenLinesMode;

public class BlueprintPreviewConfig {
    @Comment("""
    How the parts of the outline that are hidden behind blocks are drawn.
    "transparent" (intended): draws them fainter than the rest.
    "dashed" (fallback): draws them as dashed lines.
    "auto": uses the fallback while a shader pack is in use.""")
    public String hiddenLines = BlueprintHiddenLinesMode.AUTO.getTranslationKey();

    public static class Handler extends AbstractInnerConfigHandler {
        protected Handler(ConfigHandler handler) {
            super(handler);
        }
        public static Handler of(ConfigHandler handler) {
            return new Handler(handler);
        }

        public void setHiddenLines(BlueprintHiddenLinesMode mode) {
            this.setOptionHiddenLines(mode.getTranslationKey());
        }
        public BlueprintHiddenLinesMode getHiddenLines() {
            return BlueprintHiddenLinesMode.byTranslationKey(this.getOptionHiddenLines());
        }
        public void setOptionHiddenLines(String mode) {
            this.getClient().blueprintPreview.hiddenLines = mode;
        }
        public String getOptionHiddenLines() {
            return this.getClient().blueprintPreview.hiddenLines;
        }
        public String resolveOptionHiddenLines(String mode) {
            return BlueprintHiddenLinesMode.byTranslationKey(mode).resolve().getTranslationKey();
        }
        public boolean isHiddenLinesDashed() {
            return this.getHiddenLines().resolve() == BlueprintHiddenLinesMode.DASHED;
        }
    }
}
