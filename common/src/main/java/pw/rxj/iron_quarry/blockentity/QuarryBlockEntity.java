package pw.rxj.iron_quarry.blockentity;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.block.QuarryBlock;
import pw.rxj.iron_quarry.block.ZBlockTags;
import pw.rxj.iron_quarry.data.QuarryBlockScreenData;
import pw.rxj.iron_quarry.data.QuarryBlockedDataComponent;
import pw.rxj.iron_quarry.data.QuarryOwnerComponent;
import pw.rxj.iron_quarry.item.BlueprintItem;
import pw.rxj.iron_quarry.platform.Platform;
import pw.rxj.iron_quarry.screen.QuarryBlockScreenHandler;
import pw.rxj.iron_quarry.types.Face;
import pw.rxj.iron_quarry.types.IoState;
import pw.rxj.iron_quarry.util.*;

import java.util.*;

public class QuarryBlockEntity extends BlockEntity implements MenuProvider {
    public QuarryBlockEntity(BlockPos pos, BlockState state) {
        super(ZBlockEntities.QUARRY_BLOCK_ENTITY, pos, state);
    }

    private Block getBlock() {
        if(this.level == null) return null;

        return this.level.getBlockState(this.worldPosition).getBlock();
    }

    public QuarryBlock getQuarryBlock(){
        Block block = this.getBlock();
        if(block == null) return null;

        if(block instanceof QuarryBlock quarryBlock) {
            return quarryBlock;
        } else {
            return null;
        }
    }

    public boolean canPlayerUse(Player player) {
        if (this.level != null && this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        } else {
            return player.distanceToSqr((double)this.worldPosition.getX() + 0.5, (double)this.worldPosition.getY() + 0.5, (double)this.worldPosition.getZ() + 0.5) <= 64.0;
        }
    }

    //Configuration
    public MachineConfiguration Configuration = new MachineConfiguration() {
        @Override
        public void markDirty() {
            QuarryBlockEntity.this.setChanged();
        }

        @Override
        public void onIoUpdated(Face face, IoState ioState) {
            QuarryBlockEntity.this.updateListeners();
            Platform.ITEM_TRANSFER.notifyLinkedContainersChanged(QuarryBlockEntity.this);
        }

        private @Nullable IndexedMap<IoState, Object> usedIoStates;

        @Override
        public IndexedMap<IoState, Object> getUsedIoStates() {
            if(this.usedIoStates == null) {
                IndexedMap<IoState, Object> usedIo = new IndexedMap<>();
                usedIo.put(IoState.BLOCKED, null);
                usedIo.put(IoState.ORANGE, OutputInventory);
                usedIo.put(IoState.RED, DrillInventory);
                usedIo.put(IoState.PURPLE, BatteryInputInventory);

                this.usedIoStates = usedIo;
            }

            return this.usedIoStates;
        }
    };

    //Energy
    public final ComplexEnergyContainer EnergyContainer = QuarryEnergyContainer.ofRef(this);

    public void attemptCharge() {
        if(level == null) return;
        if(level.isClientSide()) return;

        long chargeEnergy = Math.min(EnergyContainer.getFreeSpace(), EnergyContainer.getMaxInsert(null));
        if(chargeEnergy <= 0) return;

        ItemStack batteryStack = BatteryInputInventory.getItem(0);
        if(batteryStack.isEmpty()) return;

        if(!Platform.ENERGY.chargeFromSlot(BatteryInputInventory, 0, EnergyContainer) && OutputInventory.canAddItem(batteryStack)) {
            OutputInventory.addItem(BatteryInputInventory.removeItemNoUpdate(0));
        }


    }

    //Inventories
    public final ComplexInventory OutputInventory = QuarryInventory.Output.ofRef(this);
    public final ComplexInventory BatteryInputInventory = QuarryInventory.BatteryInput.ofRef(this);
    public final ComplexInventory MachineUpgradesInventory = QuarryInventory.MachineUpgrades.ofRef(this);
    public final ComplexInventory ExpansionsInventory = QuarryInventory.Expansions.ofRef(this);
    public final ComplexInventory BlueprintInventory = QuarryInventory.Blueprint.ofRef(this);
    public final ComplexInventory DrillInventory = QuarryInventory.Drill.ofRef(this);

    public @Nullable Container getLinkedContainer(Direction direction){
        if(direction == null) return null;

        Face face = Face.from(direction, this.getBlockState().getValue(QuarryBlock.FACING));
        Object object = this.Configuration.getLinkedIo(face);

        if(object instanceof Container inventory) {
            return inventory;
        }

        return null;
    }

    public void attemptPushIo(IoState ioState){
        for (Face face: Face.values()) {
            if(!this.Configuration.getIoState(face).equals(ioState)) continue;

            Object object = this.Configuration.getLinkedIo(face);
            if(!(object instanceof Container inventory)) continue;

            Direction direction = face.toDirection(this.getBlockState().getValue(QuarryBlock.FACING));
            if(direction == null) return;

            Platform.ITEM_TRANSFER.pushToNeighbor(this.level, this.worldPosition, direction, inventory);
        }
    }

