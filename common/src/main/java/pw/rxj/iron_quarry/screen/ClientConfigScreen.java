package pw.rxj.iron_quarry.screen;

import pw.rxj.iron_quarry.resource.config.ConfigSide;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.resource.config.client.BlockBreakingConfig;
import pw.rxj.iron_quarry.resource.config.client.BlueprintPreviewConfig;
import pw.rxj.iron_quarry.resource.config.client.MonitorPreviewConfig;
import pw.rxj.iron_quarry.resource.config.client.QuarryMonitorOverlayConfig;
import pw.rxj.iron_quarry.types.AbsAlignment;
import pw.rxj.iron_quarry.types.BlueprintHiddenLinesMode;
import pw.rxj.iron_quarry.types.MonitorDepthOffsetMode;
import pw.rxj.iron_quarry.util.ComplexOption;

public class ClientConfigScreen extends OptionsSubScreen {
    private static final BlockBreakingConfig.Handler BLOCK_BREAKING_CONFIG = Main.CONFIG.getBlockBreakingConfig();
    private static final QuarryMonitorOverlayConfig.Handler QUARRY_MONITOR_OVERLAY_CONFIG = Main.CONFIG.getQuarryMonitorOverlayConfig();
    private static final BlueprintPreviewConfig.Handler BLUEPRINT_PREVIEW_CONFIG = Main.CONFIG.getBlueprintPreviewConfig();
    private static final MonitorPreviewConfig.Handler MONITOR_PREVIEW_CONFIG = Main.CONFIG.getMonitorPreviewConfig();

    public static OptionInstance<Integer> BLOCK_BREAK_DISTANCE_OPTION = ComplexOption.sliderOptionFrom(
            "screen.iron_quarry.client_config.block_break_distance",
            ComplexOption.valueDivider("%.0f", 1),
            true,
            0, 64,
            BLOCK_BREAKING_CONFIG.getOptionDistance(),
            BLOCK_BREAKING_CONFIG::setOptionDistance
    );
    public static OptionInstance<Integer> BLOCK_BREAK_VOLUME_OPTION = ComplexOption.sliderOptionFrom(
            "screen.iron_quarry.client_config.block_break_volume",
            ComplexOption.valueDivider("%.0f", 1),
            true,
            0, 100,
            BLOCK_BREAKING_CONFIG.getOptionVolume(),
            BLOCK_BREAKING_CONFIG::setOptionVolume
    );

    public static OptionInstance<String> QUARRY_MONITOR_ALIGNMENT = ComplexOption.rotatingOptionFrom(
            "screen.iron_quarry.client_config.quarry_monitor_alignment",
            true,
            AbsAlignment.class,
            QUARRY_MONITOR_OVERLAY_CONFIG.getOptionAlignment(),
            QUARRY_MONITOR_OVERLAY_CONFIG::setOptionAlignment
    );
    public static OptionInstance<Integer> QUARRY_MONITOR_X = ComplexOption.sliderOptionFrom(
            "screen.iron_quarry.client_config.quarry_monitor_x",
            ComplexOption.valueDivider("%.0f", 1),
            true,
            0, 128,
            QUARRY_MONITOR_OVERLAY_CONFIG.getX(),
            QUARRY_MONITOR_OVERLAY_CONFIG::setX
    );
    public static OptionInstance<Integer> QUARRY_MONITOR_Y = ComplexOption.sliderOptionFrom(
            "screen.iron_quarry.client_config.quarry_monitor_y",
            ComplexOption.valueDivider("%.0f", 1),
            true,
            0, 128,
            QUARRY_MONITOR_OVERLAY_CONFIG.getY(),
            QUARRY_MONITOR_OVERLAY_CONFIG::setY
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

        this.list.addSmall(QUARRY_MONITOR_ALIGNMENT, BLOCK_BREAK_DISTANCE_OPTION,
                         QUARRY_MONITOR_X,         BLOCK_BREAK_VOLUME_OPTION,
                         QUARRY_MONITOR_Y);

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
