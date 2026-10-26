package pw.rxj.iron_quarry.blockentity;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
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
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.block.QuarryBlock;
import pw.rxj.iron_quarry.data.QuarryBlockScreenData;
import pw.rxj.iron_quarry.data.QuarryBlockedDataComponent;
import pw.rxj.iron_quarry.data.QuarryOwnerComponent;
import pw.rxj.iron_quarry.item.BlueprintItem;
import pw.rxj.iron_quarry.item.DrillItem;
import pw.rxj.iron_quarry.network.PacketQuarryBlockBreak;
import pw.rxj.iron_quarry.network.ZNetwork;
import pw.rxj.iron_quarry.screen.QuarryBlockScreenHandler;
import pw.rxj.iron_quarry.types.Face;
import pw.rxj.iron_quarry.types.IoState;
import pw.rxj.iron_quarry.util.*;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;
import team.reborn.energy.impl.EnergyImpl;

import java.util.*;
import java.util.function.Supplier;

public class QuarryBlockEntity extends BlockEntity implements ExtendedMenuProvider<QuarryBlockScreenData> {
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

    @Override
    public void setRemoved() {
        if(level == null) return;
        if(level.isClientSide()) return;

        ChunkLoadingManager.removeTickets((ServerLevel) this.level, this.worldPosition);

        super.setRemoved();
    }

    //Machine Configuration
    public MachineConfiguration Configuration = new MachineConfiguration() {
        @Override
        public void markDirty() {
            QuarryBlockEntity.this.setChanged();
        }

        @Override
        public void onIoUpdated(Face face, IoState ioState) {
            QuarryBlockEntity.this.updateListeners();
        }

        @Override
        public IndexedMap<IoState, Object> getUsedIoStates() {
            IndexedMap<IoState, Object> usedIo = new IndexedMap<>();
            usedIo.put(IoState.BLOCKED, null);
            usedIo.put(IoState.ORANGE, OutputInventory);
            usedIo.put(IoState.RED, DrillInventory);
            usedIo.put(IoState.PURPLE, BatteryInputInventory);

            return usedIo;
        }
    };

    //Energy Handling
    public final ComplexEnergyContainer EnergyContainer = new ComplexEnergyContainer() {
        @Override
        public void onFinalCommit() {
            QuarryBlockEntity.this.setChanged();
            QuarryBlockEntity.this.updateListeners();
        }

        @Override
        public long getCapacity() {
            QuarryBlock quarryBlock = QuarryBlockEntity.this.getQuarryBlock();
            if(quarryBlock == null) return 0;

            return quarryBlock.getEnergyCapacity();
        }

        @Override
        public long getMaxInsert(@Nullable Direction side) {
            return this.getCapacity();
        }

        @Override
        public long getMaxExtract(@Nullable Direction side) {
            return 0;
        }
    };

    public EnergyStorage getEnergySideConfiguration(Direction direction){
        return this.EnergyContainer.getSideStorage(direction);
    }

    public void attemptCharge() {
        if(level == null) return;
        if(level.isClientSide()) return;

        long chargeEnergy = Math.min(EnergyContainer.getFreeSpace(), EnergyContainer.getMaxInsert(null));
        if(chargeEnergy <= 0) return;

        ItemStack batteryStack = BatteryInputInventory.getItem(0);
        if(batteryStack.isEmpty()) return;

        EnergyStorage energyStorage = ContainerItemContext.ofSingleSlot(ContainerStorage.of(BatteryInputInventory, null).getSlot(0)).find(EnergyStorage.ITEM);

        if(energyStorage != null && energyStorage.getAmount() > 0) {
            EnergyStorageUtil.move(energyStorage, EnergyContainer.getSideStorage(null), Long.MAX_VALUE, null);
        } else if(OutputInventory.canAddItem(batteryStack)) {
            OutputInventory.addItem(BatteryInputInventory.removeItemNoUpdate(0));
        }


    }

