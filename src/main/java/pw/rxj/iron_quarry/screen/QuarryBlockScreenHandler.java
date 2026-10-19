package pw.rxj.iron_quarry.screen;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.block.QuarryBlock;
import pw.rxj.iron_quarry.blockentity.QuarryBlockEntity;
import pw.rxj.iron_quarry.data.QuarryBlockScreenData;
import pw.rxj.iron_quarry.types.Face;
import pw.rxj.iron_quarry.util.*;

public class QuarryBlockScreenHandler extends ScreenHandler {
    public static final SingleByteMap Buttons = new SingleByteMap().with(6, 2);

    private final MachineConfiguration Configuration;
    private final ComplexInventory DrillInventory;
    private final ComplexInventory OutputInventory;
    private final ComplexInventory BlueprintInventory;
    private final ComplexInventory BatteryInputInventory;
    private final ComplexEnergyContainer EnergyContainer;
    private final ComplexInventory MachineUpgradesInventory;
    private QuarryBlock quarryBlock;
    private BlockPos blockPos;
    private World world;

    //This constructor gets called on the client when the server wants it to open the screenHandler,
    //The client will call the other constructor with an empty Inventory and the screenHandler will automatically
    //sync this empty inventory with the inventory on the server.
    public QuarryBlockScreenHandler(int syncId, PlayerInventory playerInventory, QuarryBlockScreenData data) {
        this(syncId, playerInventory, QuarryInventory.Output.noRef(), QuarryInventory.BatteryInput.noRef(), QuarryInventory.MachineUpgrades.noRef(),
                QuarryInventory.Blueprint.noRef(), QuarryInventory.Drill.noRef(), new ComplexEnergyContainer(), MachineConfiguration.empty(), QuarryBlock.getFallback());

        this.quarryBlock = (QuarryBlock) data.getBlock();
        this.blockPos = data.getBlockPos();
        this.world = playerInventory.player.getWorld();
    }

    //This constructor gets called from the BlockEntity on the server without calling the other constructor first, the server knows the inventory of the container
    //and can therefore directly provide it as an argument. This inventory will then be synced to the client.
    public QuarryBlockScreenHandler(int syncId, PlayerInventory playerInventory, ComplexInventory outputInventory, ComplexInventory batteryInputInventory, ComplexInventory machineUpgradesInventory,
                                                ComplexInventory blueprintInventory, ComplexInventory drillInventory, ComplexEnergyContainer energyContainer, MachineConfiguration configuration, QuarryBlock quarryBlock) {
        super(Main.QUARRY_BLOCK_SCREEN_HANDLER, syncId);

        this.quarryBlock = quarryBlock;
        this.blockPos = BlockPos.ORIGIN;
        this.world = playerInventory.player.getWorld();

        checkSize(machineUpgradesInventory, 6);
        checkSize(blueprintInventory, 1);
        checkSize(drillInventory, 1);
        checkSize(batteryInputInventory, 1);
        checkSize(outputInventory, 18);

        this.MachineUpgradesInventory = machineUpgradesInventory;
        this.BlueprintInventory = blueprintInventory;
        this.DrillInventory = drillInventory;
        this.BatteryInputInventory = batteryInputInventory;
        this.OutputInventory = outputInventory;

        this.EnergyContainer = energyContainer;
        this.Configuration = configuration;

        //Some inventories do custom logic when a player opens it
        machineUpgradesInventory.onOpen(playerInventory.player);
        blueprintInventory.onOpen(playerInventory.player);
        drillInventory.onOpen(playerInventory.player);
        batteryInputInventory.onOpen(playerInventory.player);
        outputInventory.onOpen(playerInventory.player);

        final int SLOT_SIZE = 18;

        //Machine Upgrades Inventory
        for (var row = 0; row < 2; ++row) {
            for (var slot = 0; slot < 3; ++slot) {
                this.addSlot(new ManagedSlot(machineUpgradesInventory, (row * 3) + slot, 199 + slot * SLOT_SIZE, 52 + row * SLOT_SIZE) {
                    @Override
                    public boolean isLocked() {
                        return super.isLocked() || this.getIndex() >= QuarryBlockScreenHandler.this.quarryBlock.getAugmentLimit();
                    }
                });
            }
        }

        //Blueprint Inventory
        this.addSlot(new ManagedSlot(blueprintInventory, 0, 80, 27));

        //Drill Inventory
        this.addSlot(new ManagedSlot(drillInventory, 0, 80, 48));

        //Reborn Inventory
        this.addSlot(new ManagedSlot(batteryInputInventory, 0, 8, 63));

        //Output Inventory
        for (var row = 0; row < 2; ++row) {
            for (var slot = 0; slot < 9; ++slot) {
                this.addSlot(new ManagedSlot(outputInventory, (row * 9) + slot, 8 + slot * SLOT_SIZE, 94 + row * SLOT_SIZE));
            }
        }

        //Player Inventory
        for (var row = 0; row < 3; ++row) {
            for (var slot = 0; slot < 9; ++slot) {
                this.addSlot(new ManagedSlot(playerInventory, (row * 9) + slot + 9, 8 + slot * SLOT_SIZE, 143 + row * SLOT_SIZE));
            }
        }

        //Player Hotbar
        for (var slot = 0; slot < 9; ++slot) {
            this.addSlot(new ManagedSlot(playerInventory, slot, 8 + slot * SLOT_SIZE, 201));
        }
    }

    //This getter will be used by our Screen class
    public BlockPos getBlockPos() {
        return this.blockPos;
    }
    public QuarryBlock getQuarryBlock() {
        return this.quarryBlock;
    }
    public @Nullable World getWorld() {
        return this.world;
    }
    public @Nullable QuarryBlockEntity getQuarryBlockEntity() {
        World world = this.getWorld();
        if(world == null) return null;

        BlockEntity blockEntity = world.getBlockEntity(this.getBlockPos());
        return blockEntity instanceof QuarryBlockEntity quarryBlockEntity ? quarryBlockEntity : null;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.OutputInventory.canPlayerUse(player);
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        SingleByteMap.ByteMapItem item = Buttons.get((byte) id);
        if(item == null) return false;

        int type = item.id();
        if(type == 0) {
            int faceId = item.first();
            if(faceId > 5) return false;
            int buttonId = item.second();
            if(buttonId > 1) return false;

            Face face = Face.from(faceId);
            Configuration.setIoState(face, buttonId == 0 ? this.Configuration.getNextIoState(face) : this.Configuration.getPreviousIoState(face));
        }

        return true;
    }

    //Shift + Player Inv Slot
    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        int maxInvSize = this.DrillInventory.size() + this.OutputInventory.size() + this.BlueprintInventory.size() + this.BatteryInputInventory.size() + this.MachineUpgradesInventory.size();

        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < maxInvSize) {
                if (!this.insertItem(originalStack, maxInvSize, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, maxInvSize, false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return newStack;
    }
}
