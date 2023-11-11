package pw.rxj.iron_quarry.screenhandler;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.items.AugmentItem;
import pw.rxj.iron_quarry.items.BlueprintItem;
import pw.rxj.iron_quarry.types.Face;
import pw.rxj.iron_quarry.util.*;
import team.reborn.energy.api.EnergyStorageUtil;

public class QuarryBlockScreenHandler extends ScreenHandler {
    public static final SingleByteMap Buttons = new SingleByteMap().with(6, 2);

    private final MachineConfiguration Configuration;
    private final ComplexInventory OutputInventory;
    private final ComplexInventory BlueprintInventory;
    private final ComplexInventory BatteryInputInventory;
    private final ComplexEnergyContainer EnergyContainer;
    private final ComplexInventory MachineUpgradesInventory;
    private BlockPos blockPos;

    //This constructor gets called on the client when the server wants it to open the screenHandler,
    //The client will call the other constructor with an empty Inventory and the screenHandler will automatically
    //sync this empty inventory with the inventory on the server.
    public QuarryBlockScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buffer) {
        this(syncId, playerInventory, new ComplexInventory(18), new ComplexInventory(1), new ComplexInventory(6), new ComplexInventory(1), new ComplexEnergyContainer(), new MachineConfiguration());

        blockPos = buffer.readBlockPos();
    }

    //This constructor gets called from the BlockEntity on the server without calling the other constructor first, the server knows the inventory of the container
    //and can therefore directly provide it as an argument. This inventory will then be synced to the client.
    public QuarryBlockScreenHandler(int syncId, PlayerInventory playerInventory, ComplexInventory outputInventory, ComplexInventory batteryInputInventory, ComplexInventory machineUpgradesInventory,
                                                ComplexInventory blueprintInventory, ComplexEnergyContainer energyContainer, MachineConfiguration configuration) {
        super(Main.QUARRY_BLOCK_SCREEN_HANDLER, syncId);

        blockPos = BlockPos.ORIGIN;

        checkSize(machineUpgradesInventory, 6);
        checkSize(batteryInputInventory, 1);
        checkSize(blueprintInventory, 1);
        checkSize(outputInventory, 18);

        this.MachineUpgradesInventory = machineUpgradesInventory;
        this.BatteryInputInventory = batteryInputInventory;
        this.BlueprintInventory = blueprintInventory;
        this.OutputInventory = outputInventory;
        this.EnergyContainer = energyContainer;
        this.Configuration = configuration;

        //Some inventories do custom logic when a player opens it
        batteryInputInventory.onOpen(playerInventory.player);
        outputInventory.onOpen(playerInventory.player);

        final int SLOT_SIZE = 18;

        //Machine Upgrades Inventory
        for (var row = 0; row < 2; ++row) {
            for (var slot = 0; slot < 3; ++slot) {
                this.addSlot(new ManagedSlot(machineUpgradesInventory, (row * 3) + slot, 199 + slot * SLOT_SIZE, 52 + row * SLOT_SIZE) {
                    @Override
                    public boolean canInsert(ItemStack stack) {
                        return stack.getItem() instanceof AugmentItem;
                    }
                });
            }
        }

        //Blueprint Inventory
        this.addSlot(new ManagedSlot(blueprintInventory, 0, 80, 38) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.getItem() instanceof BlueprintItem;
            }
        });


        //Reborn Inventory
        this.addSlot(new ManagedSlot(batteryInputInventory, 0, 8, 62) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return EnergyStorageUtil.isEnergyStorage(stack);
            }
        });

        //Output Inventory
        for (var row = 0; row < 2; ++row) {
            for (var slot = 0; slot < 9; ++slot) {
                this.addSlot(new ManagedSlot(outputInventory, (row * 9) + slot, 8 + slot * SLOT_SIZE, 93 + row * SLOT_SIZE));
            }
        }

        //Player Inventory
        for (var row = 0; row < 3; ++row) {
            for (var slot = 0; slot < 9; ++slot) {
                this.addSlot(new ManagedSlot(playerInventory, (row * 9) + slot + 9, 8 + slot * SLOT_SIZE, 142 + row * SLOT_SIZE));
            }
        }

        //Player Hotbar
        for (var slot = 0; slot < 9; ++slot) {
            this.addSlot(new ManagedSlot(playerInventory, slot, 8 + slot * SLOT_SIZE, 200));
        }
    }

    //This getter will be used by our Screen class
    public BlockPos getPos() {
        return blockPos;
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
    public ItemStack transferSlot(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        int maxInvSize = this.OutputInventory.size() + this.BatteryInputInventory.size() + this.MachineUpgradesInventory.size() + this.BlueprintInventory.size();

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
