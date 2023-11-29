package pw.rxj.iron_quarry.blocks;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
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
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.blockentities.QuarryBlockEntity;
import pw.rxj.iron_quarry.blockentities.ZBlockEntities;
import pw.rxj.iron_quarry.interfaces.IEnergyContainer;
import pw.rxj.iron_quarry.interfaces.IHandledCrafting;
import pw.rxj.iron_quarry.recipes.HandledCraftingRecipe;
import pw.rxj.iron_quarry.records.TexturePosition;
import pw.rxj.iron_quarry.types.Face;
import pw.rxj.iron_quarry.util.*;

import java.text.DecimalFormat;
import java.util.List;

public class QuarryBlock extends BlockWithEntity implements IHandledCrafting, IEnergyContainer {
    private final String textureReference;
    private final int ticksPerOperation;
    private final int baseConsumption;
    private final int energyCapacity;
    private final int augmentLimit;

    protected QuarryBlock(Block reference, String texRef, int augmentLimit,  int ticksPerOperation, int energyCapacity, int baseConsumption) {
        super(FabricBlockSettings.copyOf(reference));
        this.textureReference = texRef;

        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(OPEN_FRONT, false)
                .with(OPEN_BACK, false)
                .with(OPEN_RIGHT, false)
                .with(OPEN_LEFT, false)
                .with(OPEN_TOP, false)
                .with(OPEN_BOTTOM, false)
        );

