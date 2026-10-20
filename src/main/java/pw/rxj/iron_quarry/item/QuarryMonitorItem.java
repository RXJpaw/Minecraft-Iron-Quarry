package pw.rxj.iron_quarry.item;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
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

import java.util.Optional;
import java.util.function.Consumer;

public class QuarryMonitorItem extends Item implements ITickingInventoryItem, ITooltipDataProvider, IHandledCrafting {
    private boolean interdimensional;
    private int range;

    public QuarryMonitorItem(Properties settings, QuarryMonitorConfig.Entry entry) {
        super(settings);

        this.interdimensional = entry.interdimensional;
        this.range = entry.range;
    }
    public void override(QuarryMonitorConfig.Entry entry) {
        this.interdimensional = entry.interdimensional;
        this.range = entry.range;
    }

    @Override
    public Component getName(ItemStack stack) {
        long timestamp = this.getCachedTimestamp(stack);

        if(Minecraft.getInstance().hasShiftDown() && timestamp > 0) {
            ItemStack quarryBlockStack = this.getCachedQuarryStack(stack);

            if(!quarryBlockStack.isEmpty()) {
                return quarryBlockStack.getHoverName();
            } else {
                return ReadableString.translatable("item.iron_quarry.quarry_monitor.name.unknown_quarry_type");
            }
        }

        return super.getName(stack);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        ItemStack blueprintStack = this.getCachedBlueprintStack(stack);
        if(blueprintStack.isEmpty()) return super.isBarVisible(stack);

        return blueprintStack.isBarVisible();
    }
    @Override
    public int getBarColor(ItemStack stack) {
        ItemStack blueprintStack = this.getCachedBlueprintStack(stack);
        if(blueprintStack.isEmpty()) return super.getBarColor(stack);

        return blueprintStack.getBarColor();
    }
    @Override
    public int getBarWidth(ItemStack stack) {
        ItemStack blueprintStack = this.getCachedBlueprintStack(stack);
        if(blueprintStack.isEmpty()) return super.getBarWidth(stack);

        return blueprintStack.getBarWidth();
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        ItemStack quarryBlockStack = this.getCachedQuarryStack(stack);

        if(ZUtil.getBlockOrItem(quarryBlockStack) instanceof QuarryBlock quarryBlock) {
            return quarryBlock.getTooltipImage(quarryBlockStack);
        }

        return Optional.empty();
    }
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag options) {
        ItemStack quarryBlockStack = this.getCachedQuarryStack(stack);
        long timestamp = this.getCachedTimestamp(stack);

        if(Minecraft.getInstance().hasShiftDown() && timestamp > 0) {
            if(ZUtil.getBlockOrItem(quarryBlockStack) instanceof QuarryBlock quarryBlock) {
                quarryBlock.appendTooltip(quarryBlockStack, context, textConsumer, options);
            } else {
                textConsumer.accept(ReadableString.translatable("item.iron_quarry.quarry_monitor.lore.status.unknown"));
            }

        } else {
            textConsumer.accept(ReadableString.translatable("item.iron_quarry.quarry_monitor.lore.range", ReadableString.intFrom(range)));

            if(timestamp > 0) {
                textConsumer.accept(ReadableString.translatable("item.iron_quarry.quarry_monitor.lore.last_updated", ReadableString.toTimeAgo(timestamp)));

                int warningFlag = this.getWarningFlag(stack);
                if(QuarryMonitorItem.hasWarnings(warningFlag)) {
                    textConsumer.accept(ReadableString.space());

                    if(QuarryMonitorItem.hasWarning(warningFlag, QuarryMonitorItem.WARNING_QUARRY_UNKNOWN)) {
                        textConsumer.accept(ReadableString.translatable("item.iron_quarry.warning.quarry_status_unknown"));
                    } else {
                        if(QuarryMonitorItem.hasWarning(warningFlag, QuarryMonitorItem.WARNING_ENERGY_10)) {
                            textConsumer.accept(ReadableString.translatable("item.iron_quarry.warning.quarry_energy_10"));
                        } else if(QuarryMonitorItem.hasWarning(warningFlag, QuarryMonitorItem.WARNING_ENERGY_30)) {
                            textConsumer.accept(ReadableString.translatable("item.iron_quarry.warning.quarry_energy_30"));
                        }

                        if(QuarryMonitorItem.hasWarning(warningFlag, QuarryMonitorItem.WARNING_INV_90)) {
                            textConsumer.accept(ReadableString.translatable("item.iron_quarry.warning.quarry_inv_90"));
                        } else if(QuarryMonitorItem.hasWarning(warningFlag, QuarryMonitorItem.WARNING_INV_70)) {
                            textConsumer.accept(ReadableString.translatable("item.iron_quarry.warning.quarry_inv_70"));
                        }

                        if(QuarryMonitorItem.hasWarning(warningFlag, QuarryMonitorItem.WARNING_DRILL_EMPTY)) {
                            textConsumer.accept(ReadableString.translatable("item.iron_quarry.warning.quarry_drill_empty"));
                        }
                    }
                }

                textConsumer.accept(ReadableString.space());
                textConsumer.accept(ReadableString.translatable("item.iron_quarry.quarry_monitor.lore.details"));
            } else {
                textConsumer.accept(ReadableString.translatable("item.iron_quarry.quarry_monitor.lore.never_updated"));
            }
        }
    }

    @Override
    public ItemStack getCraftingOutput(ItemStack output, CraftingInput input) {
        return IHandledCrafting.copyCenterSlotComponents(output, input);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if(player == null) return InteractionResult.FAIL;

        if(!context.getHand().equals(InteractionHand.MAIN_HAND)) return InteractionResult.PASS;

        Level world = context.getLevel();
        BlockPos targetedPos = context.getClickedPos();
        if(world.getBlockState(targetedPos).getBlock() instanceof QuarryBlock) {
            ItemStack stack = context.getItemInHand();
            this.setPosition(stack, world, targetedPos);
            Component positionText = ReadableString.textFrom(targetedPos).orElse(ReadableString.ERROR);

            player.sendOverlayMessage(ReadableString.translatable("item.iron_quarry.quarry_monitor.overlay.pos_set", positionText));

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        return InteractionResult.CONSUME;
    }

    public void setWorld(ItemStack stack, Level world) {
        this.setWorld(stack, world.dimension());
    }
    public void setWorld(ItemStack stack, ResourceKey<Level> newWorldKey) {
        ResourceKey<Level> oldWorldKey = this.getWorldRegistryKey(stack).orElse(null);
        if(ZUtil.equals(oldWorldKey, newWorldKey)) return;

        this.resetPosition(stack);

        stack.set(ZComponents.QUARRY_MONITOR_WORLD, newWorldKey);

    }
    public Optional<ResourceKey<Level>> getWorldRegistryKey(ItemStack stack) {
        return Optional.ofNullable(stack.get(ZComponents.QUARRY_MONITOR_WORLD));
    }

    public void resetPosition(ItemStack stack) {
        stack.remove(ZComponents.QUARRY_MONITOR_POSITION);

    }
    private void setPosition(ItemStack stack, @Nullable Level world, BlockPos blockPos) {
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
    public void tick(ServerPlayer playerEntity, ServerLevel world, ItemStack stack) {
        MinecraftServer minecraftServer = world.getServer();

        if(ZUtil.getBlockOrItem(stack) instanceof QuarryMonitorItem quarryMonitorItem) {
            if(System.currentTimeMillis() - quarryMonitorItem.getCachedTimestamp(stack) < 1000L) return;

            QuarryBlockEntity quarryBlockEntity = quarryMonitorItem.fetchQuarryBlockEntity(minecraftServer, stack);

            if(quarryBlockEntity == null) {
                quarryMonitorItem.clearCachedQuarry(stack);
            } else {
                quarryMonitorItem.setCachedQuarry(stack, quarryBlockEntity, world.registryAccess());
            }
        }
    }

    public @Nullable QuarryBlockEntity fetchQuarryBlockEntity(MinecraftServer minecraftServer, ItemStack stack) {
        ResourceKey<Level> worldRegistryKey = this.getWorldRegistryKey(stack).orElse(null);
        ServerLevel serverWorld = minecraftServer.getLevel(worldRegistryKey);
        if(serverWorld == null) return null;

        BlockPos quarryPos = this.getPosition(stack);
        if(quarryPos == null) return null;

        ChunkPos chunkPos = new ChunkPos(quarryPos.getX() >> 4, quarryPos.getZ() >> 4);

        LevelChunk worldChunk = serverWorld.getChunkSource().getChunkNow(chunkPos.x(), chunkPos.z());
        if(worldChunk == null) return null;

        Optional<QuarryBlockEntity> quarryBlockEntity = worldChunk.getBlockEntity(quarryPos, ZBlockEntities.QUARRY_BLOCK_ENTITY);
        return quarryBlockEntity.orElse(null);

    }

    public void setCachedQuarry(ItemStack stack, QuarryBlockEntity blockEntity, HolderLookup.Provider registryLookup) {
        QuarryBlock quarryBlock = blockEntity.getQuarryBlock();
        if(quarryBlock == null) return;

        ItemStack itemStack = new ItemStack(quarryBlock);
        itemStack.applyComponents(blockEntity.collectComponents());

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

        Block block = Block.byItem(quarryStack.getItem());

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

        return (float) outputInventory.occupiedSlots() / outputInventory.getContainerSize();
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
