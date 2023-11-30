package pw.rxj.iron_quarry.blockentities;

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
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.blocks.QuarryBlock;
import pw.rxj.iron_quarry.interfaces.BasicMachineLogic;
import pw.rxj.iron_quarry.items.BlueprintItem;
import pw.rxj.iron_quarry.screenhandler.QuarryBlockScreenHandler;
import pw.rxj.iron_quarry.types.Face;
import pw.rxj.iron_quarry.types.IoState;
import pw.rxj.iron_quarry.util.*;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;

import java.util.ArrayList;
import java.util.List;

public class QuarryBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, BasicMachineLogic {
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

    @Override
    public void markRemoved() {
        if(world == null) return;
        if(world.isClient) return;

        ChunkLoadingManager.removeTickets((ServerWorld) this.world, this.pos);

        super.markRemoved();
    }

    //Machine Configuration
    public MachineConfiguration Configuration = new MachineConfiguration() {
        @Override
        public void markDirty() {
            QuarryBlockEntity.this.markDirty();
            QuarryBlockEntity.this.updateListeners();
        }

        @Override
        public void onIoUpdated(Face face, IoState ioState) {
            if(world == null) return;

            BlockState blockState = getCachedState();

            world.setBlockState(pos, blockState.with(QuarryBlock.getFacingProperty(face), ioState != IoState.BLOCKED));
        }

        @Override
        public IndexedMap<IoState, Object> getUsedIoStates() {
            IndexedMap<IoState, Object> usedIo = new IndexedMap<>();
            usedIo.put(IoState.BLOCKED, null);
            usedIo.put(IoState.ORANGE, OutputInventory);

            return usedIo;
        }
    };

    @Override
    public MachineConfiguration getMachineConfiguration() {
        return Configuration;
    }

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
        if (world == null) return;
        if (world.isClient) return;

        long chargeEnergy = Math.min(EnergyContainer.getFreeSpace(), EnergyContainer.getMaxInsert(null));
        if (chargeEnergy <= 0) return;

        if(BatteryInputInventory.getStack(0).isEmpty()) return;

