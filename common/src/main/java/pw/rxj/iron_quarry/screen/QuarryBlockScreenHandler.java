package pw.rxj.iron_quarry.screen;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.block.QuarryBlock;
import pw.rxj.iron_quarry.blockentity.QuarryBlockEntity;
import pw.rxj.iron_quarry.data.QuarryBlockScreenData;
import pw.rxj.iron_quarry.network.PacketQuarryMenuEnergy;
import pw.rxj.iron_quarry.network.ZNetwork;
import pw.rxj.iron_quarry.types.Face;
import pw.rxj.iron_quarry.util.*;

import java.util.ArrayList;
import java.util.List;

public class QuarryBlockScreenHandler extends AbstractContainerMenu {
    public static final SingleByteMap Buttons = new SingleByteMap().with(6, 2).with(2, 1);

    private final MachineConfiguration Configuration;
    private final ComplexInventory DrillInventory;
    private final ComplexInventory OutputInventory;
    private final ComplexInventory BlueprintInventory;
    private final ComplexInventory BatteryInputInventory;
    private final ComplexEnergyContainer EnergyContainer;
    private final ComplexInventory MachineUpgradesInventory;

    private final Player player;
    private final QuarryBlock quarryBlock;
    private final BlockPos blockPos;
    private final Level world;

    private long lastSentEnergy = -1;

    //This constructor gets called on the client when the server wants it to open the screenHandler,
    //The client will call the other constructor with an empty Inventory and the screenHandler will automatically
    //sync this empty inventory with the inventory on the server.
    public QuarryBlockScreenHandler(int syncId, Inventory playerInventory, QuarryBlockScreenData data) {
        this(syncId, playerInventory, QuarryInventory.Output.noRef(), QuarryInventory.BatteryInput.noRef(), QuarryInventory.MachineUpgrades.noRef(),
                QuarryInventory.Blueprint.noRef(), QuarryInventory.Drill.noRef(), ComplexEnergyContainer.ofCapacity(data.getQuarryBlock().getEnergyCapacity()), MachineConfiguration.empty(), data.getQuarryBlock(), data.getBlockPos());
    }

    //This constructor gets called from the BlockEntity on the server without calling the other constructor first, the server knows the inventory of the container
    //and can therefore directly provide it as an argument. This inventory will then be synced to the client.
    public QuarryBlockScreenHandler(int syncId, Inventory playerInventory, ComplexInventory outputInventory, ComplexInventory batteryInputInventory, ComplexInventory machineUpgradesInventory,
                                                ComplexInventory blueprintInventory, ComplexInventory drillInventory, ComplexEnergyContainer energyContainer, MachineConfiguration configuration, QuarryBlock quarryBlock, BlockPos blockPos) {
        super(Main.QUARRY_BLOCK_SCREEN_HANDLER, syncId);

        this.player = playerInventory.player;
        this.quarryBlock = quarryBlock;
        this.blockPos = blockPos;
        this.world = playerInventory.player.level();

        checkContainerSize(machineUpgradesInventory, 6);
        checkContainerSize(blueprintInventory, 1);
        checkContainerSize(drillInventory, 1);
        checkContainerSize(batteryInputInventory, 1);
        checkContainerSize(outputInventory, 18);

        this.MachineUpgradesInventory = machineUpgradesInventory;
        this.BlueprintInventory = blueprintInventory;
        this.DrillInventory = drillInventory;
        this.BatteryInputInventory = batteryInputInventory;
        this.OutputInventory = outputInventory;

        this.EnergyContainer = energyContainer;
        this.Configuration = configuration;

        //Some inventories do custom logic when a player opens it
        machineUpgradesInventory.startOpen(playerInventory.player);
        blueprintInventory.startOpen(playerInventory.player);
        drillInventory.startOpen(playerInventory.player);
        batteryInputInventory.startOpen(playerInventory.player);
        outputInventory.startOpen(playerInventory.player);

        final int SLOT_SIZE = 18;

        //Machine Upgrades Inventory
        for (var row = 0; row < 2; ++row) {
            for (var slot = 0; slot < 3; ++slot) {
                this.addSlot(new ManagedSlot(machineUpgradesInventory, (row * 3) + slot, 199 + slot * SLOT_SIZE, 52 + row * SLOT_SIZE) {
                    @Override
                    public boolean isLocked() {
                        return super.isLocked() || this.getContainerSlot() >= QuarryBlockScreenHandler.this.quarryBlock.getAugmentLimit();
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

    @Override
    public void sendAllDataToRemote() {
        super.sendAllDataToRemote();

        this.sendEnergyToRemote();
    }
    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        if(this.EnergyContainer.getStored() != this.lastSentEnergy) this.sendEnergyToRemote();
    }
    private void sendEnergyToRemote() {
        if(!(this.player instanceof ServerPlayer serverPlayer)) return;

        this.lastSentEnergy = this.EnergyContainer.getStored();
        ZNetwork.sendToPlayer(serverPlayer, PacketQuarryMenuEnergy.bake(this.containerId, this.lastSentEnergy));
    }

    public ComplexEnergyContainer getEnergyContainer() {
        return this.EnergyContainer;
    }
    public ComplexInventory getOutputInventory() {
        return this.OutputInventory;
    }
    public ComplexInventory getBlueprintInventory() {
        return this.BlueprintInventory;
    }
    public ComplexInventory getDrillInventory() {
        return this.DrillInventory;
    }
    public List<ItemStack> getMachineUpgradeStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.addAll(this.MachineUpgradesInventory.items);
        stacks.addAll(this.DrillInventory.items);

        return stacks;
    }

    //This getter will be used by our Screen class
    public BlockPos getBlockPos() {
        return this.blockPos;
    }
    public QuarryBlock getQuarryBlock() {
        return this.quarryBlock;
    }
    public @Nullable Level getWorld() {
        return this.world;
    }
    public @Nullable QuarryBlockEntity getQuarryBlockEntity() {
        Level world = this.getWorld();
        if(world == null) return null;

        BlockEntity blockEntity = world.getBlockEntity(this.getBlockPos());
        return blockEntity instanceof QuarryBlockEntity quarryBlockEntity ? quarryBlockEntity : null;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.OutputInventory.stillValid(player);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
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
        } else if(type == 1) {
            int directionId = item.first();
            if(directionId > 1) return false;

            QuarryBlockEntity quarryBlockEntity = this.getQuarryBlockEntity();
            if(quarryBlockEntity == null) return false;

            if(directionId == 0) {
                quarryBlockEntity.previousChunk();
            } else {
                quarryBlockEntity.nextChunk();
            }
        }

        return true;
    }

    //Shift + Player Inv Slot
    @Override
    public ItemStack quickMoveStack(Player player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        int maxInvSize = this.DrillInventory.getContainerSize() + this.OutputInventory.getContainerSize() + this.BlueprintInventory.getContainerSize() + this.BatteryInputInventory.getContainerSize() + this.MachineUpgradesInventory.getContainerSize();

        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasItem()) {
            ItemStack originalStack = slot.getItem();
            newStack = originalStack.copy();
            if (invSlot < maxInvSize) {
                if (!this.moveItemStackTo(originalStack, maxInvSize, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(originalStack, 0, maxInvSize, false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return newStack;
    }
}
