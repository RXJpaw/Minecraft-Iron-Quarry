package pw.rxj.iron_quarry.screen;

import pw.rxj.iron_quarry.resource.config.ConfigSide;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.resource.config.client.BlueprintPreviewConfig;
import pw.rxj.iron_quarry.resource.config.client.MonitorOverlayConfig;
import pw.rxj.iron_quarry.resource.config.client.MonitorPreviewConfig;
import pw.rxj.iron_quarry.resource.config.client.QuarryEffectsConfig;
import pw.rxj.iron_quarry.types.AbsAlignment;
import pw.rxj.iron_quarry.types.BlueprintHiddenLinesMode;
import pw.rxj.iron_quarry.types.MonitorDepthOffsetMode;
import pw.rxj.iron_quarry.util.ComplexOption;

public class ClientConfigScreen extends OptionsSubScreen {
    private static final QuarryEffectsConfig.Handler QUARRY_EFFECTS_CONFIG = Main.CONFIG.getQuarryEffectsConfig();
    private static final MonitorOverlayConfig.Handler MONITOR_OVERLAY_CONFIG = Main.CONFIG.getMonitorOverlayConfig();
    private static final BlueprintPreviewConfig.Handler BLUEPRINT_PREVIEW_CONFIG = Main.CONFIG.getBlueprintPreviewConfig();
    private static final MonitorPreviewConfig.Handler MONITOR_PREVIEW_CONFIG = Main.CONFIG.getMonitorPreviewConfig();

    public static OptionInstance<Integer> QUARRY_EFFECTS_DISTANCE = ComplexOption.sliderOptionFrom(
            "screen.iron_quarry.client_config.quarry_effects.distance",
            ComplexOption.valueDivider("%.0f", 1),
            true,
            0, 64,
            QUARRY_EFFECTS_CONFIG.getOptionDistance(),
            QUARRY_EFFECTS_CONFIG::setOptionDistance
    );
    public static OptionInstance<Integer> QUARRY_EFFECTS_MINING_VOLUME = ComplexOption.sliderOptionFrom(
            "screen.iron_quarry.client_config.quarry_effects.mining_volume",
            ComplexOption.valueDivider("%.0f", 1),
            true,
            0, 100,
            QUARRY_EFFECTS_CONFIG.getOptionMiningVolume(),
            QUARRY_EFFECTS_CONFIG::setOptionMiningVolume
    );

    public static OptionInstance<String> MONITOR_OVERLAY_ALIGNMENT = ComplexOption.rotatingOptionFrom(
            "screen.iron_quarry.client_config.monitor_overlay.alignment",
            true,
            AbsAlignment.class,
            MONITOR_OVERLAY_CONFIG.getOptionAlignment(),
            MONITOR_OVERLAY_CONFIG::setOptionAlignment
    );
    public static OptionInstance<Integer> MONITOR_OVERLAY_X = ComplexOption.sliderOptionFrom(
            "screen.iron_quarry.client_config.monitor_overlay.x",
            ComplexOption.valueDivider("%.0f", 1),
            true,
            0, 128,
            MONITOR_OVERLAY_CONFIG.getX(),
            MONITOR_OVERLAY_CONFIG::setX
    );
    public static OptionInstance<Integer> MONITOR_OVERLAY_Y = ComplexOption.sliderOptionFrom(
            "screen.iron_quarry.client_config.monitor_overlay.y",
            ComplexOption.valueDivider("%.0f", 1),
            true,
            0, 128,
            MONITOR_OVERLAY_CONFIG.getY(),
            MONITOR_OVERLAY_CONFIG::setY
    );

    public static OptionInstance<String> BLUEPRINT_HIDDEN_LINES = ComplexOption.resolvingOptionFrom(
            "screen.iron_quarry.client_config.blueprint_preview.hidden_lines",
            BLUEPRINT_PREVIEW_CONFIG::resolveOptionHiddenLines,
            BlueprintHiddenLinesMode.class,
            BLUEPRINT_PREVIEW_CONFIG.getHiddenLines().getTranslationKey(),
            BLUEPRINT_PREVIEW_CONFIG::setOptionHiddenLines
    );
    public static OptionInstance<String> MONITOR_DEPTH_OFFSET = ComplexOption.resolvingOptionFrom(
            "screen.iron_quarry.client_config.monitor_preview.depth_offset",
            MONITOR_PREVIEW_CONFIG::resolveOptionDepthOffset,
            MonitorDepthOffsetMode.class,
            MONITOR_PREVIEW_CONFIG.getDepthOffset().getTranslationKey(),
            MONITOR_PREVIEW_CONFIG::setOptionDepthOffset
    );


    public ClientConfigScreen(Screen parent) {
        super(parent, Minecraft.getInstance().options, Component.translatable("screen.iron_quarry.client_config.title"));
    }

    @Override
    protected void addOptions() {
        if(this.list == null) return;

        this.list.addHeader(Component.translatable("screen.iron_quarry.client_config.quarry_effects"));
        this.list.addSmall(QUARRY_EFFECTS_MINING_VOLUME, QUARRY_EFFECTS_DISTANCE);
        this.list.addHeader(Component.translatable("screen.iron_quarry.client_config.monitor_overlay"));
        this.list.addBig(MONITOR_OVERLAY_ALIGNMENT);
        this.list.addSmall(MONITOR_OVERLAY_X, MONITOR_OVERLAY_Y);
        this.list.addHeader(Component.translatable("screen.iron_quarry.client_config.blueprint_preview"));
        this.list.addBig(BLUEPRINT_HIDDEN_LINES);
        this.list.addHeader(Component.translatable("screen.iron_quarry.client_config.monitor_preview"));
        this.list.addBig(MONITOR_DEPTH_OFFSET);
    }

    @Override
    public void removed() {
        Main.CONFIG.write(ConfigSide.CLIENT);
    }
}
