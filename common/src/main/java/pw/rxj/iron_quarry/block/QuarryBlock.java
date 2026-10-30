package pw.rxj.iron_quarry.block;

import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.blockentity.QuarryBlockEntity;
import pw.rxj.iron_quarry.blockentity.ZBlockEntities;
import pw.rxj.iron_quarry.data.QuarryBlockScreenData;
import pw.rxj.iron_quarry.gui.CustomTooltipData;
import pw.rxj.iron_quarry.gui.ITooltipDataProvider;
import pw.rxj.iron_quarry.gui.TooltipQuarryInventoryData;
import pw.rxj.iron_quarry.interfaces.IBlockTooltipAppender;
import pw.rxj.iron_quarry.interfaces.IEnergyContainer;
import pw.rxj.iron_quarry.interfaces.IHandledBlockUse;
import pw.rxj.iron_quarry.item.QuarryMonitorItem;
import pw.rxj.iron_quarry.item.ZItemTags;
import pw.rxj.iron_quarry.platform.Platform;
import pw.rxj.iron_quarry.records.TexturePosition;
import pw.rxj.iron_quarry.resource.ResourceReloadListener;
import pw.rxj.iron_quarry.resource.config.server.QuarryStatsConfig;
import pw.rxj.iron_quarry.types.Face;
import pw.rxj.iron_quarry.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class QuarryBlock extends BaseEntityBlock implements IEnergyContainer, ITooltipDataProvider, IHandledBlockUse, IBlockTooltipAppender {
    private final String textureReference;
    private int ticksPerOperation;
    private int baseConsumption;
    private int energyCapacity;
    private int augmentLimit;

    public QuarryBlock(Properties settings, String texRef, QuarryStatsConfig.Entry entry) {
        super(settings);
        this.textureReference = texRef;

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
        );

        this.ticksPerOperation = entry.ticksPerOperation;
        this.baseConsumption = entry.baseConsumption;
        this.energyCapacity = entry.energyCapacity;
        this.augmentLimit = entry.augmentLimit;
    }
    public void override(QuarryStatsConfig.Entry quarryStats) {
        this.ticksPerOperation = quarryStats.ticksPerOperation;
        this.baseConsumption = quarryStats.baseConsumption;
        this.energyCapacity = quarryStats.energyCapacity;
        this.augmentLimit = quarryStats.augmentLimit;
    }

    public static final EnumProperty<Direction> FACING = EnumProperty.create("facing", Direction.class);

    public static boolean isOf(ItemStack stack){
        return ZUtil.getBlockOrItem(stack) instanceof QuarryBlock;
    }
    public static boolean isNotOf(ItemStack stack){
        return !isOf(stack);
    }

    public int getAugmentLimit(){
        return this.augmentLimit;
    }

    public @NotNull MachineConfiguration getMachineConfiguration(ItemStack stack) {
        return stack.getOrDefault(MachineConfiguration.COMPONENT, MachineConfiguration.empty());
    }
    public @NotNull ComplexInventory getAugmentInventory(ItemStack stack) {
        ComplexInventory augmentInventory = QuarryInventory.MachineUpgrades.noRef();
        augmentInventory.read(stack.get(QuarryInventory.MachineUpgrades.COMPONENT));

        return augmentInventory;
    }
    public @NotNull ComplexInventory getOutputInventory(ItemStack stack) {
        ComplexInventory outputInventory = QuarryInventory.Output.noRef();
        outputInventory.read(stack.get(QuarryInventory.Output.COMPONENT));

        return outputInventory;
    }
    public List<ItemStack> getMachineUpgradeStacks(ItemStack stack) {
        List<ItemStack> stacks = new ArrayList<>();

        stacks.add(this.getDrillStack(stack));
        stacks.addAll(this.getAugmentInventory(stack).getItems());

        return stacks;
    }

    public @NotNull ItemStack getDrillStack(ItemStack stack) {
        return stack.getOrDefault(QuarryInventory.Drill.COMPONENT, ItemContainerContents.EMPTY).copyOne();
    }
    public @NotNull ItemStack getBlueprintStack(ItemStack stack) {
        return stack.getOrDefault(QuarryInventory.Blueprint.COMPONENT, ItemContainerContents.EMPTY).copyOne();
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return Minecraft.getInstance().hasShiftDown() ? Optional.of(TooltipQuarryInventoryData.from(
                this.getBlueprintStack(stack),
                this.getDrillStack(stack),
                this.getAugmentInventory(stack),
                this.getAugmentLimit()))
            : Optional.empty();
    }
    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, Consumer<Component> textConsumer, TooltipFlag options) {
        MachineUpgradesUtil upgradesUtil = MachineUpgradesUtil.from(this.getMachineUpgradeStacks(stack));

        upgradesUtil.appendStatsTooltip(this, textConsumer);

        if(upgradesUtil.hasConflicts()) {
            textConsumer.accept(ReadableString.space());
            upgradesUtil.appendConflictTooltip(textConsumer);
        }

        if(Minecraft.getInstance().hasShiftDown()) {
            long capacity = this.getEnergyCapacity();
            long stored = this.getEnergyStored(stack);

            MutableComponent LORE_STORED_DETAIL = ReadableString.translatable("item.iron_quarry.lore.energy.unit", ReadableString.intFrom(stored));
            MutableComponent LORE_STORED = ReadableString.translatable("item.iron_quarry.quarry_block.lore.stored", LORE_STORED_DETAIL);

            MutableComponent LORE_CAPACITY_DETAIL = ReadableString.translatable("item.iron_quarry.lore.energy.unit", ReadableString.intFrom(capacity));
            MutableComponent LORE_CAPACITY = ReadableString.translatable("item.iron_quarry.quarry_block.lore.capacity", LORE_CAPACITY_DETAIL);

            textConsumer.accept(ReadableString.space());
            textConsumer.accept(LORE_STORED);
            textConsumer.accept(LORE_CAPACITY);

            textConsumer.accept(CustomTooltipData.MARKER);
        } else {
            MutableComponent LORE_DETAILS = ReadableString.translatable("item.iron_quarry.lore.details");

            textConsumer.accept(ReadableString.space());
            textConsumer.accept(LORE_DETAILS);
        }
    }

    public long getEnergyConsumption(MachineUpgradesUtil upgradesUtil) {
        return this.getEnergyConsumption(upgradesUtil, Blocks.STONE);
    }
    public long getEnergyConsumption(MachineUpgradesUtil upgradesUtil, Block block) {
        float blockHardnessPenalty = Math.max(1.0F, Math.min(5.0F, block.defaultDestroyTime() / 10));
        float inefficiencyPenalty = upgradesUtil.getInefficiency();
        float operations = (20.0F / this.ticksPerOperation) * upgradesUtil.getSpeedMultiplier();

        return (long) (this.baseConsumption * blockHardnessPenalty * inefficiencyPenalty * 20 / operations);
    }

    /**
     * @see net.minecraft.server.level.ServerPlayerGameMode#destroyBlock(BlockPos)
     */
    @Override
    public InteractionResult handleBlockUse(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
        BlockPos blockPos = hitResult.getBlockPos();
        Direction side = hitResult.getDirection();

        BlockEntity blockEntity = world.getBlockEntity(blockPos);
        BlockState blockState = world.getBlockState(blockPos);
        ItemStack stack = player.getItemInHand(hand);

        if (blockEntity instanceof QuarryBlockEntity quarryBlockEntity) {
            if(stack.is(ZItemTags.C_WRENCHES)) {
                if(!world.isClientSide()) {
                    if(player.isSecondaryUseActive()) {
                        this.dismantle(world, blockPos, blockState, player, stack);
                    } else {
                        MachineConfiguration Configuration = quarryBlockEntity.Configuration;
                        Face face = Face.from(side, blockState.getValue(FACING));

                        Configuration.setIoState(face, Configuration.getNextIoState(face));
                    }
                }

                return InteractionResult.SUCCESS;
            } else if(stack.getItem() instanceof QuarryMonitorItem) {
                return InteractionResult.PASS;
            } else if(!player.isSecondaryUseActive()) {
                if (!world.isClientSide()) {
                    quarryBlockEntity.refreshOwnerName();

                    if (player instanceof ServerPlayer serverPlayer) {
                        Platform.MENU.openMenu(serverPlayer, quarryBlockEntity, quarryBlockEntity.getScreenOpeningData(serverPlayer), QuarryBlockScreenData.PACKET_CODEC);
                    }
                }

                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    private void dismantle(Level world, BlockPos blockPos, BlockState blockState, Player player, ItemStack stack) {
        if(player.blockActionRestricted(world, blockPos, player.gameMode())) return;

        BlockEntity blockEntity = world.getBlockEntity(blockPos);

        if(!Platform.BLOCK_BREAK.beforeBreak(world, player, blockPos, blockState, blockEntity)) {
            Platform.BLOCK_BREAK.onBreakCanceled(world, player, blockPos, blockState, blockEntity);
            return;
        }

        this.playerWillDestroy(world, blockPos, blockState, player);
        boolean removed = world.removeBlock(blockPos, false);
        if(removed) this.destroy(world, blockPos, blockState);

        if(!player.isCreative()) {
            stack.mineBlock(world, blockState, blockPos, player);
            this.playerDestroy(world, player, blockPos, blockState, blockEntity, stack.copy());
        }

        if(removed) Platform.BLOCK_BREAK.afterBreak(world, player, blockPos, blockState, blockEntity);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(world, pos, state, placer, itemStack);
        if(world.isClientSide()) return;

        if(placer instanceof Player player && world.getBlockEntity(pos) instanceof QuarryBlockEntity quarryBlockEntity) {
            quarryBlockEntity.setOwner(player.getUUID(), player.getGameProfile().name());
        }
    }

    @Override
    public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof QuarryBlockEntity) {
            if (!world.isClientSide() && player.isCreative()) {
                ItemStack itemStack = new ItemStack(this);
                itemStack.applyComponents(blockEntity.collectComponents());

                ItemEntity itemEntity = new ItemEntity(world, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, itemStack);
                itemEntity.setDefaultPickUpDelay();
                world.addFreshEntity(itemEntity);
            }
        }

        return super.playerWillDestroy(world, pos, state, player);
    }

    public int getTicksPerOperation(){
        return this.ticksPerOperation;
    }
    @Override
    public int getBaseConsumption() { return this.baseConsumption; }
    @Override
    public int getEnergyCapacity() { return this.energyCapacity; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager) {
        stateManager.add(FACING);
    }

    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    public Identifier getTextureId(){
        return Identifier.fromNamespaceAndPath(Main.MOD_ID, "textures/" + this.textureReference + ".png");
    }
    public Identifier getParticleTextureId(){
        return Identifier.fromNamespaceAndPath(Main.MOD_ID, "textures/" + this.textureReference + "_particles.png");
    }
    private static final Identifier BLOCK_ATLAS_TEXTURE = Identifier.withDefaultNamespace("textures/atlas/blocks.png");

    public SpriteId getSpriteId(){
        return new SpriteId(BLOCK_ATLAS_TEXTURE, Identifier.fromNamespaceAndPath(Main.MOD_ID, this.textureReference));
    }
    public SpriteId getParticleSpriteId(){
        return new SpriteId(BLOCK_ATLAS_TEXTURE, Identifier.fromNamespaceAndPath(Main.MOD_ID, this.textureReference + "_particles"));
    }
    @Environment(EnvType.CLIENT)
    public void initClient() {
        ResourceReloadListener.include(this.getTextureId());
        ResourceReloadListener.include(this.getParticleTextureId());
    }

    public TexturePosition getTexturePosition(Face face, Boolean alt){
        final int u;
        final int v;

        switch (face) {
            case TOP -> {
                if(alt) {
                    u = 16;
                    v = 0;
                } else {
                    u = 64;
                    v = 0;
                }
            }
            case LEFT -> {
                if(alt) {
                    u = 0;
                    v = 16;
                } else {
                    u = 48;
                    v = 16;
                }
            }
            case BACK -> {
                if(alt) {
                    u = 32;
                    v = 32;
                } else {
                    u = 80;
                    v = 32;
                }
            }
            case RIGHT -> {
                if(alt) {
                    u = 32;
                    v = 16;
                } else {
                    u = 80;
                    v = 16;
                }
            }
            case FRONT -> {
                if(alt) {
                    u = 16;
                    v = 16;
                } else {
                    u = 64;
                    v = 16;
                }
            }
            case BOTTOM -> {
                if(alt) {
                    u = 16;
                    v = 32;
                } else {
                    u = 64;
                    v = 32;
                }
            }
            default -> {
                u = 0;
                v = 0;
            }
        }

        return new TexturePosition(u, v, 16, 16);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new QuarryBlockEntity(pos, state);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ZBlockEntities.QUARRY_BLOCK_ENTITY, QuarryBlockEntity::tick);
    }
}
