package pw.rxj.iron_quarry.blockentity;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.block.QuarryBlock;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class QuarryBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory {
    public QuarryBlockEntity(BlockPos pos, BlockState state) {
        super(ZBlockEntities.QUARRY_BLOCK_ENTITY, pos, state);
    }

    private Block getBlock() {
        if(this.world == null) return null;

        return this.world.getBlockState(this.pos).getBlock();
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

    public boolean canPlayerUse(PlayerEntity player) {
        if (this.world != null && this.world.getBlockEntity(this.pos) != this) {
            return false;
        } else {
            return player.squaredDistanceTo((double)this.pos.getX() + 0.5, (double)this.pos.getY() + 0.5, (double)this.pos.getZ() + 0.5) <= 64.0;
        }
    }

    @Override
    public void markRemoved() {
        if(world == null) return;
        if(world.isClient()) return;

        ChunkLoadingManager.removeTickets((ServerWorld) this.world, this.pos);

        super.markRemoved();
    }

    //Machine Configuration
    public MachineConfiguration Configuration = new MachineConfiguration() {
        @Override
        public void markDirty() {
            QuarryBlockEntity.this.markDirty();
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
            QuarryBlockEntity.this.markDirty();
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
        if(world == null) return;
        if(world.isClient()) return;

        long chargeEnergy = Math.min(EnergyContainer.getFreeSpace(), EnergyContainer.getMaxInsert(null));
        if(chargeEnergy <= 0) return;

        ItemStack batteryStack = BatteryInputInventory.getStack(0);
        if(batteryStack.isEmpty()) return;

        EnergyStorage energyStorage = ContainerItemContext.ofSingleSlot(InventoryStorage.of(BatteryInputInventory, null).getSlot(0)).find(EnergyStorage.ITEM);

        if(energyStorage != null && energyStorage.getAmount() > 0) {
            EnergyStorageUtil.move(energyStorage, EnergyContainer.getSideStorage(null), Long.MAX_VALUE, null);
        } else if(OutputInventory.canInsert(batteryStack)) {
            OutputInventory.addStack(BatteryInputInventory.removeStack(0));
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
        stacks.addAll(MachineUpgradesInventory.heldStacks);
        stacks.addAll(DrillInventory.heldStacks);

        return stacks;

    }

    public InventoryStorage getItemSideConfiguration(Direction direction){
        if(direction == null) return null;

        Face face = Face.from(direction, this.getCachedState().get(QuarryBlock.FACING));
        Object object = this.Configuration.getLinkedIo(face);

        if(object instanceof Inventory inventory) {
            return InventoryStorage.of(inventory, null);
        }

        return null;
    }

    public void attemptPushIo(IoState ioState){
        for (Face face: Face.values()) {
            if(!this.Configuration.getIoState(face).equals(ioState)) continue;

            Object object = this.Configuration.getLinkedIo(face);
            if(!(object instanceof Inventory inventory)) continue;

            Direction direction = face.toDirection(this.getCachedState().get(QuarryBlock.FACING));
            if(direction == null) return;

            Transaction transaction = Transaction.openOuter();

            Storage itemStorage = ItemStorage.SIDED.find(this.world, this.pos.add(direction.getVector()), direction.getOpposite());
            if(itemStorage != null) {
                for (int i = 0; i < inventory.size(); i++) {
                    ItemStack itemStack = inventory.getStack(i);
                    if(itemStack.isEmpty()) continue;

                    long insertedCount = itemStorage.insert(ItemVariant.of(itemStack), itemStack.getCount(), transaction);
                    inventory.removeStack(i, (int) insertedCount);

                }
            }

            transaction.commit();
        }
    }

    //Fluid Handling
    public final SingleFluidStorage CoolantTank = new SingleFluidStorage() {
        @Override
        protected long getCapacity(FluidVariant variant) {
            return FluidConstants.BUCKET * 2;
        }

        @Override
        protected void onFinalCommit() {
            QuarryBlockEntity.this.markDirty();
        }
    };

    public SingleFluidStorage getFluidSideConfiguration(Direction direction){
        if(direction == null) return null;

        Face face = Face.from(direction, this.getCachedState().get(QuarryBlock.FACING));
        Object object = this.Configuration.getLinkedIo(face);

        if(object instanceof SingleFluidStorage fluidStorage) {
            return fluidStorage;
        }

        return null;
    }

    //World Ticking
    private final ArrayList<BlockPos> MiningQueue = new ArrayList<>();
    private ChunkPos currentChunk;
    private int blueprintHash = 0;
    private int drillHash = 0;
    private int cooldown = 0;

    public static void tick(World thisWorld, BlockPos thisPos, BlockState thisBlockState, QuarryBlockEntity thisBlockEntity) {
        if(thisWorld == null) return;
        if(thisWorld.isClient()) return;

        Random random = thisWorld.random;

        ServerWorld thisServerWorld = (ServerWorld) thisWorld;
        MinecraftServer minecraftServer = thisServerWorld.getServer();

        QuarryBlock thisBlock = thisBlockEntity.getQuarryBlock();
        if(thisBlock == null) return;

        thisBlockEntity.attemptCharge();
        thisBlockEntity.attemptPushIo(IoState.ORANGE);

        ItemStack blueprintStack = thisBlockEntity.BlueprintInventory.getStack(0);
        int blueprintHash = blueprintStack.hashCode();
        ItemStack drillStack = thisBlockEntity.DrillInventory.getStack(0);
        int drillHash = drillStack.hashCode();

        if(thisBlockEntity.blueprintHash != blueprintHash || thisBlockEntity.drillHash != drillHash) {
            thisBlockEntity.blueprintHash = blueprintHash;
            thisBlockEntity.drillHash = drillHash;

            thisBlockEntity.currentChunk = null;
            thisBlockEntity.MiningQueue.clear();

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

        RegistryKey<World> worldRegistryKey = blueprintItem.getWorldRegistryKey(blueprintStack).orElse(null);
        ServerWorld serverWorldToBreak = minecraftServer.getWorld(worldRegistryKey);
        if(serverWorldToBreak == null) return;

        MachineUpgradesUtil upgradesUtil = MachineUpgradesUtil.from(thisBlockEntity.getMachineUpgradeStacks());
        long minimumEnergyConsumption = thisBlock.getEnergyConsumption(upgradesUtil, Blocks.STONE);
        if(thisBlockEntity.EnergyContainer.getStored() < minimumEnergyConsumption) return;

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

                thisBlockEntity.currentChunk = null;
            }

            List<ChunkPos> chunkPosList = blueprintItem.getNextChunkPos(blueprintStack, 2);
            if(chunkPosList.isEmpty()) return;

            ChunkPos chunkPos = chunkPosList.get(0);
            if(chunkPos == null) return;

            for (ChunkPos chunkPosToLoad : chunkPosList) {
                ChunkLoadingManager.addTicket(serverWorldToBreak, chunkPosToLoad, thisServerWorld, thisPos);
            }

            WorldChunk worldChunk = serverWorldToBreak.getChunkManager().getWorldChunk(chunkPos.x, chunkPos.z);
            if(worldChunk == null) return;

            thisBlockEntity.currentChunk = chunkPos;

            //General Max/Min
            int minX = Math.min(firstPos.getX(), secondPos.getX());
            int minZ = Math.min(firstPos.getZ(), secondPos.getZ());

            int maxX = Math.max(firstPos.getX(), secondPos.getX());
            int maxZ = Math.max(firstPos.getZ(), secondPos.getZ());

            //Max/Min within the current Chunk
            int minChunkBlockX = Math.max(chunkPos.x << 4, minX);
            int minChunkBlockY = Math.min(firstPos.getY(), secondPos.getY());
            int minChunkBlockZ = Math.max(chunkPos.z << 4, minZ);

            int maxChunkBlockX = Math.min((chunkPos.x << 4) + 15, maxX);
            int maxChunkBlockY = Math.max(firstPos.getY(), secondPos.getY());
            int maxChunkBlockZ = Math.min((chunkPos.z << 4) + 15, maxZ);

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

    private boolean canDrill(BlockActress blockActress, MachineUpgradesUtil upgradesUtil) {
        return this.canDrill(() -> blockActress.blockEntity, blockActress.blockState, upgradesUtil);
    }
    private boolean canDrill(WorldChunk worldChunk, BlockState blockState, BlockPos blockPos, MachineUpgradesUtil upgradesUtil) {
        return this.canDrill(() -> ZUtil.getBlockEntity(worldChunk, blockState, blockPos), blockState, upgradesUtil);
    }
    //The block hardness check will render any chest looting augment useless.
    private boolean canDrill(Supplier<BlockEntity> blockEntitySupplier, BlockState blockState, MachineUpgradesUtil upgradesUtil) {
        if(blockState.isAir()) return false;
        Block block = blockState.getBlock();
        if(block.getHardness() < 0) return false;
        BlockEntity blockEntity = blockEntitySupplier.get();
        if(blockEntity == null) return true;

        if(upgradesUtil.hasChestLooting()) {
            LootableContainerBlockEntity lootableContainer = ZUtil.getUnlockedLootableContainer(blockEntity);
            return lootableContainer != null && !lootableContainer.isEmpty();
        }

        return false;
    }

    private List<BlockPosState> drillQueue(int threads, QuarryBlock quarryBlock, BlockState quarryBlockState, BlockPos quarryPos, QuarryBlockEntity quarryBlockEntity, ServerWorld serverWorldToBreak,
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

            if(blockActress.isFluidStatePresent()) {
                blockActress.setBlockState(Blocks.AIR);
                MiningQueue.remove(miningQueueIndex);
                continue;
            }

            //handle special block entities
            if(this.canDrill(blockActress, upgradesUtil)) {
                //will only work if canDrill conditions are met! like if the container is not empty
                if(blockActress.blockEntity instanceof LootableContainerBlockEntity lootableContainer) {
                    for (int slot = 0; slot < lootableContainer.size(); slot++) {
                        ItemStack stack = lootableContainer.getStack(slot).copy();
                        if(stack.isEmpty()) continue;
                        stack.setCount(1);

                        if(OutputInventory.canInsert(stack)) {
                            long energyConsumption = quarryBlock.getEnergyConsumption(upgradesUtil);
                            if(EnergyContainer.getStored() < energyConsumption) return drillResults;

                            OutputInventory.addStack(lootableContainer.removeStack(slot, 1));
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
            ItemStack mockPickaxe = Items.NETHERITE_PICKAXE.getDefaultStack();

            if(upgradesUtil.hasSilkTouch()) {
                mockPickaxe.addEnchantment(Enchantments.SILK_TOUCH, 1);
            } else {
                int fortuneLevel = FortuneUtil.fromProbability(upgradesUtil.getFortuneMultiplier());
                mockPickaxe.addEnchantment(Enchantments.FORTUNE, fortuneLevel);
            }

            //only drop item if required mining level is present
            if(drillItem.getMiningLevel() >= blockActress.getRequiredMiningLevel()) {
                List<ItemStack> droppedStacks = blockActress.getDroppedStacks(quarryBlockState, quarryPos, mockPickaxe);

                if(OutputInventory.canInsertAll(droppedStacks)) {
                    droppedStacks.forEach(OutputInventory::addStack);
                } else {
                    return drillResults;
                }
            }

            //mining
            BlockPosState blockPosState = blockActress.toBlockPosState();

            EnergyContainer.useEnergy(energyConsumption);
            blockActress.setBlockState(Blocks.AIR);

            boolean drillBroke = drillItem.cdamage(drillStack, 1, serverWorldToBreak.getRandom(), null);
            if(drillBroke) {
                double x = quarryPos.getX() + 0.5;
                double y = quarryPos.getY() + 0.5;
                double z = quarryPos.getZ() + 0.5;

                PlaySoundS2CPacket soundPacket = new PlaySoundS2CPacket(RegistryEntry.of(SoundEvents.ENTITY_ITEM_BREAK), SoundCategory.BLOCKS, x, y, z, 1.0F, 1.0F, serverWorldToBreak.getSeed());
                serverWorldToBreak.getServer().getPlayerManager().sendToAround(null, x, y, z, 64.0, serverWorldToBreak.getRegistryKey(), soundPacket);

                drillStack.setCount(0);
            }

            MiningQueue.remove(miningQueueIndex);
            drillResults.add(blockPosState);
        }

        return drillResults;
    }


    //NBT Handling
    @Override
    public void readNbt(NbtCompound tag) {
        //Energy
        NbtCompound Energy = tag.getCompound("rxj.pw/Energy");
        EnergyContainer.read(Energy);

        //Storage
        NbtCompound Storage = tag.getCompound("rxj.pw/Storage");

        NbtCompound StorageCoolantTank = Storage.getCompound("CoolantTank");
        CoolantTank.variant = FluidVariant.fromNbt(StorageCoolantTank.getCompound("Variant"));
        CoolantTank.amount = StorageCoolantTank.getLong("Amount");

        NbtCompound StorageOutputInventory = Storage.getCompound("OutputInventory");
        OutputInventory.read(StorageOutputInventory.getList("Items", NbtElement.COMPOUND_TYPE));

        NbtCompound StorageBatteryInputInventory = Storage.getCompound("BatteryInputInventory");
        BatteryInputInventory.read(StorageBatteryInputInventory.getList("Items", NbtElement.COMPOUND_TYPE));

        NbtCompound StorageBlueprintInventory = Storage.getCompound("BlueprintInventory");
        BlueprintInventory.read(StorageBlueprintInventory.getList("Items", NbtElement.COMPOUND_TYPE));

        NbtCompound StorageDrillInventory = Storage.getCompound("DrillInventory");
        DrillInventory.read(StorageDrillInventory.getList("Items", NbtElement.COMPOUND_TYPE));

        NbtCompound StorageMachineUpgradesInventory = Storage.getCompound("MachineUpgradesInventory");
        MachineUpgradesInventory.read(StorageMachineUpgradesInventory.getList("Items", NbtElement.COMPOUND_TYPE));

        //Config
        NbtCompound Config = tag.getCompound("rxj.pw/Config");
        Configuration.read(Config);
    }
    @Override
    public void writeNbt(NbtCompound tag) {
        //Energy
        NbtCompound Energy = EnergyContainer.write();
        tag.put("rxj.pw/Energy", Energy);

        //Storage
        NbtCompound Storage = new NbtCompound();

        NbtCompound StorageCoolantTank = new NbtCompound();
        StorageCoolantTank.put("Variant", CoolantTank.variant.toNbt());
        StorageCoolantTank.putLong("Amount", CoolantTank.amount);
        Storage.put("CoolantTank", StorageCoolantTank);

        NbtCompound StorageOutputInventory = new NbtCompound();
        StorageOutputInventory.put("Items", OutputInventory.write());
        Storage.put("OutputInventory", StorageOutputInventory);

        NbtCompound StorageBatteryInputInventory = new NbtCompound();
        StorageBatteryInputInventory.put("Items", BatteryInputInventory.write());
        Storage.put("BatteryInputInventory", StorageBatteryInputInventory);

        NbtCompound StorageBlueprintInventory = new NbtCompound();
        StorageBlueprintInventory.put("Items", BlueprintInventory.write());
        Storage.put("BlueprintInventory", StorageBlueprintInventory);

        NbtCompound StorageDrillInventory = new NbtCompound();
        StorageDrillInventory.put("Items", DrillInventory.write());
        Storage.put("DrillInventory", StorageDrillInventory);

        NbtCompound StorageMachineUpgradesInventory = new NbtCompound();
        StorageMachineUpgradesInventory.put("Items", MachineUpgradesInventory.write());
        Storage.put("MachineUpgradesInventory", StorageMachineUpgradesInventory);

        tag.put("rxj.pw/Storage", Storage);

        //Config
        NbtCompound Config = Configuration.write();
        tag.put("rxj.pw/Config", Config);
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }
    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    public void updateListeners(){
        if(world == null) return;

        world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), Block.NOTIFY_LISTENERS);
    }

    //Screen Handler
    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new QuarryBlockScreenHandler(syncId, playerInventory, OutputInventory, BatteryInputInventory, MachineUpgradesInventory, BlueprintInventory, DrillInventory, EnergyContainer, Configuration, this.getQuarryBlock());
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable(this.getCachedState().getBlock().getTranslationKey());
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buffer) {
        buffer.writeIdentifier(Registries.BLOCK.getId(this.getQuarryBlock()));
        buffer.writeBlockPos(pos);
    }
}
