package pw.rxj.iron_quarry.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.block.QuarryBlock;
import pw.rxj.iron_quarry.block.ZBlockTags;
import pw.rxj.iron_quarry.compat.Compat;
import pw.rxj.iron_quarry.compat.lootr.LootrCompat;
import pw.rxj.iron_quarry.item.BlueprintItem;
import pw.rxj.iron_quarry.item.DrillItem;
import pw.rxj.iron_quarry.network.PacketQuarryBlockBreak;
import pw.rxj.iron_quarry.network.ZNetwork;
import pw.rxj.iron_quarry.types.MiningPattern;
import pw.rxj.iron_quarry.util.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.IntPredicate;
import java.util.function.Supplier;

public class QuarryMiner {
    private static final int SCAN_BLOCKS_PER_TICK = 10_000;
    private static final int BLOCKED_RETRY_TICKS = 100;

    private final QuarryBlockEntity ref;

    private QuarryMiner(QuarryBlockEntity ref) {
        this.ref = ref;
    }
    public static QuarryMiner ofRef(QuarryBlockEntity ref) {
        return new QuarryMiner(ref);
    }

    //World Ticking
    private List<ChunkPos> loadedChunks = List.of();
    private @Nullable ChunkPos currentChunk;
    private int blueprintHash = 0;
    private int drillHash = 0;
    private int cooldown = 0;
    private int blockedCooldown = 0;

    void tick(ServerLevel quarryWorld, BlockPos quarryPos, BlockState quarryBlockState, QuarryBlock quarryBlock) {
        QuarryBlockEntity quarryBlockEntity = this.ref;

        ItemStack blueprintStack = quarryBlockEntity.BlueprintInventory.getItem(0);
        ItemStack drillStack = quarryBlockEntity.DrillInventory.getItem(0);
        this.resetIfDrillOrBlueprintChanged(blueprintStack, drillStack);

        if(quarryBlockEntity.getOwner() == null) return;

        if(!(drillStack.getItem() instanceof DrillItem drillItem)) return;
        if(!(blueprintStack.getItem() instanceof BlueprintItem blueprintItem)) return;
        if(!blueprintItem.isSealed(blueprintStack) || blueprintItem.allChunksMined(blueprintStack)) return;

        BlockPos firstPos = blueprintItem.getFirstPos(blueprintStack).orElse(null);
        if(firstPos == null) return;
        BlockPos secondPos = blueprintItem.getSecondPos(blueprintStack).orElse(null);
        if(secondPos == null) return;

        ResourceKey<Level> worldRegistryKey = blueprintItem.getWorldRegistryKey(blueprintStack).orElse(null);
        ServerLevel serverWorldToBreak = quarryWorld.getServer().getLevel(worldRegistryKey);
        if(serverWorldToBreak == null) return;

        MachineExpansionsUtil expansionsUtil = MachineExpansionsUtil.from(quarryBlockEntity);
        if(!expansionsUtil.canMineIn(serverWorldToBreak.dimension())) return;

        ChunkLoadingManager.keepLoaded(serverWorldToBreak, this.loadedChunks);

        MachineUpgradesUtil upgradesUtil = MachineUpgradesUtil.from(quarryBlockEntity);
        long minimumEnergyConsumption = quarryBlock.getMinEnergyConsumption(upgradesUtil);
        if(quarryBlockEntity.EnergyContainer.getStored() < minimumEnergyConsumption) return;

        if(this.blockedCooldown > 0) {
            this.blockedCooldown--; return;
        }
        if(this.cooldown > 0) {
            this.cooldown--; return;
        }

        int threads = this.rollThreads(quarryBlock, upgradesUtil, quarryWorld.getRandom());

        ChunkPos currentChunk = this.prepareCurrentChunk(serverWorldToBreak, quarryPos, blueprintStack, blueprintItem, expansionsUtil);
        if(currentChunk == null) return;

        LevelChunk worldChunk = serverWorldToBreak.getChunkSource().getChunkNow(currentChunk.x(), currentChunk.z());
        if(worldChunk == null) {
            this.cooldown = 0; return;
        }

        if(!this.fillMiningQueue(worldChunk, threads, upgradesUtil)) {
            this.cooldown = 0; return;
        }

        List<BlockPosState> drillResults = this.drillQueue(threads, quarryBlock, quarryBlockState, quarryPos, serverWorldToBreak, drillStack, drillItem, upgradesUtil);
        if(drillResults.isEmpty()) return;

        sendBlockBreakEffectsToNearbyPlayers(serverWorldToBreak, drillResults);
    }
    private void resetIfDrillOrBlueprintChanged(ItemStack blueprintStack, ItemStack drillStack) {
        int blueprintHash = ItemStack.hashItemAndComponents(blueprintStack);
        int drillHash = drillStack.isEmpty() ? 0 : drillStack.hashCode();
        if(this.blueprintHash == blueprintHash && this.drillHash == drillHash) return;

        this.blueprintHash = blueprintHash;
        this.drillHash = drillHash;

        this.dropCurrentChunk();
        this.ref.setBlocked(null);
        this.blockedCooldown = 0;
        this.miningPos = null;

        this.loadedChunks = List.of();
    }
    private int rollThreads(QuarryBlock quarryBlock, MachineUpgradesUtil upgradesUtil, RandomSource random) {
        int threads = 1;

        float messy_tpo = quarryBlock.getTicksPerOperation() / upgradesUtil.getSpeedMultiplier();
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

        this.cooldown = ceiled_tpo - 1;

        return threads;
    }

