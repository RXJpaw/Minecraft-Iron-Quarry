package pw.rxj.iron_quarry.screen;

import net.fabricmc.api.EnvType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.resource.config.client.BlockBreakingConfig;
import pw.rxj.iron_quarry.resource.config.client.QuarryMonitorOverlayConfig;
import pw.rxj.iron_quarry.types.AbsAlignment;
import pw.rxj.iron_quarry.util.ComplexOption;

public class ClientConfigScreen extends GameOptionsScreen {
    private static final BlockBreakingConfig.Handler BLOCK_BREAKING_CONFIG = Main.CONFIG.getBlockBreakingConfig();
    private static final QuarryMonitorOverlayConfig.Handler QUARRY_MONITOR_OVERLAY_CONFIG = Main.CONFIG.getQuarryMonitorOverlayConfig();
    private final Screen parent;
    private OptionListWidget list;

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
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.list = new OptionListWidget(this.client, this.width, this.height - 64, 32, 25);
        this.list.addAll(new SimpleOption[]{ QUARRY_MONITOR_ALIGNMENT, BLOCK_BREAK_DISTANCE_OPTION,
                                             QUARRY_MONITOR_X,         BLOCK_BREAK_VOLUME_OPTION,
                                             QUARRY_MONITOR_Y,         null});
        this.addSelectableChild(this.list);

        this.addDrawableChild(
                ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
                    if(this.client != null) this.client.setScreen(this.parent);
                })
                .position(this.width / 2 - 100, this.height - 27)
                .size(200, 20)
                .build()
        );
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.list.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 16777215);
    }

    @Override
    public void removed() {
        Main.CONFIG.write(EnvType.CLIENT);
    }
}
