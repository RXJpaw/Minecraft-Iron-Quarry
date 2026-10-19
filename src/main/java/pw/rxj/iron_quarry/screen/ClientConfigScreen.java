package pw.rxj.iron_quarry.screen;

import net.fabricmc.api.EnvType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.resource.config.client.BlockBreakingConfig;
import pw.rxj.iron_quarry.resource.config.client.QuarryMonitorOverlayConfig;
import pw.rxj.iron_quarry.types.AbsAlignment;
import pw.rxj.iron_quarry.util.ComplexOption;

public class ClientConfigScreen extends GameOptionsScreen {
    private static final BlockBreakingConfig.Handler BLOCK_BREAKING_CONFIG = Main.CONFIG.getBlockBreakingConfig();
    private static final QuarryMonitorOverlayConfig.Handler QUARRY_MONITOR_OVERLAY_CONFIG = Main.CONFIG.getQuarryMonitorOverlayConfig();

    public static SimpleOption<Integer> BLOCK_BREAK_DISTANCE_OPTION = ComplexOption.sliderOptionFrom(
            "screen.iron_quarry.client_config.block_break_distance",
            ComplexOption.valueDivider("%.0f", 1),
            true,
            0, 64,
            BLOCK_BREAKING_CONFIG.getOptionDistance(),
            BLOCK_BREAKING_CONFIG::setOptionDistance
    );
    public static SimpleOption<Integer> BLOCK_BREAK_VOLUME_OPTION = ComplexOption.sliderOptionFrom(
            "screen.iron_quarry.client_config.block_break_volume",
            ComplexOption.valueDivider("%.0f", 1),
            true,
            0, 100,
            BLOCK_BREAKING_CONFIG.getOptionVolume(),
            BLOCK_BREAKING_CONFIG::setOptionVolume
    );

    public static SimpleOption<String> QUARRY_MONITOR_ALIGNMENT = ComplexOption.rotatingOptionFrom(
            "screen.iron_quarry.client_config.quarry_monitor_alignment",
            true,
            AbsAlignment.class,
            QUARRY_MONITOR_OVERLAY_CONFIG.getOptionAlignment(),
            QUARRY_MONITOR_OVERLAY_CONFIG::setOptionAlignment
    );
    public static SimpleOption<Integer> QUARRY_MONITOR_X = ComplexOption.sliderOptionFrom(
            "screen.iron_quarry.client_config.quarry_monitor_x",
            ComplexOption.valueDivider("%.0f", 1),
            true,
            0, 128,
            QUARRY_MONITOR_OVERLAY_CONFIG.getX(),
            QUARRY_MONITOR_OVERLAY_CONFIG::setX
    );
    public static SimpleOption<Integer> QUARRY_MONITOR_Y = ComplexOption.sliderOptionFrom(
            "screen.iron_quarry.client_config.quarry_monitor_y",
            ComplexOption.valueDivider("%.0f", 1),
            true,
            0, 128,
            QUARRY_MONITOR_OVERLAY_CONFIG.getY(),
            QUARRY_MONITOR_OVERLAY_CONFIG::setY
    );


    public ClientConfigScreen(Screen parent) {
        super(parent, MinecraftClient.getInstance().options, Text.translatable("screen.iron_quarry.client_config.title"));
    }

    @Override
    protected void addOptions() {
        if(this.body == null) return;

        this.body.addAll(QUARRY_MONITOR_ALIGNMENT, BLOCK_BREAK_DISTANCE_OPTION,
                         QUARRY_MONITOR_X,         BLOCK_BREAK_VOLUME_OPTION,
                         QUARRY_MONITOR_Y);
    }

    @Override
    public void removed() {
        Main.CONFIG.write(EnvType.CLIENT);
    }
}