    private @Nullable ChunkPos prepareCurrentChunk(ServerLevel serverWorldToBreak, BlockPos quarryPos, ItemStack blueprintStack, BlueprintItem blueprintItem, MachineExpansionsUtil expansionsUtil) {
        if(this.isCurrentChunkFinished()) {
            this.loadedChunks = List.of();

            blueprintItem.increaseMinedChunks(blueprintStack);
            this.blueprintHash = ItemStack.hashItemAndComponents(blueprintStack);
            this.ref.setChanged();

            this.dropCurrentChunk();
        }

        if(this.currentChunk != null && this.miningPattern != expansionsUtil.getMiningPattern(this.currentChunk)) this.dropCurrentChunk();

        if(this.currentChunk == null) {
            List<ChunkPos> chunkPosList = blueprintItem.getNextChunkPos(blueprintStack, 2);
            if(chunkPosList.isEmpty()) return null;

            ChunkPos chunkPos = chunkPosList.getFirst();
            if(chunkPos == null) return null;
            BoundingBox chunkBox = blueprintItem.getChunkBox(blueprintStack, chunkPos).orElse(null);
            if(chunkBox == null) return null;

            this.loadedChunks = List.copyOf(chunkPosList);
            ChunkLoadingManager.keepLoaded(serverWorldToBreak, this.loadedChunks);

            MiningPattern miningPattern = expansionsUtil.getMiningPattern(chunkPos);
            this.miningIterator = miningPattern.create(chunkBox, IMiningIterator.seedOf(quarryPos, chunkPos));
            this.miningPattern = miningPattern;
            this.currentChunk = chunkPos;
        }

        return this.currentChunk;
    }
    private boolean isCurrentChunkFinished() {
        return this.currentChunk != null && this.MiningQueue.isEmpty() && !this.miningIterator.hasNext();
    }
    private void dropCurrentChunk() {
        this.miningIterator = IMiningIterator.EMPTY;
        this.currentChunk = null;
        this.MiningQueue.clear();
    }

    private static void sendBlockBreakEffectsToNearbyPlayers(ServerLevel serverWorldToBreak, List<BlockPosState> drillResults) {
        ZNetwork.sendToAllInLevel(serverWorldToBreak, player -> {
            List<BlockPosState> nearbyDrillResults = drillResults.stream()
                    .filter(drill -> ZUtil.toVec3d(drill.blockPos).distanceTo(player.getEyePosition()) <= 64.0)
                    .toList();

            return nearbyDrillResults.isEmpty() ? null : PacketQuarryBlockBreak.bake(nearbyDrillResults);
        });
    }

    //Mining
    private IMiningIterator miningIterator = IMiningIterator.EMPTY;
    private @Nullable MiningPattern miningPattern;
    private final ArrayDeque<BlockPos> MiningQueue = new ArrayDeque<>();
    private @Nullable BlockPos miningPos;

