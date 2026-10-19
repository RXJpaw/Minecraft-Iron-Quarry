package pw.rxj.iron_quarry.item;

import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipData;
import net.minecraft.client.item.TooltipType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.block.QuarryBlock;
import pw.rxj.iron_quarry.blockentity.QuarryBlockEntity;
import pw.rxj.iron_quarry.blockentity.ZBlockEntities;
import pw.rxj.iron_quarry.data.ZComponents;
import pw.rxj.iron_quarry.gui.ITooltipDataProvider;
import pw.rxj.iron_quarry.interfaces.IHandledCrafting;
import pw.rxj.iron_quarry.interfaces.ITickingInventoryItem;
import pw.rxj.iron_quarry.resource.config.server.QuarryMonitorConfig;
import pw.rxj.iron_quarry.util.ComplexInventory;
import pw.rxj.iron_quarry.util.ReadableString;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.List;
import java.util.Optional;

public class QuarryMonitorItem extends Item implements ITickingInventoryItem, ITooltipDataProvider, IHandledCrafting {
    private boolean interdimensional;
    private int range;

    public QuarryMonitorItem(Settings settings, QuarryMonitorConfig.Entry entry) {
        super(settings);

        this.interdimensional = entry.interdimensional;
        this.range = entry.range;
    }
    public void override(QuarryMonitorConfig.Entry entry) {
        this.interdimensional = entry.interdimensional;
        this.range = entry.range;
    }