    //Item Handling
    public final ComplexInventory OutputInventory = QuarryInventory.Output.ofRef(this);
    public final ComplexInventory BatteryInputInventory = QuarryInventory.BatteryInput.ofRef(this);
    public final ComplexInventory MachineUpgradesInventory = QuarryInventory.MachineUpgrades.ofRef(this);
    public final ComplexInventory BlueprintInventory = QuarryInventory.Blueprint.ofRef(this);
    public final ComplexInventory DrillInventory = QuarryInventory.Drill.ofRef(this);

    public List<ItemStack> getMachineUpgradeStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.addAll(MachineUpgradesInventory.items);
        stacks.addAll(DrillInventory.items);

        return stacks;

    }

    public ContainerStorage getItemSideConfiguration(Direction direction){
        if(direction == null) return null;

        Face face = Face.from(direction, this.getBlockState().getValue(QuarryBlock.FACING));
        Object object = this.Configuration.getLinkedIo(face);

        if(object instanceof Container inventory) {
            return ContainerStorage.of(inventory, null);
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

            Transaction transaction = Transaction.openOuter();

            Storage<ItemVariant> itemStorage = ItemStorage.SIDED.find(this.level, this.worldPosition.offset(direction.getUnitVec3i()), direction.getOpposite());
            if(itemStorage != null) {
                for (int i = 0; i < inventory.getContainerSize(); i++) {
                    ItemStack itemStack = inventory.getItem(i);
                    if(itemStack.isEmpty()) continue;

                    long insertedCount = itemStorage.insert(ItemVariant.of(itemStack), itemStack.getCount(), transaction);
                    inventory.removeItem(i, (int) insertedCount);

                }
            }

            transaction.commit();
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
    public void refreshOwnerName() {
        if(this.owner == null || !(this.level instanceof ServerLevel serverWorld)) return;

        NameAndId nameAndId = serverWorld.getServer().services().nameToIdCache().get(this.owner.getUuid()).orElse(null);
        if(nameAndId != null && !nameAndId.name().equals(this.owner.getName())) {
            this.setOwner(this.owner.getUuid(), nameAndId.name());
        }
    }
    public @Nullable QuarryBlockedDataComponent getBlocked() {
        return this.blocked;
    }

    //World Ticking
    private final ArrayList<BlockPos> MiningQueue = new ArrayList<>();
    private ChunkPos currentChunk;
    private int blueprintHash = 0;
    private int drillHash = 0;
    private int cooldown = 0;
    private static final int BLOCKED_RETRY_TICKS = 100;
    private int blockedCooldown = 0;
    private @Nullable QuarryBlockedDataComponent blocked;

    public static void tick(Level thisWorld, BlockPos thisPos, BlockState thisBlockState, QuarryBlockEntity thisBlockEntity) {
        if(thisWorld == null) return;
        if(thisWorld.isClientSide()) return;

        RandomSource random = thisWorld.getRandom();

        ServerLevel thisServerWorld = (ServerLevel) thisWorld;
        MinecraftServer minecraftServer = thisServerWorld.getServer();

        QuarryBlock thisBlock = thisBlockEntity.getQuarryBlock();
        if(thisBlock == null) return;

        thisBlockEntity.attemptCharge();
        thisBlockEntity.attemptPushIo(IoState.ORANGE);

        ItemStack blueprintStack = thisBlockEntity.BlueprintInventory.getItem(0);
        int blueprintHash = ItemStack.hashItemAndComponents(blueprintStack);
        ItemStack drillStack = thisBlockEntity.DrillInventory.getItem(0);
        int drillHash = drillStack.isEmpty() ? 0 : drillStack.hashCode();

        if(thisBlockEntity.blueprintHash != blueprintHash || thisBlockEntity.drillHash != drillHash) {
            thisBlockEntity.blueprintHash = blueprintHash;
            thisBlockEntity.drillHash = drillHash;

            thisBlockEntity.currentChunk = null;
            thisBlockEntity.MiningQueue.clear();
            thisBlockEntity.setBlocked(null);

            ChunkLoadingManager.removeTickets(thisServerWorld, thisPos);

            //Unlike what the name "removeTickets" might suggest, chunk tickets are NOT instantly removed,
            //but instead are removed at the start of the next tick aka they only get "marked for removal".
            //If this tick isn't skipped after this, the quarry will show abnormal behaviour.
            return;
        }

        if(!(drillStack.getItem() instanceof DrillItem drillItem)) return;
        if(!(blueprintStack.getItem() instanceof BlueprintItem blueprintItem)) return;
        if(!blueprintItem.isSealed(blueprintStack) || blueprintItem.allChunksMined(blueprintStack)) return;

        BlockPos firstPos = blueprintItem.getFirstPos(blueprintStack).orElse(null);
        if(firstPos == null) return;
        BlockPos secondPos = blueprintItem.getSecondPos(blueprintStack).orElse(null);
        if(secondPos == null) return;

        ResourceKey<Level> worldRegistryKey = blueprintItem.getWorldRegistryKey(blueprintStack).orElse(null);
        ServerLevel serverWorldToBreak = minecraftServer.getLevel(worldRegistryKey);
        if(serverWorldToBreak == null) return;
        if(thisBlockEntity.owner == null) return;

        MachineUpgradesUtil upgradesUtil = MachineUpgradesUtil.from(thisBlockEntity.getMachineUpgradeStacks());
        long minimumEnergyConsumption = thisBlock.getEnergyConsumption(upgradesUtil, Blocks.STONE);
        if(thisBlockEntity.EnergyContainer.getStored() < minimumEnergyConsumption) return;

        if(thisBlockEntity.blockedCooldown > 0) {
            thisBlockEntity.blockedCooldown--; return;
        }

        int threads = 1;

        if(thisBlockEntity.cooldown > 0) {
            thisBlockEntity.cooldown--; return;
        } else {
            float messy_tpo = thisBlock.getTicksPerOperation() / upgradesUtil.getSpeedMultiplier();
            int ceiled_tpo = (int) Math.ceil(messy_tpo);

            float operation_leftover = ceiled_tpo - messy_tpo;
            if(operation_leftover > 0) {
                if(messy_tpo <= 1) {
                    float messy_threads = 1.0F / messy_tpo;
                    threads = (int) Math.floor(messy_threads);

                    if(random.nextFloat() <= messy_threads - threads) threads++;
                } else {
                    if(random.nextFloat() <= operation_leftover) ceiled_tpo--;
                }
            }

            thisBlockEntity.cooldown = ceiled_tpo - 1;
        }

        ArrayList<BlockPos> MiningQueue = thisBlockEntity.MiningQueue;

        if(MiningQueue.isEmpty()) {
            if(thisBlockEntity.currentChunk != null) {
                ChunkLoadingManager.removeTicket(serverWorldToBreak, thisBlockEntity.currentChunk, thisServerWorld, thisPos);

                blueprintItem.increaseMinedChunks(blueprintStack);
                thisBlockEntity.blueprintHash = ItemStack.hashItemAndComponents(blueprintStack);

                thisBlockEntity.currentChunk = null;
            }

            List<ChunkPos> chunkPosList = blueprintItem.getNextChunkPos(blueprintStack, 2);
            if(chunkPosList.isEmpty()) return;

            ChunkPos chunkPos = chunkPosList.get(0);
            if(chunkPos == null) return;

            for (ChunkPos chunkPosToLoad : chunkPosList) {
                ChunkLoadingManager.addTicket(serverWorldToBreak, chunkPosToLoad, thisServerWorld, thisPos);
            }

            LevelChunk worldChunk = serverWorldToBreak.getChunkSource().getChunkNow(chunkPos.x(), chunkPos.z());
            if(worldChunk == null) return;

            thisBlockEntity.currentChunk = chunkPos;

            //General Max/Min
            int minX = Math.min(firstPos.getX(), secondPos.getX());
            int minZ = Math.min(firstPos.getZ(), secondPos.getZ());

            int maxX = Math.max(firstPos.getX(), secondPos.getX());
            int maxZ = Math.max(firstPos.getZ(), secondPos.getZ());

            //Max/Min within the current Chunk
            int minChunkBlockX = Math.max(chunkPos.x() << 4, minX);
            int minChunkBlockY = Math.min(firstPos.getY(), secondPos.getY());
            int minChunkBlockZ = Math.max(chunkPos.z() << 4, minZ);

            int maxChunkBlockX = Math.min((chunkPos.x() << 4) + 15, maxX);
            int maxChunkBlockY = Math.max(firstPos.getY(), secondPos.getY());
            int maxChunkBlockZ = Math.min((chunkPos.z() << 4) + 15, maxZ);

            for (int x = minChunkBlockX; x <= maxChunkBlockX; x++) {
                for (int z = minChunkBlockZ; z <= maxChunkBlockZ; z++) {
                    for (int y = minChunkBlockY; y <= maxChunkBlockY; y++) {
                        BlockPos blockPos = new BlockPos(x, y, z);
                        BlockState blockState = worldChunk.getBlockState(blockPos);

                        if(thisBlockEntity.canDrill(worldChunk, blockState, blockPos, upgradesUtil)) {
                            MiningQueue.add(blockPos);
                        }
                    }
                }
            }

            return;
        }

        List<BlockPosState> drillResults = thisBlockEntity.drillQueue(threads, thisBlock, thisBlockState, thisPos, thisBlockEntity, serverWorldToBreak, drillStack, drillItem, upgradesUtil);
        HashSet<Integer> shouldPlaySound = ZUtil.getNSpacedIndexes(drillResults.size(), 10);

        for (int i = 0; i < drillResults.size(); i++) {
            BlockPosState drill = drillResults.get(i);
            boolean playSound = shouldPlaySound.contains(i);

            ZNetwork.sendToAround(serverWorldToBreak, drill.blockPos, 64.0, player -> {
                return PacketQuarryBlockBreak.bake(drill.blockPos, drill.blockState, playSound);
            });
        }
    }

    private boolean canModify(ServerLevel world, BlockActress blockActress) {
        if(this.owner == null) return false;

        FakePlayer fakePlayer = FakePlayer.get(world, new GameProfile(this.owner.getUuid(), this.owner.getName()));
        boolean allowed = PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(world, fakePlayer, blockActress.blockPos, blockActress.blockState, blockActress.blockEntity);

        if(allowed) {
            this.setBlocked(null);
        } else {
            this.setBlocked(QuarryBlockedDataComponent.of(blockActress.block, blockActress.blockPos));
        }

        return allowed;
    }
    private void setBlocked(@Nullable QuarryBlockedDataComponent blocked) {
        this.blockedCooldown = blocked == null ? 0 : BLOCKED_RETRY_TICKS;
        if(Objects.equals(this.blocked, blocked)) return;

        this.blocked = blocked;

        this.setChanged();
        this.updateListeners();
    }
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

    private boolean canDrill(BlockActress blockActress, MachineUpgradesUtil upgradesUtil) {
        return this.canDrill(() -> blockActress.blockEntity, blockActress.blockState, upgradesUtil);
    }
    private boolean canDrill(LevelChunk worldChunk, BlockState blockState, BlockPos blockPos, MachineUpgradesUtil upgradesUtil) {
        return this.canDrill(() -> ZUtil.getBlockEntity(worldChunk, blockState, blockPos), blockState, upgradesUtil);
    }
    //The block hardness check will render any chest looting augment useless.
    private boolean canDrill(Supplier<BlockEntity> blockEntitySupplier, BlockState blockState, MachineUpgradesUtil upgradesUtil) {
        if(blockState.isAir()) return false;
        Block block = blockState.getBlock();
        if(block.defaultDestroyTime() < 0) return false;
        BlockEntity blockEntity = blockEntitySupplier.get();
        if(blockEntity == null) return true;

        if(upgradesUtil.hasChestLooting()) {
            RandomizableContainerBlockEntity lootableContainer = ZUtil.getUnlockedLootableContainer(blockEntity);
            return lootableContainer != null && !lootableContainer.isEmpty();
        }

        return false;
    }

    private List<BlockPosState> drillQueue(int threads, QuarryBlock quarryBlock, BlockState quarryBlockState, BlockPos quarryPos, QuarryBlockEntity quarryBlockEntity, ServerLevel serverWorldToBreak,
                                           ItemStack drillStack, DrillItem drillItem, MachineUpgradesUtil upgradesUtil) {
        List<BlockPosState> drillResults = new ArrayList<>();

        ComplexEnergyContainer EnergyContainer = quarryBlockEntity.EnergyContainer;
        ComplexInventory OutputInventory = quarryBlockEntity.OutputInventory;
        ArrayList<BlockPos> MiningQueue = quarryBlockEntity.MiningQueue;

        for (int threadIndex = 0; threadIndex < threads; threadIndex++) {
            if(drillStack.isEmpty()) return drillResults;
            if(MiningQueue.isEmpty()) return drillResults;
            int miningQueueIndex = MiningQueue.size() - 1;

            BlockPos blockPosToBreak = MiningQueue.get(miningQueueIndex);
            Optional<BlockActress> testActress = BlockActress.of(serverWorldToBreak, blockPosToBreak);
            if(testActress.isEmpty()) return drillResults;

            //enables block entity tracking / disables neighbor- and redstone updates to reduce lag!
            BlockActress blockActress = testActress.get().with(true, 0, 2);

            if(blockActress.isLiquidBlock()) {
                if(!quarryBlockEntity.canModify(serverWorldToBreak, blockActress)) return drillResults;

                blockActress.setBlockState(Blocks.AIR);
                MiningQueue.remove(miningQueueIndex);
                continue;
            }

            //handle special block entities
            if(this.canDrill(blockActress, upgradesUtil)) {
                if(!quarryBlockEntity.canModify(serverWorldToBreak, blockActress)) return drillResults;

                //will only work if canDrill conditions are met! like if the container is not empty
                if(blockActress.blockEntity instanceof RandomizableContainerBlockEntity lootableContainer) {
                    for (int slot = 0; slot < lootableContainer.getContainerSize(); slot++) {
                        ItemStack stack = lootableContainer.getItem(slot).copy();
                        if(stack.isEmpty()) continue;
                        stack.setCount(1);

                        if(OutputInventory.canAddItem(stack)) {
                            long energyConsumption = quarryBlock.getEnergyConsumption(upgradesUtil);
                            if(EnergyContainer.getStored() < energyConsumption) return drillResults;

                            OutputInventory.addItem(lootableContainer.removeItem(slot, 1));
                            EnergyContainer.useEnergy(energyConsumption);

                            break;
                        }
                        //not enough space in quarry inventory
                        return drillResults;
                    }
                    //only one item per operation
                    continue;
                }
                //non-block entities will fall through
            } else {
                MiningQueue.remove(miningQueueIndex);
                continue;
            }

            //energy consumption
            long energyConsumption = quarryBlock.getEnergyConsumption(upgradesUtil, blockActress.block);
            if(EnergyContainer.getStored() < energyConsumption) return drillResults;

            //loot generation
            ItemStack mockPickaxe = drillItem.getToolStack().copy();

            if(upgradesUtil.hasSilkTouch()) {
                mockPickaxe.enchant(ZUtil.getEnchantmentEntry(serverWorldToBreak.registryAccess(), Enchantments.SILK_TOUCH), 1);
            } else {
                int fortuneLevel = FortuneUtil.fromProbability(upgradesUtil.getFortuneMultiplier());
                if(fortuneLevel > 0) mockPickaxe.enchant(ZUtil.getEnchantmentEntry(serverWorldToBreak.registryAccess(), Enchantments.FORTUNE), fortuneLevel);
            }

            //only drop item if required mining level is present
            if(blockActress.isCorrectForDrops(drillItem.getToolComponent())) {
                List<ItemStack> droppedStacks = blockActress.getDroppedStacks(quarryBlockState, quarryPos, mockPickaxe);

                if(OutputInventory.canInsertAll(droppedStacks)) {
                    droppedStacks.forEach(OutputInventory::addItem);
                } else {
                    return drillResults;
                }
            }

            //mining
            BlockPosState blockPosState = blockActress.toBlockPosState();

            EnergyContainer.useEnergy(energyConsumption);
            blockActress.setBlockState(Blocks.AIR);

            boolean drillBroke = drillItem.cdamage(drillStack, 1, serverWorldToBreak, null);
            if(drillBroke) drillItem.triggerBreakEvent(drillStack, quarryPos.getCenter(), serverWorldToBreak);

            MiningQueue.remove(miningQueueIndex);
            drillResults.add(blockPosState);
        }

        return drillResults;
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        EnergyContainer.read(components.getOrDefault(EnergyImpl.ENERGY_COMPONENT, 0L));

        OutputInventory.read(components.get(QuarryInventory.Output.COMPONENT));
        BatteryInputInventory.read(components.get(QuarryInventory.BatteryInput.COMPONENT));
        BlueprintInventory.read(components.get(QuarryInventory.Blueprint.COMPONENT));
        DrillInventory.read(components.get(QuarryInventory.Drill.COMPONENT));
        MachineUpgradesInventory.read(components.get(QuarryInventory.MachineUpgrades.COMPONENT));

        Configuration.read(components.get(MachineConfiguration.COMPONENT));
    }
    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder componentMapBuilder) {
        componentMapBuilder.set(EnergyImpl.ENERGY_COMPONENT, EnergyContainer.getStored());

        componentMapBuilder.set(QuarryInventory.Output.COMPONENT, OutputInventory);
        componentMapBuilder.set(QuarryInventory.BatteryInput.COMPONENT, BatteryInputInventory);
        componentMapBuilder.set(QuarryInventory.Blueprint.COMPONENT, BlueprintInventory);
        componentMapBuilder.set(QuarryInventory.Drill.COMPONENT, DrillInventory);
        componentMapBuilder.set(QuarryInventory.MachineUpgrades.COMPONENT, MachineUpgradesInventory);

        componentMapBuilder.set(MachineConfiguration.COMPONENT, Configuration);
    }
    @Override
    public void removeComponentsFromTag(ValueOutput view) {
        view.discard("rxj.pw/Energy");
        view.discard("rxj.pw/Storage");
        view.discard("rxj.pw/Config");
    }

    //NBT Handling
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

        //Config
        CompoundTag Config = Configuration.write();
        view.store("rxj.pw/Config", CompoundTag.CODEC, Config);

        //Owner
        if(this.owner != null) view.store("rxj.pw/Owner", QuarryOwnerComponent.CODEC, this.owner);

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
        return saveWithoutMetadata(registryLookup);
    }

    public void updateListeners(){
        if(level == null) return;

        level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
    }

    //Screen Handler
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new QuarryBlockScreenHandler(syncId, playerInventory, OutputInventory, BatteryInputInventory, MachineUpgradesInventory, BlueprintInventory, DrillInventory, EnergyContainer, Configuration, this.getQuarryBlock(), this.worldPosition);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(this.getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public QuarryBlockScreenData getScreenOpeningData(ServerPlayer player) {
        return QuarryBlockScreenData.of(this.getQuarryBlock(), worldPosition);
    }
}
