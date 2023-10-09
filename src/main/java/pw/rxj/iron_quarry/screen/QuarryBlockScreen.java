package pw.rxj.iron_quarry.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.blockentities.QuarryBlockEntity;
import pw.rxj.iron_quarry.blocks.QuarryBlock;
import pw.rxj.iron_quarry.records.AugmentSlot;
import pw.rxj.iron_quarry.records.IoOption;
import pw.rxj.iron_quarry.records.TexturePosition;
import pw.rxj.iron_quarry.util.ScreenBackgroundButton;
import pw.rxj.iron_quarry.screenhandler.QuarryBlockScreenHandler;
import pw.rxj.iron_quarry.types.Face;
import pw.rxj.iron_quarry.types.IoState;
import pw.rxj.iron_quarry.util.ManagedSlot;

import java.util.List;
import java.util.Optional;

public class QuarryBlockScreen extends HandledScreen<QuarryBlockScreenHandler> {
    //A path to the gui texture. In this example we use the texture from the dispenser
    private final Identifier BACKGROUND_TEXTURE = new Identifier(Main.MOD_ID, "textures/gui/quarry_block_interface.png");
    private final Identifier OPTIONS_TEXTURE = new Identifier(Main.MOD_ID, "textures/gui/options.png");
    private final Identifier OPTIONS_CONFIGURATION_TEXTURE = new Identifier(Main.MOD_ID, "textures/gui/options_configuration.png");
    private final Identifier AUGMENTATION_CONFIGURATION_TEXTURE = new Identifier(Main.MOD_ID, "textures/gui/augmentation_configuration.png");
    private final BlockPos blockPos;

    private ScreenBackgroundButton EnergyDisplay = new ScreenBackgroundButton();
    private ScreenBackgroundButton IoConfigIcon = new ScreenBackgroundButton();
    private ScreenBackgroundButton AugmentsConfigIcon = new ScreenBackgroundButton();

    private final List<IoOption> IoOptions = List.of(
            new IoOption(Face.TOP, Face.TOP, 40, 24),
            new IoOption(Face.LEFT, Face.RIGHT, 20, 44),
            new IoOption(Face.FRONT, Face.FRONT, 40, 44),
            new IoOption(Face.RIGHT, Face.LEFT,60, 44),
            new IoOption(Face.BOTTOM, Face.BOTTOM, 40, 64),
            new IoOption(Face.BACK, Face.BACK, 60, 64)
    );

    private final List<AugmentSlot> AugmentSlots = List.of(
            new AugmentSlot(0, 0, 22, 25),
            new AugmentSlot(1, 1, 40, 25),
            new AugmentSlot(2, 2, 58, 25),
            new AugmentSlot(3, 3, 22, 43),
            new AugmentSlot(4, 4, 40, 43),
            new AugmentSlot(5, 5, 58, 43)
    );