    @Override
    public Text getName(ItemStack stack) {
        long timestamp = this.getCachedTimestamp(stack);

        if(Screen.hasShiftDown() && timestamp > 0) {
            ItemStack quarryBlockStack = this.getCachedQuarryStack(stack);

            if(!quarryBlockStack.isEmpty()) {
                return quarryBlockStack.getName();
            } else {
                return ReadableString.translatable("item.iron_quarry.quarry_monitor.name.unknown_quarry_type");
            }
        }

        return super.getName(stack);
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        ItemStack blueprintStack = this.getCachedBlueprintStack(stack);
        if(blueprintStack.isEmpty()) return super.isItemBarVisible(stack);

        return blueprintStack.isItemBarVisible();
    }
    @Override
    public int getItemBarColor(ItemStack stack) {
        ItemStack blueprintStack = this.getCachedBlueprintStack(stack);
        if(blueprintStack.isEmpty()) return super.getItemBarColor(stack);

        return blueprintStack.getItemBarColor();
    }
    @Override
    public int getItemBarStep(ItemStack stack) {
        ItemStack blueprintStack = this.getCachedBlueprintStack(stack);
        if(blueprintStack.isEmpty()) return super.getItemBarStep(stack);

        return blueprintStack.getItemBarStep();
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        ItemStack quarryBlockStack = this.getCachedQuarryStack(stack);

        if(ZUtil.getBlockOrItem(quarryBlockStack) instanceof QuarryBlock quarryBlock) {
            return quarryBlock.getTooltipData(quarryBlockStack);
        }

        return Optional.empty();
    }
    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType options) {
        ItemStack quarryBlockStack = this.getCachedQuarryStack(stack);
        long timestamp = this.getCachedTimestamp(stack);

        if(Screen.hasShiftDown() && timestamp > 0) {
            if(ZUtil.getBlockOrItem(quarryBlockStack) instanceof QuarryBlock quarryBlock) {
                quarryBlock.appendTooltip(quarryBlockStack, context, tooltip, options);
            } else {
                tooltip.add(ReadableString.translatable("item.iron_quarry.quarry_monitor.lore.status.unknown"));
            }

        } else {
            tooltip.add(ReadableString.translatable("item.iron_quarry.quarry_monitor.lore.range", ReadableString.intFrom(range)));

            if(timestamp > 0) {
                tooltip.add(ReadableString.translatable("item.iron_quarry.quarry_monitor.lore.last_updated", ReadableString.toTimeAgo(timestamp)));

                int warningFlag = this.getWarningFlag(stack);
                if(QuarryMonitorItem.hasWarnings(warningFlag)) {
                    tooltip.add(ReadableString.space());

                    if(QuarryMonitorItem.hasWarning(warningFlag, QuarryMonitorItem.WARNING_QUARRY_UNKNOWN)) {
                        tooltip.add(ReadableString.translatable("item.iron_quarry.warning.quarry_status_unknown"));
                    } else {
                        if(QuarryMonitorItem.hasWarning(warningFlag, QuarryMonitorItem.WARNING_ENERGY_10)) {
                            tooltip.add(ReadableString.translatable("item.iron_quarry.warning.quarry_energy_10"));
                        } else if(QuarryMonitorItem.hasWarning(warningFlag, QuarryMonitorItem.WARNING_ENERGY_30)) {
                            tooltip.add(ReadableString.translatable("item.iron_quarry.warning.quarry_energy_30"));
                        }

                        if(QuarryMonitorItem.hasWarning(warningFlag, QuarryMonitorItem.WARNING_INV_90)) {
                            tooltip.add(ReadableString.translatable("item.iron_quarry.warning.quarry_inv_90"));
                        } else if(QuarryMonitorItem.hasWarning(warningFlag, QuarryMonitorItem.WARNING_INV_70)) {
                            tooltip.add(ReadableString.translatable("item.iron_quarry.warning.quarry_inv_70"));
                        }

                        if(QuarryMonitorItem.hasWarning(warningFlag, QuarryMonitorItem.WARNING_DRILL_EMPTY)) {
                            tooltip.add(ReadableString.translatable("item.iron_quarry.warning.quarry_drill_empty"));
                        }
                    }
                }

                tooltip.add(ReadableString.space());
                tooltip.add(ReadableString.translatable("item.iron_quarry.quarry_monitor.lore.details"));
            } else {
                tooltip.add(ReadableString.translatable("item.iron_quarry.quarry_monitor.lore.never_updated"));
            }
        }
    }

    @Override
    public ItemStack getCraftingOutput(ShapedRecipe handler, RecipeInputInventory recipeInputInventory, RegistryWrapper.WrapperLookup wrapperLookup) {
        return IHandledCrafting.COPY_CENTER_SLOT_NBT.apply(handler, recipeInputInventory, wrapperLookup);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        if(player == null) return ActionResult.FAIL;

        if(!context.getHand().equals(Hand.MAIN_HAND)) return ActionResult.PASS;

        World world = context.getWorld();
        BlockPos targetedPos = context.getBlockPos();
        if(world.getBlockState(targetedPos).getBlock() instanceof QuarryBlock) {
            ItemStack stack = context.getStack();
            this.setPosition(stack, world, targetedPos);
            Text positionText = ReadableString.textFrom(targetedPos).orElse(ReadableString.ERROR);

            player.sendMessage(ReadableString.translatable("item.iron_quarry.quarry_monitor.overlay.pos_set", positionText), true);

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return TypedActionResult.success(user.getStackInHand(hand), false);
    }

    public void setWorld(ItemStack stack, World world) {
        this.setWorld(stack, world.getRegistryKey());
    }
    public void setWorld(ItemStack stack, RegistryKey<World> newWorldKey) {
        RegistryKey<World> oldWorldKey = this.getWorldRegistryKey(stack).orElse(null);
        if(ZUtil.equals(oldWorldKey, newWorldKey)) return;

        this.resetPosition(stack);

        stack.set(ZComponents.QUARRY_MONITOR_WORLD, newWorldKey);

    }
    public Optional<RegistryKey<World>> getWorldRegistryKey(ItemStack stack) {
        return Optional.ofNullable(stack.get(ZComponents.QUARRY_MONITOR_WORLD));
    }

    public void resetPosition(ItemStack stack) {
        stack.remove(ZComponents.QUARRY_MONITOR_POSITION);

    }
    private void setPosition(ItemStack stack, @Nullable World world, BlockPos blockPos) {
        if(world != null) {
            this.setWorld(stack, world);
            blockPos = ZUtil.limitInsideWorldBounds(blockPos, world);
        } else if(this.getWorldRegistryKey(stack).isEmpty()) {
            throw new IllegalStateException("QuarryItemMonitor#setWorld has to be called first if no world is supplied.");
        }

        stack.set(ZComponents.QUARRY_MONITOR_POSITION, blockPos);
    }
    public @Nullable BlockPos getPosition(ItemStack stack) {
        return stack.get(ZComponents.QUARRY_MONITOR_POSITION);
    }

    @Override
    public void tick(ServerPlayerEntity playerEntity, ServerWorld world, ItemStack stack) {
        MinecraftServer minecraftServer = world.getServer();

        if(ZUtil.getBlockOrItem(stack) instanceof QuarryMonitorItem quarryMonitorItem) {
            if(System.currentTimeMillis() - quarryMonitorItem.getCachedTimestamp(stack) < 1000L) return;

            QuarryBlockEntity quarryBlockEntity = quarryMonitorItem.fetchQuarryBlockEntity(minecraftServer, stack);

            if(quarryBlockEntity == null) {
                quarryMonitorItem.clearCachedQuarry(stack);
            } else {
                quarryMonitorItem.setCachedQuarry(stack, quarryBlockEntity, world.getRegistryManager());
            }
        }
    }

    public @Nullable QuarryBlockEntity fetchQuarryBlockEntity(MinecraftServer minecraftServer, ItemStack stack) {
        RegistryKey<World> worldRegistryKey = this.getWorldRegistryKey(stack).orElse(null);
        ServerWorld serverWorld = minecraftServer.getWorld(worldRegistryKey);
        if(serverWorld == null) return null;

        BlockPos quarryPos = this.getPosition(stack);
        if(quarryPos == null) return null;

        ChunkPos chunkPos = new ChunkPos(quarryPos.getX() >> 4, quarryPos.getZ() >> 4);

        WorldChunk worldChunk = serverWorld.getChunkManager().getWorldChunk(chunkPos.x, chunkPos.z);
        if(worldChunk == null) return null;

        Optional<QuarryBlockEntity> quarryBlockEntity = worldChunk.getBlockEntity(quarryPos, ZBlockEntities.QUARRY_BLOCK_ENTITY);
        return quarryBlockEntity.orElse(null);

    }

    public void setCachedQuarry(ItemStack stack, QuarryBlockEntity blockEntity, RegistryWrapper.WrapperLookup registryLookup) {
        QuarryBlock quarryBlock = blockEntity.getQuarryBlock();
        if(quarryBlock == null) return;

        ItemStack itemStack = new ItemStack(quarryBlock);
        blockEntity.setStackNbt(itemStack, registryLookup);

        stack.set(ZComponents.QUARRY_MONITOR_CACHED_QUARRY, itemStack);
        stack.set(ZComponents.QUARRY_MONITOR_CACHED_TIMESTAMP, System.currentTimeMillis());
    }

    private @NotNull ItemStack getCachedQuarryStack(ItemStack stack) {
        return stack.getOrDefault(ZComponents.QUARRY_MONITOR_CACHED_QUARRY, ItemStack.EMPTY);
    }
    public boolean exists(ItemStack stack) {
        ItemStack quarryStack = this.getCachedQuarryStack(stack);

        return !quarryStack.isEmpty();
    }
    public void clearCachedQuarry(ItemStack stack) {
        ItemStack quarryStack = this.getCachedQuarryStack(stack);
        if(quarryStack.isEmpty()) return;

        stack.remove(ZComponents.QUARRY_MONITOR_CACHED_QUARRY);
        stack.set(ZComponents.QUARRY_MONITOR_CACHED_TIMESTAMP, System.currentTimeMillis());
    }
    private @Nullable QuarryBlock getCachedQuarryBlock(ItemStack quarryStack) {
        if(quarryStack.isEmpty()) return null;

        Block block = Block.getBlockFromItem(quarryStack.getItem());

        if(block instanceof QuarryBlock quarryBlock) {
            return quarryBlock;
        }

        return null;
    }

    public long getCachedTimestamp(ItemStack stack) {
        return stack.getOrDefault(ZComponents.QUARRY_MONITOR_CACHED_TIMESTAMP, 0L);
    }
    public float getCachedEnergyPct(ItemStack stack) {
        ItemStack quarryStack = this.getCachedQuarryStack(stack);
        if(quarryStack.isEmpty()) return 0.0F;

        QuarryBlock quarryBlock = this.getCachedQuarryBlock(quarryStack);
        if(quarryBlock == null) return 0.0F;

        return (float) quarryBlock.getEnergyStored(quarryStack) / quarryBlock.getEnergyCapacity();
    }
    public float getCachedOutputInvPct(ItemStack stack) {
        ItemStack quarryStack = this.getCachedQuarryStack(stack);
        if(quarryStack.isEmpty()) return 0.0F;

        QuarryBlock quarryBlock = this.getCachedQuarryBlock(quarryStack);
        if(quarryBlock == null) return 0.0F;

        ComplexInventory outputInventory = quarryBlock.getOutputInventory(quarryStack);

        return (float) outputInventory.occupiedSlots() / outputInventory.size();
    }
    public ItemStack getCachedBlueprintStack(ItemStack stack) {
        ItemStack quarryStack = this.getCachedQuarryStack(stack);
        if(quarryStack.isEmpty()) return ItemStack.EMPTY;

        QuarryBlock quarryBlock = this.getCachedQuarryBlock(quarryStack);
        if(quarryBlock == null) return ItemStack.EMPTY;

        return quarryBlock.getBlueprintStack(quarryStack);
    }
    public ItemStack getCachedDrillStack(ItemStack stack) {
        ItemStack quarryStack = this.getCachedQuarryStack(stack);
        if(quarryStack.isEmpty()) return ItemStack.EMPTY;

        QuarryBlock quarryBlock = this.getCachedQuarryBlock(quarryStack);
        if(quarryBlock == null) return ItemStack.EMPTY;

        return quarryBlock.getDrillStack(quarryStack);
    }

    //warnings

    public static final int WARNING_QUARRY_UNKNOWN = 0;
    public static final int WARNING_INV_70 = 1;
    public static final int WARNING_INV_90 = 2;
    public static final int WARNING_ENERGY_30 = 3;
    public static final int WARNING_ENERGY_10 = 4;
    public static final int WARNING_DRILL_EMPTY = 5;

    public static boolean hasWarnings(int flag) {
        return flag != 0;
    }
    public static boolean hasWarning(int flag, int warning) {
        warning = 1 << warning;

        return (flag & warning) == warning;
    }

    public boolean[] getWarningList(ItemStack stack) {
        float invPct = this.getCachedOutputInvPct(stack);
        float energyPct = this.getCachedEnergyPct(stack);
        boolean hasDrillStack = this.getCachedDrillStack(stack).getItem() instanceof DrillItem;
        ItemStack quarryBlockStack = this.getCachedQuarryStack(stack);

        return new boolean[] { quarryBlockStack.isEmpty(), invPct >= 0.7F, invPct >= 0.9F, energyPct <= 0.3F, energyPct <= 0.1F, !hasDrillStack };
    }
    public int getWarningAmount(ItemStack stack) {
        int amount = 0;

        for (boolean hasConflict : this.getWarningList(stack)) {
            if(hasConflict) amount++;
        }

        return amount;
    }
    public int getWarningFlag(ItemStack stack) {
        boolean[] list = this.getWarningList(stack);
        int flag = 0;

        if(list[WARNING_QUARRY_UNKNOWN]) flag += 1 << WARNING_QUARRY_UNKNOWN;
        if(list[WARNING_INV_70]) flag += 1 << WARNING_INV_70;
        if(list[WARNING_INV_90]) flag += 1 << WARNING_INV_90;
        if(list[WARNING_ENERGY_30]) flag += 1 << WARNING_ENERGY_30;
        if(list[WARNING_ENERGY_10]) flag += 1 << WARNING_ENERGY_10;
        if(list[WARNING_DRILL_EMPTY]) flag += 1 << WARNING_DRILL_EMPTY;

        return flag;
    }
}