    public @Nullable BlockPos getMiningPos() {
        return this.miningPos;
    }
    private boolean fillMiningQueue(LevelChunk worldChunk, int target, MachineUpgradesUtil upgradesUtil) {
        IntPredicate hasOnlyAir = y -> ZUtil.chunkSectionHasOnlyAir(worldChunk, y);
        int scanBudget = SCAN_BLOCKS_PER_TICK;

        while(this.MiningQueue.size() < target && scanBudget > 0 && this.miningIterator.hasNext()) {
            scanBudget--;
            if(this.miningIterator.skipAirSection(hasOnlyAir)) continue;

            BlockPos blockPos = this.miningIterator.next();
            BlockState blockState = worldChunk.getBlockState(blockPos);

            if(this.canDrill(worldChunk, blockState, blockPos, upgradesUtil)) {
                this.MiningQueue.addLast(blockPos);
            }
        }

        return !this.MiningQueue.isEmpty();
    }
    private boolean canDrill(BlockActress blockActress, MachineUpgradesUtil upgradesUtil) {
        return this.canDrill(() -> blockActress.blockEntity, blockActress.blockState, upgradesUtil);
    }
    private boolean canDrill(LevelChunk worldChunk, BlockState blockState, BlockPos blockPos, MachineUpgradesUtil upgradesUtil) {
        return this.canDrill(() -> ZUtil.getBlockEntity(worldChunk, blockState, blockPos), blockState, upgradesUtil);
    }