        EnergyStorageUtil.move(
                ContainerItemContext.ofSingleSlot(InventoryStorage.of(BatteryInputInventory, null).getSlot(0)).find(EnergyStorage.ITEM),
                EnergyContainer.getSideStorage(null),
                Long.MAX_VALUE,
                null
        );
    }

    //Item Handling
    public final ComplexInventory OutputInventory = new ComplexInventory(18) {
        @Override
        public void markDirty() {
            QuarryBlockEntity.this.markDirty();
        }
    };
    public final ComplexInventory BatteryInputInventory = new ComplexInventory(1) {
        @Override
        public void markDirty() {
            QuarryBlockEntity.this.markDirty();
        }
    };
    public final ComplexInventory MachineUpgradesInventory = new ComplexInventory(6){
        @Override
        public void markDirty() {
            QuarryBlockEntity.this.markDirty();
        }

        @Override
        public int getMaxCountPerStack() {
            return 1;
        }
    };
    public final ComplexInventory BlueprintInventory = new ComplexInventory(1){
        @Override
        public void markDirty() {
            QuarryBlockEntity.this.markDirty();
        }
    };

    public InventoryStorage getItemSideConfiguration(Direction direction){
        if(direction == null) return null;

        Face face = Face.from(direction, getCachedState().get(QuarryBlock.FACING));
        Object object = this.Configuration.getLinkedIo(face);

        if(object instanceof Inventory inventory) {
            return InventoryStorage.of(inventory, null);
        }

        return null;
    }

    public void attemptPushIo(){
        for (Face face: Face.values()) {
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

        Face face = Face.from(direction, getCachedState().get(QuarryBlock.FACING));
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
    private int cooldown = 0;

    public static void tick(World thisWorld, BlockPos thisPos, BlockState thisBlockState, QuarryBlockEntity thisBlockEntity) {
        if (thisWorld == null) return;
        if (thisWorld.isClient) return;

        ServerWorld thisServerWorld = (ServerWorld) thisWorld;
        MinecraftServer minecraftServer = thisServerWorld.getServer();

        QuarryBlock thisBlock = thisBlockEntity.getQuarryBlock();
        if (thisBlock == null) return;

        thisBlockEntity.attemptCharge();
        thisBlockEntity.attemptPushIo();

        ItemStack blueprintStack = thisBlockEntity.BlueprintInventory.getStack(0);
        int blueprintHash = blueprintStack.hashCode();

        if(thisBlockEntity.blueprintHash != blueprintHash) {
            thisBlockEntity.blueprintHash = blueprintHash;
            thisBlockEntity.currentChunk = null;
            thisBlockEntity.MiningQueue.clear();

            ChunkLoadingManager.removeTickets(thisServerWorld, thisPos);
        }

        if (!(blueprintStack.getItem() instanceof BlueprintItem blueprintItem)) return;
        if(!blueprintItem.isSealed(blueprintStack)) return;

        RegistryKey<World> worldRegistryKey = blueprintItem.getWorldRegistryKey(blueprintStack);
        ServerWorld serverWorldToBreak = minecraftServer.getWorld(worldRegistryKey);
        if(serverWorldToBreak == null) return;

        MachineUpgradesUtil upgradesUtil = MachineUpgradesUtil.from(thisBlockEntity.MachineUpgradesInventory);
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

                    if(Math.random() <= messy_threads - threads) threads++;
                } else {
                    if(Math.random() <= operation_leftover) ceiled_tpo--;
                }
            }

            thisBlockEntity.cooldown = ceiled_tpo - 1;
        }

        BlockPos firstPos = blueprintItem.getFirstPos(blueprintStack);
        if(firstPos == null) return;
        BlockPos secondPos = blueprintItem.getSecondPos(blueprintStack);
        if(secondPos == null) return;

        ArrayList<BlockPos> MiningQueue = thisBlockEntity.MiningQueue;

        if(MiningQueue.isEmpty()) {
            if(thisBlockEntity.currentChunk != null) {
                ChunkLoadingManager.removeTicket(serverWorldToBreak, thisBlockEntity.currentChunk, thisServerWorld, thisPos);
                blueprintItem.increaseMinedChunks(blueprintStack);

                thisBlockEntity.currentChunk = null;
            }

            List<ChunkPos> chunkPosList = blueprintItem.getNextChunkPos(blueprintStack, 5);
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

                        if(blockState.isAir()) continue;
                        if(blockState.getBlock().getHardness() < 0) continue;
                        if(ZUtil.isActualBlockEntity(worldChunk, blockState, blockPos)) continue;

                        MiningQueue.add(blockPos);
                    }
                }
            }

            return;
        }

        for (int i = 0; i < threads; i++) {
            if(MiningQueue.isEmpty()) return;

            BlockPos blockPosToBreak = MiningQueue.remove(MiningQueue.size() - 1);
            WorldChunk worldChunkToBreak = serverWorldToBreak.getChunkManager().getWorldChunk(blockPosToBreak.getX() >> 4, blockPosToBreak.getZ() >> 4);

            if(worldChunkToBreak == null) {
                MiningQueue.add(blockPosToBreak); return;
            }

            BlockState blockStateToBreak = worldChunkToBreak.getBlockState(blockPosToBreak);;

            if(!blockStateToBreak.getFluidState().isEmpty()) {
                serverWorldToBreak.setBlockState(blockPosToBreak, Blocks.AIR.getDefaultState(), 2, 0); continue;
            }

            if(blockStateToBreak.isAir()) continue;
            if(blockStateToBreak.getBlock().getHardness() < 0) continue;
            if(ZUtil.isActualBlockEntity(serverWorldToBreak, blockStateToBreak, blockPosToBreak)) continue;

            //Energy
            long actualEnergyConsumption = thisBlock.getActualEnergyConsumption(upgradesUtil, blockStateToBreak.getBlock());

            if(thisBlockEntity.EnergyContainer.getStored() < actualEnergyConsumption) {
                MiningQueue.add(blockPosToBreak); return;
            }

            //Loot
            ItemStack breakingItem = Items.NETHERITE_PICKAXE.getDefaultStack();
            if(upgradesUtil.hasSilkTouch()) {
                breakingItem.addEnchantment(Enchantments.SILK_TOUCH, 1);
            } else {
                int fortuneLevel = FortuneUtil.fromProbability(upgradesUtil.getFortuneMultiplier());
                breakingItem.addEnchantment(Enchantments.FORTUNE, fortuneLevel);
            }

            List<ItemStack> droppingItems = blockStateToBreak.getDroppedStacks(
                    new LootContext.Builder(serverWorldToBreak)
                            .parameter(LootContextParameters.BLOCK_STATE, thisBlockState)
                            .parameter(LootContextParameters.ORIGIN, new Vec3d(thisPos.getX(), thisPos.getY(), thisPos.getZ()))
                            .parameter(LootContextParameters.TOOL, breakingItem)
            );

            List<ItemStack> stacksToAward = new ArrayList<>();

            for (ItemStack droppingItem : droppingItems) {
                if(!thisBlockEntity.OutputInventory.canInsert(droppingItem)) {
                    MiningQueue.add(blockPosToBreak); return;
                } else {
                    stacksToAward.add(droppingItem);
                }
            }

            stacksToAward.forEach(thisBlockEntity.OutputInventory::addStack);

            //Mining
            serverWorldToBreak.setBlockState(blockPosToBreak, Blocks.AIR.getDefaultState(), 2, 0);
            thisBlockEntity.EnergyContainer.useEnergy(actualEnergyConsumption);
        }
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

    private void updateListeners(){
        if(world == null) return;

        world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), Block.NOTIFY_LISTENERS);
    }

    //Screen Handler
    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new QuarryBlockScreenHandler(syncId, playerInventory, OutputInventory, BatteryInputInventory, MachineUpgradesInventory,BlueprintInventory, EnergyContainer, Configuration, this.getQuarryBlock());
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buffer) {
        buffer.writeIdentifier(Registry.BLOCK.getId(this.getQuarryBlock()));
        buffer.writeBlockPos(pos);
    }
}