    //Ownership
    private @Nullable QuarryOwnerComponent owner;

    public void setOwner(UUID uuid, String name) {
        this.owner = QuarryOwnerComponent.of(uuid, name);

        this.setChanged();
        this.updateListeners();
    }
    public @Nullable QuarryOwnerComponent getOwner() {
        return this.owner;
    }
    @Nullable ServerPlayer getOwnerPlayer(@Nullable Level world) {
        if(this.owner == null || !(world instanceof ServerLevel serverWorld)) return null;

        return Platform.BLOCK_BREAK.getOwnerPlayer(serverWorld, new GameProfile(this.owner.getUuid(), this.owner.getName()));
    }
    public void refreshOwnerName() {
        if(this.owner == null || !(this.level instanceof ServerLevel serverWorld)) return;

        NameAndId nameAndId = serverWorld.getServer().services().nameToIdCache().get(this.owner.getUuid()).orElse(null);
        if(nameAndId != null && !nameAndId.name().equals(this.owner.getName())) {
            this.setOwner(this.owner.getUuid(), nameAndId.name());
        }
    }

    //Authorization
    private @Nullable QuarryBlockedDataComponent blocked;

    public @Nullable QuarryBlockedDataComponent getBlocked() {
        return this.blocked;
    }
    boolean canModify(ServerLevel world, BlockActress blockActress) {
        if(this.owner == null) return false;

        boolean modifiableWithoutBreakCheck = blockActress.blockEntity != null && blockActress.blockState.is(ZBlockTags.MODIFIABLE_WITHOUT_BREAK_CHECK);
        boolean allowed = modifiableWithoutBreakCheck || Platform.BLOCK_BREAK.canOwnerBreak(world, new GameProfile(this.owner.getUuid(), this.owner.getName()), blockActress.blockPos, blockActress.blockState, blockActress.blockEntity);

        if(allowed) {
            this.setBlocked(null);
        } else {
            this.setBlocked(QuarryBlockedDataComponent.of(blockActress.block, blockActress.blockPos));
        }

        return allowed;
    }
    void setBlocked(@Nullable QuarryBlockedDataComponent blocked) {
        if(Objects.equals(this.blocked, blocked)) return;

        this.blocked = blocked;

        this.updateListeners();
    }

    //World Ticking
    private boolean deferringChanges = false;
    private boolean hasDeferredChanges = false;

    @Override
    public void setChanged() {
        if(this.deferringChanges) {
            this.hasDeferredChanges = true;
        } else {
            super.setChanged();
        }
    }
    public static void tick(Level thisWorld, BlockPos thisPos, BlockState thisBlockState, QuarryBlockEntity thisBlockEntity) {
        thisBlockEntity.deferringChanges = true;

        try {
            tickMachine(thisWorld, thisPos, thisBlockState, thisBlockEntity);
        } finally {
            thisBlockEntity.deferringChanges = false;

            if(thisBlockEntity.hasDeferredChanges) {
                thisBlockEntity.hasDeferredChanges = false;
                thisBlockEntity.setChanged();
            }
        }
    }
    private static void tickMachine(Level thisWorld, BlockPos thisPos, BlockState thisBlockState, QuarryBlockEntity thisBlockEntity) {
        if(thisWorld == null) return;
        if(thisWorld.isClientSide()) return;

        QuarryBlock thisBlock = thisBlockEntity.getQuarryBlock();
        if(thisBlock == null) return;

        thisBlockEntity.attemptCharge();
        thisBlockEntity.attemptPushIo(IoState.ORANGE);

        thisBlockEntity.Miner.tick((ServerLevel) thisWorld, thisPos, thisBlockState, thisBlock);
    }

    //Chunk Navigation
    public void nextChunk() {
        ItemStack blueprintStack = this.BlueprintInventory.getItem(0);
        if(!(blueprintStack.getItem() instanceof BlueprintItem blueprintItem)) return;
        if(!blueprintItem.hasNextChunk(blueprintStack)) return;

        blueprintItem.increaseMinedChunks(blueprintStack);
        this.setChanged();
    }
    public void previousChunk() {
        ItemStack blueprintStack = this.BlueprintInventory.getItem(0);
        if(!(blueprintStack.getItem() instanceof BlueprintItem blueprintItem)) return;
        if(!blueprintItem.hasPreviousChunk(blueprintStack)) return;

        blueprintItem.increaseMinedChunks(blueprintStack, -1);
        this.setChanged();
    }

    //Mining
    public final QuarryMiner Miner = QuarryMiner.ofRef(this);

    //Data Components
    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        EnergyContainer.read(components.getOrDefault(Platform.ENERGY.getEnergyComponent(), 0L));

        OutputInventory.read(components.get(QuarryInventory.Output.COMPONENT));
        BatteryInputInventory.read(components.get(QuarryInventory.BatteryInput.COMPONENT));
        BlueprintInventory.read(components.get(QuarryInventory.Blueprint.COMPONENT));
        DrillInventory.read(components.get(QuarryInventory.Drill.COMPONENT));
        MachineUpgradesInventory.read(components.get(QuarryInventory.MachineUpgrades.COMPONENT));
        ExpansionsInventory.read(components.get(QuarryInventory.Expansions.COMPONENT));