    public QuarryBlockScreen(QuarryBlockScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        this.backgroundWidth = 176 + 100 * 2;
        this.backgroundHeight = 224;

        this.titleY = -1;
        this.titleX = 108;
        this.playerInventoryTitleY = 124;
        this.playerInventoryTitleX = 108;

        blockPos = handler.getPos();
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        MinecraftClient MinecraftInstance = MinecraftClient.getInstance();
        if(MinecraftInstance.world == null) return;

        QuarryBlockEntity blockEntity = (QuarryBlockEntity) MinecraftInstance.world.getBlockEntity(blockPos);
        if(blockEntity == null) return;

        QuarryBlock block = blockEntity.getQuarryBlock();
        if(block == null) return;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int backgroundX = ((this.width - this.backgroundWidth)) / 2 + 100;
        int backgroundY = ((this.height - this.backgroundHeight) - 14) / 2;
        int backgroundWidth = this.backgroundWidth - 200;
        int backgroundHeight = this.backgroundHeight;

        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        drawTexture(matrices, backgroundX, backgroundY, 0, 0, backgroundWidth, backgroundHeight);

        //Energy-Fill-% rendering
        if(blockEntity.EnergyContainer.getStored() > 0) {
            int chargedPixels = Math.max(0, Math.min(40, (int) (blockEntity.EnergyContainer.getFillPercent() * 40)));
            drawTexture(matrices, backgroundX + 10, backgroundY + 17 + 40 - chargedPixels, 179, 57 - chargedPixels, 12, chargedPixels);
        }

        //Battery Slot rendering
        if(!handler.slots.get(7).hasStack()){
            drawTexture(matrices, backgroundX + 11, backgroundY + 63, 180, 60, 10, 14);
        }

        //Energy-Fill-% tooltip
        EnergyDisplay = EnergyDisplay.setAll(backgroundX+9, backgroundY+16, 14, 42, mouseX, mouseY);
        if(EnergyDisplay.isMouseOver()){
            renderTooltip(matrices, Text.of(String.format("%s / %s RF", blockEntity.EnergyContainer.getStored(), blockEntity.EnergyContainer.getCapacity())), mouseX, mouseY);
        }

        //Augmentation Configuration
        int augmentsMenuX = backgroundX + backgroundWidth;
        int augmentsMenuY = backgroundY + 4 + 22;
        int augmentsMenuWidth = (int) Math.min(22 + AugmentsConfigIcon.wasMouseOverMillis() * 40, 100);
        int augmentsMenuHeight = (int) Math.min(22 + AugmentsConfigIcon.wasMouseOverMillis() * 40, 92);

        AugmentsConfigIcon = AugmentsConfigIcon.setAll(augmentsMenuX, augmentsMenuY, augmentsMenuWidth, augmentsMenuHeight, mouseX, mouseY);
        if(IoConfigIcon.wasMouseOverMillis() == 0 && (AugmentsConfigIcon.isMouseOver(delta, 2) || AugmentsConfigIcon.wasMouseOverMillis() > 0)){
            RenderSystem.setShaderTexture(0, AUGMENTATION_CONFIGURATION_TEXTURE);
            drawTexture(matrices, augmentsMenuX, augmentsMenuY, 0, 0, augmentsMenuWidth, augmentsMenuHeight);

            AugmentSlots.forEach(augmentSlot -> {
                int bgX = augmentSlot.bgX() + 4;
                int bgY = augmentSlot.bgY() + 4;
                boolean slotEnabled = augmentsMenuWidth >= bgX && augmentsMenuHeight >= bgY;

                if(handler.slots.get(augmentSlot.slotIndex()) instanceof ManagedSlot slot) slot.setEnabled(slotEnabled);

            });
        } else {
            RenderSystem.setShaderTexture(0, OPTIONS_TEXTURE);
            drawTexture(matrices, augmentsMenuX, augmentsMenuY, 0, 22, 22, 22);

            AugmentSlots.forEach(augmentSlot -> {
                if(handler.slots.get(augmentSlot.slotIndex()) instanceof ManagedSlot slot) slot.setEnabled(false);
            });
        }

        //Settings Configuration
        int settingsMenuX = backgroundX + backgroundWidth;
        int settingsMenuY = backgroundY + 4;
        int settingsMenuWidth = (int) Math.min(22 + IoConfigIcon.wasMouseOverMillis() * 40, 100);
        int settingsMenuHeight = (int) Math.min(22 + IoConfigIcon.wasMouseOverMillis() * 40, 92);

        IoConfigIcon = IoConfigIcon.setAll(settingsMenuX, settingsMenuY, settingsMenuWidth, settingsMenuHeight, mouseX, mouseY);
        if(AugmentsConfigIcon.wasMouseOverMillis() == 0 && (IoConfigIcon.isMouseOver(delta, 2) || IoConfigIcon.wasMouseOverMillis() > 0)){
            RenderSystem.setShaderTexture(0, OPTIONS_CONFIGURATION_TEXTURE);
            drawTexture(matrices, settingsMenuX, settingsMenuY, 0, 0, settingsMenuWidth, settingsMenuHeight);

            IoOptions.forEach(ioOption -> {
                int bgX = ioOption.bgX();
                int bgY = ioOption.bgY();
                Face face = ioOption.frontFace();
                IoState ioState = blockEntity.Configuration.getIoState(face);
                TexturePosition ioTexture = blockEntity.getIoTexturePosition(face);
                TexturePosition bgTexture = block.getTexturePosition(face, ioState != IoState.BLOCKED);

                if(settingsMenuWidth >= bgX && settingsMenuHeight >= bgY){
                    RenderSystem.setShaderTexture(0, block.getTextureId());
                    drawTexture(matrices,
                            settingsMenuX + bgX, settingsMenuY + bgY,
                            bgTexture.u(), bgTexture.v(),
                            Math.min(bgTexture.width(), settingsMenuWidth - bgX), Math.min(bgTexture.height(), settingsMenuHeight - bgY));
                }

                if(settingsMenuWidth >= bgX + 4 && settingsMenuHeight >= bgY + 4){
                    RenderSystem.setShaderTexture(0, blockEntity.getIoTextureId());
                    drawTexture(matrices,
                            settingsMenuX + bgX + 4, settingsMenuY + bgY + 4,
                            ioTexture.u(), ioTexture.v(),
                            Math.min(ioTexture.width(), settingsMenuWidth - (bgX + 4)), Math.min(ioTexture.height(), settingsMenuHeight - (bgY + 4)));

                }
            });

        } else {
            RenderSystem.setShaderTexture(0, OPTIONS_TEXTURE);
            drawTexture(matrices, settingsMenuX, settingsMenuY, 0, 0, 22, 22);
        }
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        super.drawForeground(matrices, mouseX, mouseY);
    }

