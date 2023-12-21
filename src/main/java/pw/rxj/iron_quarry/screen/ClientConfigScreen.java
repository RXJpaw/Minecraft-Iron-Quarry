package pw.rxj.iron_quarry.screen;

import net.fabricmc.api.EnvType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.resource.ConfigHandler;
import pw.rxj.iron_quarry.util.ComplexOption;

public class ClientConfigScreen extends GameOptionsScreen {
    private static final ConfigHandler.BlockBreakingConfigHandler BlockBreakingConfig = Main.CONFIG.getBlockBreakingConfig();
    private final Screen parent;
    private ButtonListWidget list;


    public static SimpleOption<Integer> BLOCK_BREAK_DISTANCE_OPTION = ComplexOption.sliderOptionFrom(
            "screen.iron_quarry.client_config.block_break_distance",
            ComplexOption.valueDivider("%.0f", 1),
            true,
            0, 64,
            BlockBreakingConfig.getOptionDistance(),
            BlockBreakingConfig::setOptionDistance
    );
    public static SimpleOption<Integer> BLOCK_BREAK_VOLUME_OPTION = ComplexOption.sliderOptionFrom(
            "screen.iron_quarry.client_config.block_break_volume",
            ComplexOption.valueDivider("%.0f", 1),
            true,
            0, 100,
            BlockBreakingConfig.getOptionVolume(),
            BlockBreakingConfig::setOptionVolume
    );


    public ClientConfigScreen(Screen parent) {
        super(parent, MinecraftClient.getInstance().options, Text.translatable("screen.iron_quarry.client_config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.list = new ButtonListWidget(this.client, this.width, this.height, 32, this.height - 32, 25);
        this.list.addAll(new SimpleOption[]{ BLOCK_BREAK_DISTANCE_OPTION, BLOCK_BREAK_VOLUME_OPTION });
        this.addSelectableChild(this.list);
        this.addDrawableChild(new ButtonWidget(
                this.width / 2 - 100, this.height - 27,
                200, 20,
                ScreenTexts.DONE, (button) -> {
                    if(this.client != null) this.client.setScreen(this.parent);
                }
        ));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.list.render(matrices, mouseX, mouseY, delta);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 15, 16777215);
        super.render(matrices, mouseX, mouseY, delta);
        this.renderOrderedTooltip(matrices, getHoveredButtonTooltip(this.list, mouseX, mouseY), mouseX, mouseY + 25);
    }

    @Override
    public void removed() {
        Main.CONFIG.write(EnvType.CLIENT);
    }
}