        Configuration.read(components.get(MachineConfiguration.COMPONENT));
    }
    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder componentMapBuilder) {
        componentMapBuilder.set(Platform.ENERGY.getEnergyComponent(), EnergyContainer.getStored());

        componentMapBuilder.set(QuarryInventory.Output.COMPONENT, ItemContainerContents.fromItems(OutputInventory.getItems()));
        componentMapBuilder.set(QuarryInventory.BatteryInput.COMPONENT, ItemContainerContents.fromItems(BatteryInputInventory.getItems()));
        componentMapBuilder.set(QuarryInventory.Blueprint.COMPONENT, ItemContainerContents.fromItems(BlueprintInventory.getItems()));
        componentMapBuilder.set(QuarryInventory.Drill.COMPONENT, ItemContainerContents.fromItems(DrillInventory.getItems()));
        componentMapBuilder.set(QuarryInventory.MachineUpgrades.COMPONENT, ItemContainerContents.fromItems(MachineUpgradesInventory.getItems()));
        componentMapBuilder.set(QuarryInventory.Expansions.COMPONENT, ItemContainerContents.fromItems(ExpansionsInventory.getItems()));

        componentMapBuilder.set(MachineConfiguration.COMPONENT, Configuration.snapshot());
    }
    @Override
    public void removeComponentsFromTag(ValueOutput view) {
        view.discard("rxj.pw/Energy");
        view.discard("rxj.pw/Storage");
        view.discard("rxj.pw/Config");
    }

    //Persistence
    @Override
    public void loadAdditional(ValueInput view) {
        //Energy
        CompoundTag Energy = view.read("rxj.pw/Energy", CompoundTag.CODEC).orElseGet(CompoundTag::new);
        EnergyContainer.read(Energy);

        //Storage
        ValueInput Storage = view.childOrEmpty("rxj.pw/Storage");

        OutputInventory.read(Storage.childOrEmpty("OutputInventory"));
        BatteryInputInventory.read(Storage.childOrEmpty("BatteryInputInventory"));
        BlueprintInventory.read(Storage.childOrEmpty("BlueprintInventory"));
        DrillInventory.read(Storage.childOrEmpty("DrillInventory"));
        MachineUpgradesInventory.read(Storage.childOrEmpty("MachineUpgradesInventory"));
        ExpansionsInventory.read(Storage.childOrEmpty("ExpansionsInventory"));

        //Config
        CompoundTag Config = view.read("rxj.pw/Config", CompoundTag.CODEC).orElseGet(CompoundTag::new);
        Configuration.read(Config);

        //Owner
        this.owner = view.read("rxj.pw/Owner", QuarryOwnerComponent.CODEC).orElse(null);

        //Blocked
        this.blocked = view.read("rxj.pw/Blocked", QuarryBlockedDataComponent.CODEC).orElse(null);
    }
    @Override
    public void saveAdditional(ValueOutput view) {
        //Energy
        CompoundTag Energy = EnergyContainer.write();
        view.store("rxj.pw/Energy", CompoundTag.CODEC, Energy);

        //Storage
        ValueOutput Storage = view.child("rxj.pw/Storage");

        OutputInventory.write(Storage.child("OutputInventory"));
        BatteryInputInventory.write(Storage.child("BatteryInputInventory"));
        BlueprintInventory.write(Storage.child("BlueprintInventory"));
        DrillInventory.write(Storage.child("DrillInventory"));
        MachineUpgradesInventory.write(Storage.child("MachineUpgradesInventory"));
        ExpansionsInventory.write(Storage.child("ExpansionsInventory"));

        this.savePersistentClientData(view);
    }

    //Client Sync
    private void savePersistentClientData(ValueOutput view) {
        //Config
        CompoundTag Config = Configuration.write();
        view.store("rxj.pw/Config", CompoundTag.CODEC, Config);

        //Owner
        if(this.owner != null) view.store("rxj.pw/Owner", QuarryOwnerComponent.CODEC, this.owner);
    }
    private void saveTransientClientData(ValueOutput view) {
        //Blocked
        if(this.blocked != null) view.store("rxj.pw/Blocked", QuarryBlockedDataComponent.CODEC, this.blocked);
    }
    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
        TagValueOutput view = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registryLookup);
        this.savePersistentClientData(view);
        this.saveTransientClientData(view);

        return view.buildResult();
    }
    public void updateListeners(){
        if(level == null) return;

        level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
    }

    //Menu
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new QuarryBlockScreenHandler(syncId, playerInventory, OutputInventory, BatteryInputInventory, MachineUpgradesInventory, ExpansionsInventory, BlueprintInventory, DrillInventory, EnergyContainer, Configuration, this.getQuarryBlock(), this.worldPosition);
    }
    @Override
    public Component getDisplayName() {
        return Component.translatable(this.getBlockState().getBlock().getDescriptionId());
    }
    public QuarryBlockScreenData getScreenOpeningData(ServerPlayer player) {
        return QuarryBlockScreenData.of(this.getQuarryBlock(), worldPosition);
    }
}