    private boolean canDrill(Supplier<BlockEntity> blockEntitySupplier, BlockState blockState, MachineUpgradesUtil upgradesUtil) {
        if(blockState.isAir() || blockState.is(ZBlockTags.IGNORED_BY_QUARRY)) return false;

        Block block = blockState.getBlock();
        //The block hardness check will render any chest looting augment useless.
        if(block.defaultDestroyTime() < 0) return false;

        BlockEntity blockEntity = blockEntitySupplier.get();
        if(blockEntity == null) return true;

        if(upgradesUtil.hasChestLooting()) {
            Container lootableContainer = this.getLootableContainer(blockEntity);
            return lootableContainer != null && !lootableContainer.isEmpty();
        }

        return false;
    }
    private List<BlockPosState> drillQueue(int threads, QuarryBlock quarryBlock, BlockState quarryBlockState, BlockPos quarryPos, ServerLevel serverWorldToBreak,
                                           ItemStack drillStack, DrillItem drillItem, MachineUpgradesUtil upgradesUtil) {
        List<BlockPosState> drillResults = new ArrayList<>();

        QuarryBlockEntity quarryBlockEntity = this.ref;
        ComplexEnergyContainer EnergyContainer = quarryBlockEntity.EnergyContainer;
        ComplexInventory OutputInventory = quarryBlockEntity.OutputInventory;

        ItemStack toolStack = drillItem.createToolStack();
        Tool toolComponent = drillItem.getToolComponent();

        for (int threadIndex = 0; threadIndex < threads; threadIndex++) {
            if(drillStack.isEmpty()) return drillResults;
            if(this.MiningQueue.isEmpty()) return drillResults;

            BlockPos blockPosToBreak = this.MiningQueue.getFirst();
            Optional<BlockActress> testActress = BlockActress.of(serverWorldToBreak, blockPosToBreak);
            if(testActress.isEmpty()) return drillResults;

            //enables block entity tracking / disables neighbor- and redstone updates to reduce lag!
            BlockActress blockActress = testActress.get().with(true, 0, 2);

            if(!this.canDrill(blockActress, upgradesUtil)) {
                this.MiningQueue.removeFirst();
                continue;
            }

            if(!quarryBlockEntity.canModify(serverWorldToBreak, blockActress)) {
                this.blockedCooldown = BLOCKED_RETRY_TICKS; return drillResults;
            }
            this.miningPos = blockPosToBreak;

            //handle special block entities
            //will only work if canDrill conditions are met! like if the container is not empty
            if(blockActress.blockEntity != null) {
                int lootedItems = this.lootBlockEntity(blockActress.blockEntity, threads - threadIndex, quarryBlock, upgradesUtil);
                //not enough space in quarry inventory
                if(lootedItems == 0) return drillResults;

                //only one item per operation
                threadIndex += lootedItems - 1;
                continue;
            }
            //non-block entities will fall through

            //energy consumption
            long energyConsumption = quarryBlock.getEnergyConsumption(upgradesUtil, blockActress.blockState);
            if(EnergyContainer.getStored() < energyConsumption) return drillResults;

            //loot generation
            ItemStack mockPickaxe = this.createMockPickaxe(toolStack, serverWorldToBreak, upgradesUtil);

            boolean isLiquidBlock = blockActress.isLiquidBlock();

            //only drop item if required mining level is present
            if(!isLiquidBlock && blockActress.isCorrectForDrops(toolComponent)) {
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
            this.MiningQueue.removeFirst();

            if(isLiquidBlock) continue;

            boolean drillBroke = drillItem.cdamage(drillStack, 1, serverWorldToBreak, null);
            if(drillBroke) drillItem.triggerBreakEvent(drillStack, quarryPos.getCenter(), serverWorldToBreak);

            drillResults.add(blockPosState);
        }

        return drillResults;
    }
    private ItemStack createMockPickaxe(ItemStack toolStack, ServerLevel serverWorldToBreak, MachineUpgradesUtil upgradesUtil) {
        ItemStack mockPickaxe = toolStack.copy();

        if(upgradesUtil.hasSilkTouch()) {
            mockPickaxe.enchant(ZUtil.getEnchantmentEntry(serverWorldToBreak.registryAccess(), Enchantments.SILK_TOUCH), 1);
        } else {
            int fortuneLevel = FortuneUtil.rollLevel(upgradesUtil.getFortuneLevel(), serverWorldToBreak.getRandom());
            if(fortuneLevel > 0) mockPickaxe.enchant(ZUtil.getEnchantmentEntry(serverWorldToBreak.registryAccess(), Enchantments.FORTUNE), fortuneLevel);
        }

        return mockPickaxe;
    }

    //Chest Looting
    private @Nullable Container getLootableContainer(BlockEntity blockEntity) {
        RandomizableContainerBlockEntity lootableContainer = ZUtil.getUnlockedLootableContainer(blockEntity);
        if(lootableContainer == null || !Compat.LOOTR_LOADED) return lootableContainer;

        return LootrCompat.getLootableContainer(lootableContainer, () -> this.ref.getOwnerPlayer(lootableContainer.getLevel()));
    }
    private void markLooted(BlockEntity blockEntity) {
        if(!Compat.LOOTR_LOADED) return;

        LootrCompat.markOpened(blockEntity, () -> this.ref.getOwnerPlayer(blockEntity.getLevel()));
    }
    private int lootBlockEntity(BlockEntity blockEntity, int maxItems, QuarryBlock quarryBlock, MachineUpgradesUtil upgradesUtil) {
        Container lootableContainer = this.getLootableContainer(blockEntity);
        if(lootableContainer == null) return 0;

        int lootedItems = this.lootContainer(lootableContainer, maxItems, quarryBlock, upgradesUtil);
        if(lootedItems > 0) this.markLooted(blockEntity);

        return lootedItems;
    }
    private int lootContainer(Container lootableContainer, int maxItems, QuarryBlock quarryBlock, MachineUpgradesUtil upgradesUtil) {
        ComplexEnergyContainer EnergyContainer = this.ref.EnergyContainer;
        ComplexInventory OutputInventory = this.ref.OutputInventory;

        long energyConsumption = quarryBlock.getMinEnergyConsumption(upgradesUtil);
        int lootedItems = 0;

        for (int slot = 0; slot < lootableContainer.getContainerSize() && lootedItems < maxItems; slot++) {
            ItemStack stack = lootableContainer.getItem(slot);
            if(stack.isEmpty()) continue;

            long affordableItems = energyConsumption > 0 ? EnergyContainer.getStored() / energyConsumption : Long.MAX_VALUE;
            int items = (int) Math.min(Math.min(stack.getCount(), maxItems - lootedItems), Math.min(affordableItems, OutputInventory.getInsertableCount(stack)));
            if(items <= 0) break;

            OutputInventory.addItem(lootableContainer.removeItem(slot, items));
            EnergyContainer.useEnergy(energyConsumption * items);
            lootedItems += items;

            if(!lootableContainer.getItem(slot).isEmpty()) break;
        }

        return lootedItems;
    }
}
