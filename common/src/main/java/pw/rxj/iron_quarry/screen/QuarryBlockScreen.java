package pw.rxj.iron_quarry.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;
import pw.rxj.iron_quarry.Global;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.block.QuarryBlock;
import pw.rxj.iron_quarry.blockentity.QuarryBlockEntity;
import pw.rxj.iron_quarry.compat.Compat;
import pw.rxj.iron_quarry.data.QuarryOwnerComponent;
import pw.rxj.iron_quarry.item.BlueprintItem;
import pw.rxj.iron_quarry.records.IoOption;
import pw.rxj.iron_quarry.records.TexturePosition;
import pw.rxj.iron_quarry.render.RenderUtil;
import pw.rxj.iron_quarry.types.Face;
import pw.rxj.iron_quarry.types.IoState;
import pw.rxj.iron_quarry.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QuarryBlockScreen extends AbstractContainerScreen<QuarryBlockScreenHandler> {
    //A path to the gui texture. In this example we use the texture from the dispenser
    public static final Identifier INVENTORY_LOCATION = Identifier.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/quarry_block_interface.png");
    public static final Identifier BACKGROUND_IO_OVERLAY = Identifier.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/quarry_block_io_overlay.png");
    public static final Identifier OPTIONS_TEXTURE = Identifier.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/options.png");
    public static final Identifier OPTIONS_CONFIGURATION_TEXTURE = Identifier.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/options_configuration.png");
    public static final Identifier AUGMENTATION_CONFIGURATION_TEXTURE = Identifier.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/augmentation_configuration.png");
    public static final Identifier BUTTON_SPRITE = Identifier.withDefaultNamespace("widget/button");
    public static final Identifier BUTTON_HIGHLIGHTED_SPRITE = Identifier.withDefaultNamespace("widget/button_highlighted");
    public static final Identifier BUTTON_DISABLED_SPRITE = Identifier.withDefaultNamespace("widget/button_disabled");

    private final TrackableZone InfoDisplay = TrackableZone.empty();
    private final TrackableZone EnergyDisplay = TrackableZone.empty();
    private final TrackableZone PreviousChunkButton = TrackableZone.empty();
    private final TrackableZone NextChunkButton = TrackableZone.empty();
    private final TrackableZone AugmentsConfig = TrackableZone.empty()
            .onTickDelta(2.5F, (zone, ticks) -> {
                zone.width = (int) Math.min(22 + ticks * 31.2F, 100);
                zone.height = (int) Math.min(22 + ticks * 28.0F, 92);
            });
    private final TrackableZone IoConfig = TrackableZone.empty()
            .onTickDelta(2.5F, (zone, ticks) -> {
                zone.width = (int) Math.min(22 + ticks * 31.2F, 100);
                zone.height = (int) Math.min(22 + ticks * 28.0F, 92);
            });

    private final int expandableMenuWidth;
    private final int realBackgroundWidth;
    private final int realBackgroundHeight;

    private final BlockPos blockPos;

    private final List<IoOption> IO_OPTIONS = List.of(
            new IoOption(Face.TOP, Face.TOP, 40, 24),
            new IoOption(Face.LEFT, Face.RIGHT, 20, 44),
            new IoOption(Face.FRONT, Face.FRONT, 40, 44),
            new IoOption(Face.RIGHT, Face.LEFT,60, 44),
            new IoOption(Face.BOTTOM, Face.BOTTOM, 40, 64),
            new IoOption(Face.BACK, Face.BACK, 60, 64)
    );
    private final List<ManagedSlot> AUGMENT_SLOTS = new ArrayList<>();
    private final ManagedSlot BLUEPRINT_SLOT;
    private final ManagedSlot DRILL_SLOT;
    private final ManagedSlot BATTERY_SLOT;

    private void drawLockedSlot(GuiGraphicsExtractor context, Slot slot, int width, int height) {
        if(width < 0) Main.LOGGER.warn("Tried to drawLockedSlot with width < 0 ({})", width);
        if(width > 18) Main.LOGGER.warn("Tried to drawLockedSlot with width > 18 ({})", width);
        if(height < 0) Main.LOGGER.warn("Tried to drawLockedSlot with height < 0 ({})", height);
        if(height > 18) Main.LOGGER.warn("Tried to drawLockedSlot with height > 18 ({})", height);

        RenderUtil.drawTexture(context, AUGMENTATION_CONFIGURATION_TEXTURE, this.leftPos + slot.x - 1, this.topPos + slot.y - 1, 100, 0, width, height);
    }

    public QuarryBlockScreen(QuarryBlockScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title, 176 + (Compat.REI_LOADED ? 0 : 100), 226);

        this.expandableMenuWidth = Compat.REI_LOADED ? 0 : 100;
        this.realBackgroundWidth = 176;
        this.realBackgroundHeight = 226;

        this.titleLabelY = 6;
        this.titleLabelX = 8;
        this.inventoryLabelY = 132;
        this.inventoryLabelX = 8;

        blockPos = handler.getBlockPos();

        for (int i = 0; i < 6; i++) AUGMENT_SLOTS.add((ManagedSlot) handler.getSlot(i));
        BLUEPRINT_SLOT = (ManagedSlot) handler.getSlot(6);
        DRILL_SLOT = (ManagedSlot) handler.getSlot(7);
        BATTERY_SLOT = (ManagedSlot) handler.getSlot(8);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractBackground(context, mouseX, mouseY, delta);

        if(this.minecraft.level == null) return;

        QuarryBlockEntity blockEntity = menu.getQuarryBlockEntity();
        if(blockEntity == null) return;

        QuarryBlock block = blockEntity.getQuarryBlock();
        if(block == null) return;

        ComplexEnergyContainer EnergyContainer = blockEntity.EnergyContainer;
        MachineConfiguration MachineConfiguration = blockEntity.Configuration;
        MachineUpgradesUtil upgradesUtil = MachineUpgradesUtil.from(blockEntity.getMachineUpgradeStacks());
        MachineWarningsUtil warningsUtil = MachineWarningsUtil.from(blockEntity);

        int backgroundX = this.leftPos;
        int backgroundY = this.topPos;

        RenderUtil.drawTexture(context, INVENTORY_LOCATION, backgroundX, backgroundY, 0, 0, this.realBackgroundWidth, this.realBackgroundHeight);

        //Energy-Fill-% rendering
        if(EnergyContainer.getStored() > 0) {
            int chargedPixels = Math.max(0, Math.min(40, (int) (EnergyContainer.getFillPercent() * 40)));
            RenderUtil.drawTexture(context, INVENTORY_LOCATION, backgroundX + 10, backgroundY + 58 - chargedPixels, 179, 57 - chargedPixels, 12, chargedPixels);
        }

        //Battery Slot rendering
        if(!BATTERY_SLOT.hasItem()){
            RenderUtil.drawTexture(context, INVENTORY_LOCATION, backgroundX + 11, backgroundY + 64, 180, 60, 10, 14);
        }

        //Energy-Fill-% tooltip
        EnergyDisplay.consume(TrackableZone.Zone.from(backgroundX + 9, backgroundY + 17, 14, 42), mouseX, mouseY);
        EnergyDisplay.supplyLine(() -> ReadableString.translatable("item.iron_quarry.lore.energy.capacity",
                ZUtil.expandableFixedInt(EnergyContainer.getStored()),
                ZUtil.expandableFixedInt(EnergyContainer.getCapacity())
        ).setStyle(Style.EMPTY.withColor(Global.RGB_RF_PURPLE)));

        //Info tooltip
        InfoDisplay.consume(TrackableZone.Zone.from(backgroundX + titleLabelX + this.font.width(this.title) - 1, backgroundY + titleLabelY/2, 13, 13), mouseX, mouseY);
        InfoDisplay.supplyText(() -> {
            List<Component> tooltip = new ArrayList<>();

            QuarryOwnerComponent owner = blockEntity.getOwner();
            tooltip.add(owner != null
                    ? ReadableString.translatable("screen.iron_quarry.quarry_block.title.info_display", owner.getName())
                    : ReadableString.translatable("screen.iron_quarry.quarry_block.title.info_display.no_owner"));
            upgradesUtil.appendStatsTooltip(block, tooltip::add);

            if(warningsUtil.hasWarnings()) {
                tooltip.add(ReadableString.space());
                warningsUtil.appendTooltip(tooltip::add);
            }

            return tooltip;
        });

        RenderUtil.drawTexture(context, INVENTORY_LOCATION, InfoDisplay.zone.x + 5, InfoDisplay.zone.y + 2, 179, 5, 3, 9);
        if(warningsUtil.hasWarnings()) {
            double alpha = Math.sin(System.currentTimeMillis() / 100.0) + 0.5;

            RenderUtil.drawTexture(context, INVENTORY_LOCATION, InfoDisplay.zone.x + 5, InfoDisplay.zone.y + 2, warningsUtil.hasCriticalWarnings() ? 185 : 182, 5, 3, 9, 256, 256, RenderUtil.whiteWithAlpha(alpha));
        }

        //Blueprint Buttons
        PreviousChunkButton.consume(TrackableZone.Zone.from(backgroundX + 58, backgroundY + 26, 18, 18), mouseX, mouseY);
        NextChunkButton.consume(TrackableZone.Zone.from(backgroundX + 100, backgroundY + 26, 18, 18), mouseX, mouseY);

        this.drawChunkButton(context, PreviousChunkButton, this.hasPreviousChunk(), 180, mouseX, mouseY);
        this.drawChunkButton(context, NextChunkButton, this.hasNextChunk(), 197, mouseX, mouseY);

        //Augmentation Configuration
        TrackableZone.Zone augmentsMenuZone = AugmentsConfig.zone;
        augmentsMenuZone.x = backgroundX + this.realBackgroundWidth;
        augmentsMenuZone.y = backgroundY + 26;

        AugmentsConfig.consume(augmentsMenuZone, mouseX, mouseY);

        if(IoConfig.isUnused() && AugmentsConfig.consumeTickDelta(delta)){
            int augmentsMenuX = AugmentsConfig.zone.x;
            int augmentsMenuY = AugmentsConfig.zone.y;
            int augmentsMenuWidth = AugmentsConfig.zone.width;
            int augmentsMenuHeight = AugmentsConfig.zone.height;

            RenderUtil.drawTexture(context, AUGMENTATION_CONFIGURATION_TEXTURE, augmentsMenuX, augmentsMenuY, 0, 0, augmentsMenuWidth, augmentsMenuHeight);

            RenderUtil.runScissored(context, augmentsMenuX, augmentsMenuY, augmentsMenuWidth, augmentsMenuHeight, () -> {
                context.text(this.font, ReadableString.translatable("screen.iron_quarry.quarry_block.title.augmentation"), augmentsMenuX + 20, augmentsMenuY + 7, 0xFFFFFFFF);
            });

            for (ManagedSlot slot: AUGMENT_SLOTS) {
                int testX = augmentsMenuWidth + this.realBackgroundWidth;
                int testY = augmentsMenuHeight + 26;

                boolean slotVisible = (testX >= slot.x - 1) && (testY >= slot.y - 1);
                slot.setEnabled(slotVisible);

                if(slot.isLocked() && slotVisible) {
                    this.drawLockedSlot(context, slot, Math.min(testX - slot.x + 1, 18), Math.min(testY - slot.y + 1, 18));
                }
            }

        } else {
            RenderUtil.drawTexture(context, OPTIONS_TEXTURE, augmentsMenuZone.x, augmentsMenuZone.y, 0, 22, 22, 22);

            AUGMENT_SLOTS.forEach(slot -> slot.setEnabled(false));
        }

        //Io Configuration
        TrackableZone.Zone ioConfigZone = IoConfig.zone;
        ioConfigZone.x = backgroundX + this.realBackgroundWidth;
        ioConfigZone.y = backgroundY + 4;

        IoConfig.consume(ioConfigZone, mouseX, mouseY);

        if(AugmentsConfig.isUnused() && IoConfig.consumeTickDelta(delta)){
            int ioConfigX = IoConfig.zone.x;
            int ioConfigY = IoConfig.zone.y;
            int ioConfigWidth = IoConfig.zone.width;
            int ioConfigHeight = IoConfig.zone.height;

            //Io Overlay
            float cappedMaxTicks = IoConfig.getMaxTicks() / 3;
            if(IoConfig.getTicks() > cappedMaxTicks) {
                float cappedTicks = IoConfig.getTicks() - cappedMaxTicks;

                int overlayColor = RenderUtil.whiteWithAlpha(cappedTicks / cappedMaxTicks);

                RenderUtil.drawTexture(context, BACKGROUND_IO_OVERLAY, backgroundX, backgroundY, 0, 0, this.realBackgroundWidth, this.realBackgroundHeight, 256, 256, overlayColor);

                //Battery Slot rendering
                if(!BATTERY_SLOT.hasItem()){
                    RenderUtil.drawTexture(context, BACKGROUND_IO_OVERLAY, backgroundX + 11, backgroundY + 64, 180, 60, 10, 14, 256, 256, overlayColor);
                }
            }

            RenderUtil.drawTexture(context, OPTIONS_CONFIGURATION_TEXTURE, ioConfigX, ioConfigY, 0, 0, ioConfigWidth, ioConfigHeight);

            RenderUtil.runScissored(context, ioConfigX, ioConfigY, ioConfigWidth, ioConfigHeight, () -> {
                context.text(this.font, ReadableString.translatable("screen.iron_quarry.quarry_block.title.configuration"), ioConfigX + 20, ioConfigY + 7, 0xFFFFFFFF);
            });

            IO_OPTIONS.forEach(ioOption -> {
                int bgX = ioOption.bgX();
                int bgY = ioOption.bgY();
                Face face = ioOption.frontFace();
                IoState ioState = MachineConfiguration.getIoState(face);
                TexturePosition bgTexture = block.getTexturePosition(face, ioState != IoState.BLOCKED);
                TexturePosition ioTexture = IoState.getTexturePosition(MachineConfiguration.getIoState(face));

                if(ioConfigWidth >= bgX && ioConfigHeight >= bgY){
                    RenderUtil.drawTexture(context, block.getTextureId(),
                            ioConfigX + bgX, ioConfigY + bgY,
                            bgTexture.u(), bgTexture.v(),
                            Math.min(bgTexture.width(), ioConfigWidth - bgX), Math.min(bgTexture.height(), ioConfigHeight - bgY));
                }

                if(ioConfigWidth >= bgX + 4 && ioConfigHeight >= bgY + 4){
                    RenderUtil.drawTexture(context, IoState.getTextureId(),
                            ioConfigX + bgX + 4, ioConfigY + bgY + 4,
                            ioTexture.u(), ioTexture.v(),
                            Math.min(ioTexture.width(), ioConfigWidth - (bgX + 4)), Math.min(ioTexture.height(), ioConfigHeight - (bgY + 4)));

                }

                if(TrackableZone.isMouseOver(ioConfigX + bgX, ioConfigY + bgY, bgTexture.width(), bgTexture.height(), mouseX, mouseY)) {
                    drawIoHighlight(context, ioConfigX + bgX, ioConfigY + bgY);
                }
            });

        } else {
            RenderUtil.drawTexture(context, OPTIONS_TEXTURE, ioConfigZone.x, ioConfigZone.y, 0, 0, 22, 22);
        }
    }

    @Override
    protected void extractSlot(GuiGraphicsExtractor context, Slot slot, int mouseX, int mouseY) {
        if(AUGMENT_SLOTS.contains((ManagedSlot) slot)) {
            TrackableZone.Zone zone = AugmentsConfig.zone;
            RenderUtil.runScissored(context, zone.x - this.leftPos, zone.y - this.topPos, zone.width, zone.height, () -> {
                super.extractSlot(context, slot, mouseX, mouseY);
            });
        } else {
            super.extractSlot(context, slot, mouseX, mouseY);
        }
    }

    public List<TrackableZone.Zone> getExclusionZones(){
        return List.of(IoConfig.zone, AugmentsConfig.zone);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        super.extractLabels(context, mouseX, mouseY);

        if(!this.menu.getCarried().isEmpty()) return;

        Matrix3x2fStack matrices = context.pose();
        matrices.pushMatrix();
        matrices.translate(-this.leftPos, -this.topPos);

        if(EnergyDisplay.isMouseOver(mouseX, mouseY)) {
            context.setComponentTooltipForNextFrame(this.font, EnergyDisplay.getSuppliedText(), mouseX, mouseY);
        } else if(InfoDisplay.isMouseOver(mouseX, mouseY)) {
            context.setComponentTooltipForNextFrame(this.font, InfoDisplay.getSuppliedText(), mouseX, mouseY);
        } else if(BLUEPRINT_SLOT.getItem().isEmpty() && TrackableZone.isMouseOver(BLUEPRINT_SLOT, this.leftPos, this.topPos, mouseX, mouseY)) {
            context.setTooltipForNextFrame(this.font, ReadableString.translatable("screen.iron_quarry.quarry_block.tooltip.blueprint_info"), mouseX, mouseY);
        } else if(DRILL_SLOT.getItem().isEmpty() && TrackableZone.isMouseOver(DRILL_SLOT, this.leftPos, this.topPos, mouseX, mouseY)) {
            context.setTooltipForNextFrame(this.font, ReadableString.translatable("screen.iron_quarry.quarry_block.tooltip.drill_info"), mouseX, mouseY);
        } else if(BATTERY_SLOT.getItem().isEmpty() && TrackableZone.isMouseOver(BATTERY_SLOT, this.leftPos, this.topPos, mouseX, mouseY)) {
            context.setTooltipForNextFrame(this.font, ReadableString.translatable("screen.iron_quarry.quarry_block.tooltip.battery_info"), mouseX, mouseY);
        } else if(PreviousChunkButton.isMouseOver(mouseX, mouseY)) {
            context.setTooltipForNextFrame(this.font, ReadableString.translatable(this.hasPreviousChunk() ? "screen.iron_quarry.quarry_block.tooltip.previous_chunk" : "screen.iron_quarry.quarry_block.tooltip.previous_chunk.disabled"), mouseX, mouseY);
        } else if(NextChunkButton.isMouseOver(mouseX, mouseY)) {
            context.setTooltipForNextFrame(this.font, ReadableString.translatable(this.hasNextChunk() ? "screen.iron_quarry.quarry_block.tooltip.next_chunk" : "screen.iron_quarry.quarry_block.tooltip.next_chunk.disabled"), mouseX, mouseY);
        } else if(AugmentsConfig.isMouseOver()) {
            for (ManagedSlot slot : AUGMENT_SLOTS) {
                if(slot.isLocked() && TrackableZone.isMouseOver(slot, this.leftPos, this.topPos, mouseX, mouseY)) {
                    context.setTooltipForNextFrame(this.font, ReadableString.translatable("screen.iron_quarry.quarry_block.tooltip.locked_augment_slot"), mouseX, mouseY);

                    break;
                }
            }
        }

        matrices.popMatrix();
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top) {
        if(IoConfig.isMouseOver() || AugmentsConfig.isMouseOver()) return false;

        int offsetX = 0;
        int offsetY = 0;

        int topLeftX = (width/2) - (this.realBackgroundWidth/2) - offsetX;
        int topLeftY = (height/2) - (this.realBackgroundHeight/2) - offsetY;

        int bottomRightX = (width/2) + (this.realBackgroundWidth/2) - offsetX;
        int bottomRightY = (height/2) + (this.realBackgroundHeight/2) - offsetY;

        return (mouseX < topLeftX || mouseX > bottomRightX) || (mouseY < topLeftY || mouseY > bottomRightY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        if(this.minecraft.level == null) return false;

        QuarryBlockEntity blockEntity = menu.getQuarryBlockEntity();
        if(blockEntity == null) return false;

        QuarryBlock block = blockEntity.getQuarryBlock();
        if(block == null) return false;

        if(this.minecraft.gameMode == null) return false;
        if(this.minecraft.getCameraEntity() == null) return false;

        if(button == 0 && PreviousChunkButton.isMouseOver((int) mouseX, (int) mouseY)) {
            if(this.hasPreviousChunk()) this.pressChunkButton(0);

            return true;
        }
        if(button == 0 && NextChunkButton.isMouseOver((int) mouseX, (int) mouseY)) {
            if(this.hasNextChunk()) this.pressChunkButton(1);

            return true;
        }

        if(IoConfig.isUsed()) {
            IO_OPTIONS.forEach(ioOption -> {
                TrackableZone.Zone ioButtonZone = TrackableZone.Zone.from(
                        IoConfig.zone.x + ioOption.bgX(),
                        IoConfig.zone.y + ioOption.bgY(),
                        16,
                        16
                );

                TrackableZone ioButton = TrackableZone.bake(ioButtonZone, (int) mouseX, (int) mouseY);

                if(ioButton.isMouseOver()) {
                    this.minecraft.getCameraEntity().playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.2F, 1.0F);

                    Optional<Byte> id = QuarryBlockScreenHandler.Buttons.toByte(0, ioOption.frontFace().getId(), button);
                    if(id.isEmpty()) return;

                    this.minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id.get());
                }
            });
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.realBackgroundWidth) / 2;
        this.topPos = (this.height - this.realBackgroundHeight) / 2;
    }

    private boolean hasPreviousChunk() {
        ItemStack blueprintStack = BLUEPRINT_SLOT.getItem();

        return blueprintStack.getItem() instanceof BlueprintItem blueprintItem && blueprintItem.hasPreviousChunk(blueprintStack);
    }
    private boolean hasNextChunk() {
        ItemStack blueprintStack = BLUEPRINT_SLOT.getItem();

        return blueprintStack.getItem() instanceof BlueprintItem blueprintItem && blueprintItem.hasNextChunk(blueprintStack);
    }
    private void drawChunkButton(GuiGraphicsExtractor context, TrackableZone chunkButton, boolean active, int iconU, int mouseX, int mouseY) {
        Identifier sprite = !active ? BUTTON_DISABLED_SPRITE : chunkButton.isMouseOver(mouseX, mouseY) ? BUTTON_HIGHLIGHTED_SPRITE : BUTTON_SPRITE;

        RenderUtil.drawSprite(context, sprite, chunkButton.zone.x, chunkButton.zone.y, 18, 18);
        RenderUtil.drawTexture(context, INVENTORY_LOCATION, chunkButton.zone.x + 1, chunkButton.zone.y + 1, iconU, active ? 78 : 95, 16, 16);
    }
    private void pressChunkButton(int directionId) {
        if(this.minecraft.gameMode == null || this.minecraft.getCameraEntity() == null) return;

        this.minecraft.getCameraEntity().playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.2F, 1.0F);

        Optional<Byte> buttonId = QuarryBlockScreenHandler.Buttons.toByte(1, directionId, 0);
        buttonId.ifPresent(id -> this.minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id));
    }

    public static void drawIoHighlight(GuiGraphicsExtractor context, int x, int y) {
        context.fillGradient(x, y, x + 16, y + 16, 1612718112, 1612718112);
    }
}