    @Override
    protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
        int offsetX = 0;
        int offsetY = 7;

        int topLeftX = (width/2) - (this.backgroundWidth/2) - offsetX;
        int topLeftY = (height/2) - (this.backgroundHeight/2) - offsetY;

        int bottomRightX = (width/2) + (this.backgroundWidth/2) - offsetX;
        int bottomRightY = (height/2) + (this.backgroundHeight/2) - offsetY;

        return (mouseX < topLeftX || mouseX > bottomRightX) || (mouseY < topLeftY || mouseY > bottomRightY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        MinecraftClient MinecraftInstance = MinecraftClient.getInstance();
        if(MinecraftInstance.world == null) return false;

        QuarryBlockEntity blockEntity = (QuarryBlockEntity) MinecraftInstance.world.getBlockEntity(blockPos);
        if(blockEntity == null) return false;

        QuarryBlock block = blockEntity.getQuarryBlock();
        if(block == null) return false;

        if(this.client == null) return false;
        if(this.client.interactionManager == null) return false;
        if(MinecraftInstance.cameraEntity == null) return false;

        if(IoConfigIcon.wasMouseOverMillis() > 0) {
            IoOptions.forEach(ioOption -> {
                ScreenBackgroundButton ioButton = new ScreenBackgroundButton(IoConfigIcon.getButtonX() + ioOption.bgX(), IoConfigIcon.getButtonY() + ioOption.bgY(), 16, 16, (int) mouseX, (int) mouseY);
                if(ioButton.isMouseOver()) {
                    MinecraftInstance.cameraEntity.playSound(SoundEvents.UI_BUTTON_CLICK, 0.2F, 1.0F);

                    Optional<Byte> id = QuarryBlockScreenHandler.Buttons.toByte(0, ioOption.frontFace().getId(), button);
                    if(id.isEmpty()) return;

                    this.client.interactionManager.clickButton(handler.syncId, id.get());
                }
            });
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
//        if(this.handler.slots.get(31) instanceof ManagedSlot testSlot) testSlot.setEnabled(false);

        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void init() {
        super.init();
        // Center the title
        // titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
        // titleY = -6;
    }
}
