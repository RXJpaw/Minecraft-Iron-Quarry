package pw.rxj.iron_quarry.block;

import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.item.TooltipData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.blockentity.QuarryBlockEntity;
import pw.rxj.iron_quarry.blockentity.ZBlockEntities;
import pw.rxj.iron_quarry.gui.CustomTooltipData;
import pw.rxj.iron_quarry.gui.ITooltipDataProvider;
import pw.rxj.iron_quarry.gui.TooltipQuarryInventoryData;
import pw.rxj.iron_quarry.interfaces.IEnergyContainer;
import pw.rxj.iron_quarry.interfaces.IHandledCrafting;
import pw.rxj.iron_quarry.interfaces.IHandledKeyedAction;
import pw.rxj.iron_quarry.interfaces.IHandledUseBlock;
import pw.rxj.iron_quarry.item.QuarryMonitorItem;
import pw.rxj.iron_quarry.item.ZItemTags;
import pw.rxj.iron_quarry.network.KeyedActionPacket;
import pw.rxj.iron_quarry.network.ZNetwork;
import pw.rxj.iron_quarry.records.TexturePosition;
import pw.rxj.iron_quarry.resource.ResourceReloadListener;
import pw.rxj.iron_quarry.resource.config.server.QuarryStatsConfig;
import pw.rxj.iron_quarry.types.ActionGoal;
import pw.rxj.iron_quarry.types.Face;
import pw.rxj.iron_quarry.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QuarryBlock extends BlockWithEntity implements IHandledCrafting, IEnergyContainer, ITooltipDataProvider, IHandledUseBlock, IHandledKeyedAction {
    private final String textureReference;
    private int ticksPerOperation;
    private int baseConsumption;
    private int energyCapacity;
    private int augmentLimit;

    protected QuarryBlock(Block reference, String texRef, QuarryStatsConfig.Entry entry) {
        super(FabricBlockSettings.copyOf(reference));
        this.textureReference = texRef;

        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, Direction.NORTH)
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

    public static final DirectionProperty FACING = DirectionProperty.of("facing");

    public static QuarryBlock getFallback() {
        return ZBlocks.COPPER_QUARRY.getBlock();
    }

    public static boolean isOf(ItemStack stack){
        return ZUtil.getBlockOrItem(stack) instanceof QuarryBlock;
    }
    public static boolean isNotOf(ItemStack stack){
        return !isOf(stack);
    }

    public int getAugmentLimit(){
        return this.augmentLimit;
    }
    public @NotNull ComplexInventory getAugmentInventory(ItemStack stack) {
        ComplexInventory MachineUpgradesInventory = new ComplexInventory(this.getAugmentLimit());

        if(stack == null) return MachineUpgradesInventory;
        NbtCompound tag = stack.getNbt();
        if(tag == null) return MachineUpgradesInventory;

        NbtCompound Storage = tag.getCompound("BlockEntityTag").getCompound("rxj.pw/Storage");
        NbtCompound StorageMachineUpgradesInventory = Storage.getCompound("MachineUpgradesInventory");
        MachineUpgradesInventory.read(StorageMachineUpgradesInventory.getList("Items", NbtElement.COMPOUND_TYPE));

        return MachineUpgradesInventory;
    }
    public List<ItemStack> getMachineUpgradeStacks(ItemStack stack) {
        List<ItemStack> stacks = new ArrayList<>();

        if(stack == null) return stacks;
        NbtCompound tag = stack.getNbt();
        if(tag == null) return stacks;

        SimpleInventory MachineUpgradesInventory = this.getAugmentInventory(stack);
        SimpleInventory DrillInventory = new SimpleInventory(1);

        NbtCompound Storage = tag.getCompound("BlockEntityTag").getCompound("rxj.pw/Storage");
        NbtCompound StorageDrillInventory = Storage.getCompound("DrillInventory");
        DrillInventory.readNbtList(StorageDrillInventory.getList("Items", NbtElement.COMPOUND_TYPE));

        stacks.addAll(MachineUpgradesInventory.heldStacks);
        stacks.addAll(DrillInventory.heldStacks);

        return stacks;
    }

    public @NotNull MachineConfiguration getMachineConfiguration(ItemStack stack) {
        MachineConfiguration MachineConfiguration = new MachineConfiguration();

        if(stack == null) return MachineConfiguration;
        NbtCompound tag = stack.getNbt();
        if(tag == null) return MachineConfiguration;

        NbtCompound Config = tag.getCompound("BlockEntityTag").getCompound("rxj.pw/Config");
        MachineConfiguration.read(Config);

        return MachineConfiguration;
    }
    public @NotNull ItemStack getBlueprintStack(ItemStack stack) {
        ComplexInventory BlueprintInventory = new ComplexInventory(1);

        if(stack == null) return ItemStack.EMPTY;
        NbtCompound tag = stack.getNbt();
        if(tag == null) return ItemStack.EMPTY;

        NbtCompound Storage = tag.getCompound("BlockEntityTag").getCompound("rxj.pw/Storage");
        NbtCompound StorageBlueprintInventoryInventory = Storage.getCompound("BlueprintInventory");
        BlueprintInventory.read(StorageBlueprintInventoryInventory.getList("Items", NbtElement.COMPOUND_TYPE));

        return BlueprintInventory.getStack(0);
    }
    public @NotNull ItemStack getDrillStack(ItemStack stack) {
        ComplexInventory DrillInventory = new ComplexInventory(1);

        if(stack == null) return ItemStack.EMPTY;
        NbtCompound tag = stack.getNbt();
        if(tag == null) return ItemStack.EMPTY;

        NbtCompound Storage = tag.getCompound("BlockEntityTag").getCompound("rxj.pw/Storage");
        NbtCompound StorageDrillInventoryInventory = Storage.getCompound("DrillInventory");
        DrillInventory.read(StorageDrillInventoryInventory.getList("Items", NbtElement.COMPOUND_TYPE));

        return DrillInventory.getStack(0);
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        return Screen.hasShiftDown() ? Optional.of(TooltipQuarryInventoryData.from(
                this.getBlueprintStack(stack),
                this.getDrillStack(stack),
                this.getAugmentInventory(stack),
                this.getAugmentLimit()))
            : Optional.empty();
    }
    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
        MachineUpgradesUtil machineUpgradesUtil = MachineUpgradesUtil.from(this.getMachineUpgradeStacks(stack));

        float yield_bonus = (machineUpgradesUtil.getFortuneMultiplier() - 1) * 100.0F;
        float energy_usage = this.baseConsumption * machineUpgradesUtil.getInefficiency();
        float operations = (20.0F / this.ticksPerOperation) * machineUpgradesUtil.getSpeedMultiplier();

        SupplicableAlt<?> HasShiftDown = SupplicableAlt.when(Screen.hasShiftDown());

        SupplicableAlt<String> PER_TYPE = HasShiftDown.<String>copy()
                .then(() -> "per_operation")
                .or(() -> "per_tick");
        SupplicableAlt<String> PER_UNIT = HasShiftDown.<String>copy()
                .then(() -> ZUtil.expandableFixedInt((int) (energy_usage * 20 / operations)))
                .or(() -> ReadableString.cIntFrom(energy_usage));

        MutableText LORE_USAGE_DETAIL = ReadableString.translatable("item.iron_quarry.lore.energy." + PER_TYPE.get(), PER_UNIT.get());
        MutableText LORE_USAGE = ReadableString.translatable("item.iron_quarry.quarry_block.lore.usage", LORE_USAGE_DETAIL);

        MutableText LORE_SPEED_DETAIL = ReadableString.translatable("item.iron_quarry.lore.operation.per_second", ZUtil.expandableFixedFloat(operations));
        MutableText LORE_SPEED = ReadableString.translatable("item.iron_quarry.quarry_block.lore.speed", LORE_SPEED_DETAIL);

        MutableText LORE_YIELD_DETAIL = ReadableString.translatable("item.iron_quarry.lore.percentage.positive", ZUtil.expandableFixedFloat(yield_bonus));
        MutableText LORE_YIELD = ReadableString.translatable("item.iron_quarry.quarry_block.lore.yield", LORE_YIELD_DETAIL);

        tooltip.add(LORE_USAGE);
        tooltip.add(LORE_SPEED);
        tooltip.add(LORE_YIELD);

        int conflictFlag = machineUpgradesUtil.getConflictFlag();
        if(MachineUpgradesUtil.hasConflicts(conflictFlag)) {
            tooltip.add(ReadableString.space());

            if(MachineUpgradesUtil.hasConflict(conflictFlag, MachineUpgradesUtil.CONFLICT_SPEED)) {
                tooltip.add(ReadableString.translatable("item.iron_quarry.conflict.speed_limit", ZUtil.expandableFixedFloat((MachineUpgradesUtil.SPEED_LIMIT-1)*100)));
            }
            if(MachineUpgradesUtil.hasConflict(conflictFlag, MachineUpgradesUtil.CONFLICT_FORTUNE)) {
                tooltip.add(ReadableString.translatable("item.iron_quarry.conflict.fortune_limit", ZUtil.expandableFixedFloat((MachineUpgradesUtil.FORTUNE_LIMIT-1)*100)));
            }
            if(MachineUpgradesUtil.hasConflict(conflictFlag, MachineUpgradesUtil.CONFLICT_SILK_TOUCH)) {
                tooltip.add(ReadableString.translatable("item.iron_quarry.conflict.silk_touch_fortune"));
            }
        }

        if(HasShiftDown.test()) {
            long capacity = this.getEnergyCapacity();
            long stored = this.getEnergyStored(stack);

            MutableText LORE_STORED_DETAIL = ReadableString.translatable("item.iron_quarry.lore.energy.unit", ReadableString.intFrom(stored));
            MutableText LORE_STORED = ReadableString.translatable("item.iron_quarry.quarry_block.lore.stored", LORE_STORED_DETAIL);

            MutableText LORE_CAPACITY_DETAIL = ReadableString.translatable("item.iron_quarry.lore.energy.unit", ReadableString.intFrom(capacity));
            MutableText LORE_CAPACITY = ReadableString.translatable("item.iron_quarry.quarry_block.lore.capacity", LORE_CAPACITY_DETAIL);

            tooltip.add(ReadableString.space());
            tooltip.add(LORE_STORED);
            tooltip.add(LORE_CAPACITY);

            tooltip.add(CustomTooltipData.MARKER);
        } else {
            MutableText LORE_DETAILS = ReadableString.translatable("item.iron_quarry.lore.details");

            tooltip.add(ReadableString.space());
            tooltip.add(LORE_DETAILS);
        }
    }

    public long getEnergyConsumption(MachineUpgradesUtil upgradesUtil) {
        return this.getEnergyConsumption(upgradesUtil, Blocks.STONE);
    }
    public long getEnergyConsumption(MachineUpgradesUtil upgradesUtil, Block block) {
        float blockHardnessPenalty = Math.max(1.0F, Math.min(5.0F, block.getHardness() / 10));
        float inefficiencyPenalty = upgradesUtil.getInefficiency();
        float operations = (20.0F / this.ticksPerOperation) * upgradesUtil.getSpeedMultiplier();

        return (long) (this.baseConsumption * blockHardnessPenalty * inefficiencyPenalty * 20 / operations);
    }

    /**
     * @see net.minecraft.server.network.ServerPlayerInteractionManager#tryBreakBlock(BlockPos)
     */
    @Override
    public ActionResult useOnBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        BlockPos blockPos = hitResult.getBlockPos();
        Direction side = hitResult.getSide();

        BlockEntity blockEntity = world.getBlockEntity(blockPos);
        BlockState blockState = world.getBlockState(blockPos);
        ItemStack stack = player.getStackInHand(hand);

        if (blockEntity instanceof QuarryBlockEntity quarryBlockEntity) {
            if(stack.isIn(ZItemTags.C_WRENCHES)) {
                if(world.isClient() && MinecraftClient.getInstance().options.sneakKey.isPressed()) {
                    ZNetwork.sendToServer(KeyedActionPacket.bake("key.sneak", hand, ActionGoal.USE_ON_BLOCK, hitResult));
                } else {
                    MachineConfiguration Configuration = quarryBlockEntity.Configuration;
                    Face face = Face.from(side, blockState.get(FACING));

                    Configuration.setIoState(face, Configuration.getNextIoState(face));
                }

                return ActionResult.SUCCESS;
            } else if(stack.getItem() instanceof QuarryMonitorItem) {
                return ActionResult.PASS;
            } else if(!player.shouldCancelInteraction()) {
                if (!world.isClient()) {
                    NamedScreenHandlerFactory screenHandlerFactory = blockState.createScreenHandlerFactory(world, blockPos);
                    if (screenHandlerFactory != null) player.openHandledScreen(screenHandlerFactory);
                }

                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }

    @Override
    public void keyedUseOnBlock(String keyName, ItemUsageContext context, BlockState blockState) {
        if(!keyName.equals("key.sneak")) return;

        PlayerEntity player = context.getPlayer();
        if(player == null) return;

        BlockPos blockPos = context.getBlockPos();
        if(player.squaredDistanceTo((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5) > 64.0) return;

        World world = context.getWorld();
        ItemStack stack = context.getStack();
        BlockEntity blockEntity = world.getBlockEntity(blockPos);

        if(stack.isIn(ZItemTags.C_WRENCHES)) {
            this.onBreak(world, blockPos, blockState, player);
            boolean removed = world.removeBlock(blockPos, false);
            if(removed) this.onBroken(world, blockPos, blockState);

            if(!player.isCreative()) {
                stack.postMine(world, blockState, blockPos, player);
                this.afterBreak(world, player, blockPos, blockState, blockEntity, stack.copy());
            }
        }
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof QuarryBlockEntity) {
            if (!world.isClient && player.isCreative()) {
                ItemStack itemStack = new ItemStack(this);
                blockEntity.setStackNbt(itemStack);

                ItemEntity itemEntity = new ItemEntity(world, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, itemStack);
                itemEntity.setToDefaultPickupDelay();
                world.spawnEntity(itemEntity);
            }
        }

        return super.onBreak(world, pos, state, player);
    }

    @Override
    public ItemStack getCraftingOutput(ShapedRecipe handler, RecipeInputInventory recipeInputInventory, DynamicRegistryManager dynamicRegistryManager) {
        ItemStack output = handler.getResult(dynamicRegistryManager).copy();

        for (int i = 0; i < recipeInputInventory.size(); i++) {
            ItemStack stack = recipeInputInventory.getStack(i).copy();

            if(QuarryBlock.isOf(stack)) {
                if(stack.hasNbt()) output.setNbt(stack.getNbt());

                break;
            }
        }

        return output;
    }

    public int getTicksPerOperation(){
        return this.ticksPerOperation;
    }
    @Override
    public int getBaseConsumption() { return this.baseConsumption; }
    @Override
    public int getEnergyCapacity() { return this.energyCapacity; }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(FACING);
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    public Identifier getTextureId(){
        return new Identifier(Main.MOD_ID, "textures/" + this.textureReference + ".png");
    }
    public Identifier getParticleTextureId(){
        return new Identifier(Main.MOD_ID, "textures/" + this.textureReference + "_particles.png");
    }
    public SpriteIdentifier getSpriteId(){
        return new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, Identifier.of(Main.MOD_ID, this.textureReference));
    }
    public SpriteIdentifier getParticleSpriteId(){
        return new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, Identifier.of(Main.MOD_ID, this.textureReference + "_particles"));
    }
    @Environment(EnvType.CLIENT)
    public void initClient() {
        ResourceReloadListener.include(this.getTextureId());
        ResourceReloadListener.include(this.getParticleTextureId());
        BlockRenderLayerMap.INSTANCE.putBlock(this, RenderLayer.getCutoutMipped());
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
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new QuarryBlockEntity(pos, state);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, ZBlockEntities.QUARRY_BLOCK_ENTITY, QuarryBlockEntity::tick);
    }
}