        this.ticksPerOperation = ticksPerOperation;
        this.baseConsumption = baseConsumption;
        this.energyCapacity = energyCapacity;
        this.augmentLimit = augmentLimit;
    }

    public static QuarryBlock getFallback() {
        return (QuarryBlock) ZBlocks.COPPER_QUARRY.getBlock();
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

    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
        ItemStack quarryBlockStack = stack.copy();

        NbtCompound nbt = quarryBlockStack.getOrCreateNbt();
        NbtCompound MachineUpgradesNbt = nbt.getCompound("BlockEntityTag").getCompound("rxj.pw/Storage").getCompound("MachineUpgradesInventory");

        ComplexInventory MachineUpgradesInventory = new ComplexInventory(this.augmentLimit);
        MachineUpgradesInventory.read(MachineUpgradesNbt.getList("Items", NbtElement.COMPOUND_TYPE));

        MachineUpgradesUtil machineUpgradesUtil = MachineUpgradesUtil.from(MachineUpgradesInventory);

        DecimalFormat integerFormat = new DecimalFormat("#,##0");
        float yield_bonus = (machineUpgradesUtil.getFortuneMultiplier() - 1) * 100.0F;
        float energy_usage = this.baseConsumption * machineUpgradesUtil.getInefficiency();
        float operations = (20.0F / this.ticksPerOperation) * machineUpgradesUtil.getSpeedMultiplier();

        SupplicableAlt<?> HasShiftDown = SupplicableAlt.when(Screen.hasShiftDown());

        SupplicableAlt<String> PER_TYPE = HasShiftDown.<String>copy()
                .then(() -> "per_operation")
                .or(() -> "per_tick");
        SupplicableAlt<String> PER_UNIT = HasShiftDown.<String>copy()
                .then(() -> ZUtil.expandableFixedFloat(energy_usage * 20 / operations))
                .or(() -> integerFormat.format(energy_usage));

        MutableText LORE_USAGE_DETAIL = ReadableString.translatable("item.iron_quarry.lore.energy." + PER_TYPE.get(), PER_UNIT.get());
        MutableText LORE_USAGE = ReadableString.translatable("item.iron_quarry.quarry_block.lore.usage", LORE_USAGE_DETAIL);

        MutableText LORE_SPEED_DETAIL = ReadableString.translatable("item.iron_quarry.lore.operation.per_second", ZUtil.expandableFixedFloat(operations));
        MutableText LORE_SPEED = ReadableString.translatable("item.iron_quarry.quarry_block.lore.speed", LORE_SPEED_DETAIL);

        MutableText LORE_YIELD_DETAIL = ReadableString.translatable("item.iron_quarry.lore.percentage.positive", ZUtil.expandableFixedFloat(yield_bonus));
        MutableText LORE_YIELD = ReadableString.translatable("item.iron_quarry.quarry_block.lore.yield", LORE_YIELD_DETAIL);

        tooltip.add(LORE_USAGE);
        tooltip.add(LORE_SPEED);
        tooltip.add(LORE_YIELD);

        if(HasShiftDown.test()) {
            long capacity = this.getEnergyCapacity();
            long stored = this.getEnergyStored(stack);

            MutableText LORE_STORED_DETAIL = ReadableString.translatable("item.iron_quarry.lore.energy.unit", integerFormat.format(stored));
            MutableText LORE_STORED = ReadableString.translatable("item.iron_quarry.quarry_block.lore.stored", LORE_STORED_DETAIL);

            MutableText LORE_CAPACITY_DETAIL = ReadableString.translatable("item.iron_quarry.lore.energy.unit", integerFormat.format(capacity));
            MutableText LORE_CAPACITY = ReadableString.translatable("item.iron_quarry.quarry_block.lore.capacity", LORE_CAPACITY_DETAIL);

            tooltip.add(Text.empty());
            tooltip.add(LORE_STORED);
            tooltip.add(LORE_CAPACITY);
        } else {
            MutableText LORE_DETAILS = ReadableString.translatable("item.iron_quarry.lore.details");



            tooltip.add(Text.empty());
            tooltip.add(LORE_DETAILS);
        }
    }

    public long getActualEnergyConsumption(MachineUpgradesUtil upgradesUtil, Block block) {
        float blockHardnessPenalty = Math.max(1.0F, Math.min(5.0F, block.getHardness() / 10));
        float inefficiencyPenalty = upgradesUtil.getInefficiency();
        float operations = (20.0F / this.ticksPerOperation) * upgradesUtil.getSpeedMultiplier();

        return (long) (this.baseConsumption * blockHardnessPenalty * inefficiencyPenalty * 20 / operations);
    }

    public static final DirectionProperty FACING = DirectionProperty.of("facing");
    public static final BooleanProperty OPEN_FRONT = BooleanProperty.of("open_front");
    public static final BooleanProperty OPEN_BACK = BooleanProperty.of("open_back");
    public static final BooleanProperty OPEN_RIGHT = BooleanProperty.of("open_right");
    public static final BooleanProperty OPEN_LEFT = BooleanProperty.of("open_left");
    public static final BooleanProperty OPEN_TOP = BooleanProperty.of("open_top");
    public static final BooleanProperty OPEN_BOTTOM = BooleanProperty.of("open_bottom");

    public static BooleanProperty getFacingProperty(Face face){
        switch(face) {
            case BOTTOM -> {
                return OPEN_BOTTOM;
            }
            case TOP -> {
                return OPEN_TOP;
            }
            case FRONT -> {
                return OPEN_FRONT;
            }
            case BACK -> {
                return OPEN_BACK;
            }
            case LEFT -> {
                return OPEN_LEFT;
            }
            case RIGHT -> {
                return OPEN_RIGHT;
            }
            default -> {
                return null;
            }
        }
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
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

        super.onBreak(world, pos, state, player);
    }

    @Override
    public ItemStack getCraftingOutput(HandledCraftingRecipe handler, CraftingInventory craftingInventory) {
        ItemStack output = handler.getOutput().copy();

        for (int i = 0; i < craftingInventory.size(); i++) {
            ItemStack stack = craftingInventory.getStack(i).copy();

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
        stateManager.add(FACING, OPEN_FRONT, OPEN_BACK, OPEN_RIGHT, OPEN_LEFT, OPEN_TOP, OPEN_BOTTOM);
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite());
    }

    public Identifier getTextureId(){
        return new Identifier(Main.MOD_ID, "textures/" + this.textureReference + ".png");
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
                    u = 48;
                    v = 0;
                }
            }
            case LEFT, BACK, RIGHT -> {
                if(alt) {
                    u = 0;
                    v = 16;
                } else {
                    u = 32;
                    v = 16;
                }
            }
            case FRONT -> {
                if(alt) {
                    u = 16;
                    v = 16;
                } else {
                    u = 48;
                    v = 16;
                }
            }
            case BOTTOM -> {
                if(alt) {
                    u = 16;
                    v = 32;
                } else {
                    u = 48;
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
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            //This will call the createScreenHandlerFactory method from BlockWithEntity, which will return our blockEntity cast to
            //a namedScreenHandlerFactory. If your block class does not extend BlockWithEntity, it needs to implement createScreenHandlerFactory.
            NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, pos);

            if (screenHandlerFactory != null) {
                //With this call the server will request the client to open the appropriate ScreenHandler
                player.openHandledScreen(screenHandlerFactory);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, ZBlockEntities.QUARRY_BLOCK_ENTITY, QuarryBlockEntity::tick);
    }
}
