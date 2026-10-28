package pw.rxj.iron_quarry.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.data.BlueprintPositionComponent;
import pw.rxj.iron_quarry.data.ZComponents;
import pw.rxj.iron_quarry.interfaces.*;
import pw.rxj.iron_quarry.network.PacketBlueprintSelect;
import pw.rxj.iron_quarry.render.Cuboid;
import pw.rxj.iron_quarry.screen.QuarryBlockScreen;
import pw.rxj.iron_quarry.types.DualPosition;
import pw.rxj.iron_quarry.types.ScrollDirection;
import pw.rxj.iron_quarry.util.ReadableString;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class BlueprintItem extends Item implements IHandledAttackOnBlock, IHandledUseOnBlock, IHandledGrinding, IHandledItemEntity, IHandledMainHandScroll, IHandledMainHandAttack, IAlwaysRenderItemName, IModifyCreativeTabEntries {
    public BlueprintItem(Properties settings) {
        super(settings);
    }

    public static boolean isOf(ItemStack stack){
        return ZUtil.getBlockOrItem(stack) instanceof BlueprintItem;
    }
    public static boolean isNotOf(ItemStack stack){
        return !isOf(stack);
    }

    @Override
    public boolean isGrindable(ItemStack stack) {
        return this.isSealed(stack);
    }
    @Override
    public ItemStack getGrindingOutput(ItemStack stack, ItemStack other) {
        if(!other.isEmpty()) return null;
        ItemStack output = stack.copy();

        if(output.getItem() instanceof BlueprintItem blueprintItem) {
            blueprintItem.setSealed(output, false);
            blueprintItem.resetMinedChunks(output);
        }

        return output;
    }

    @Override
    public void handleItemEntity(ItemEntity itemEntity) {
        if(this.isSealed(itemEntity.getItem())) itemEntity.setInvulnerable(true);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean handleMainHandScroll(LocalPlayer player, ItemStack stack, ScrollDirection scrollDirection) {
        return BlueprintSelection.scroll(this, player, stack, scrollDirection);
    }
    public boolean expandInDirection(ItemStack stack, Player player, Direction direction, int amount) {
        Level playerWorld = player.level();

        ResourceKey<Level> world = this.getWorldRegistryKey(stack).orElseGet(playerWorld::dimension);
        if(!world.equals(player.level().dimension())) return false;

        BlockPos firstPos = this.getFirstPos(stack).orElseGet(player::blockPosition);
        BlockPos secondPos = this.getSecondPos(stack).orElseGet(player::blockPosition);

        final boolean first;
        final Vec3i vector;

        switch (direction) {
            case UP -> {
                first = firstPos.getY() > secondPos.getY();
                vector = new Vec3i(0, amount, 0);
            }
            case DOWN -> {
                first = firstPos.getY() < secondPos.getY();
                vector = new Vec3i(0, -amount, 0);
            }
            case SOUTH -> {
                first = firstPos.getZ() > secondPos.getZ();
                vector = new Vec3i(0, 0, amount);
            }
            case NORTH -> {
                first = firstPos.getZ() < secondPos.getZ();
                vector = new Vec3i(0, 0, -amount);
            }
            case EAST -> {
                first = firstPos.getX() > secondPos.getX();
                vector = new Vec3i(amount, 0, 0);
            }
            case WEST -> {
                first = firstPos.getX() < secondPos.getX();
                vector = new Vec3i(-amount, 0, 0);
            }
            default -> {
                return false;
            }
        }

        if(first) {
            firstPos = firstPos.offset(vector);
        } else {
            secondPos = secondPos.offset(vector);
        }

        this.setFirstPos(stack, playerWorld, firstPos);
        this.setSecondPos(stack, playerWorld, secondPos);

        return true;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return this.isSealed(stack) & this.getMinedChunks(stack) > 0;
    }
    @Override
    public int getBarColor(ItemStack stack) {
        return 0x5980DF;
    }
    @Override
    public int getBarWidth(ItemStack stack) {
        return (int) Math.ceil(((double) this.getMinedChunks(stack) / this.getMineableChunks(stack)) * 13.0);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag options) {
        ResourceKey<Level> worldKey = this.getWorldRegistryKey(stack).orElse(null);
        Identifier worldId = worldKey != null ? worldKey.identifier() : null;

        BlockPos firstPos = this.getFirstPos(stack).orElse(null);
        BlockPos secondPos = this.getSecondPos(stack).orElse(null);
        boolean isSealed = this.isSealed(stack);

        MutableComponent LORE_EMPTY = ReadableString.translatable("item.iron_quarry.blueprint.lore.empty");

        MutableComponent LORE_WORLD = ReadableString.translatable("item.iron_quarry.blueprint.lore.world", ReadableString.textFrom(worldId).orElse(LORE_EMPTY));
        MutableComponent LORE_FIRST_POS = ReadableString.translatable("item.iron_quarry.blueprint.lore.first_pos", ReadableString.textFrom(firstPos).orElse(LORE_EMPTY));
        MutableComponent LORE_SECOND_POS = ReadableString.translatable("item.iron_quarry.blueprint.lore.second_pos", ReadableString.textFrom(secondPos).orElse(LORE_EMPTY));

        textConsumer.accept(LORE_WORLD);
        textConsumer.accept(LORE_FIRST_POS);
        textConsumer.accept(LORE_SECOND_POS);

        if(isSealed) {
            long mined = this.getMinedChunks(stack);
            long mineable = this.getMineableChunks(stack);
            float percentage = mineable > 0 ? (float) mined / mineable : 0;

            MutableComponent LORE_MINED = ReadableString.translatable("item.iron_quarry.blueprint.lore.mined", mined, mineable, ZUtil.expandableFixedFloat(percentage * 100));

            textConsumer.accept(ReadableString.space());
            textConsumer.accept(LORE_MINED);
            if(this.allChunksMined(stack)) {
                MutableComponent LORE_COMPLETED = ReadableString.translatable("item.iron_quarry.blueprint.lore.completed");
                textConsumer.accept(LORE_COMPLETED);
            }
        } else {
            Minecraft minecraftClient = Minecraft.getInstance();

            if(minecraftClient.screen instanceof QuarryBlockScreen) {
                MutableComponent LORE_SEAL_FIRST = ReadableString.translatable("item.iron_quarry.blueprint.lore.seal_first");

                textConsumer.accept(ReadableString.space());
                textConsumer.accept(LORE_SEAL_FIRST);
            } else {
                textConsumer.accept(ReadableString.space());

                if(Minecraft.getInstance().hasShiftDown()) {
                    Options gameOptions = minecraftClient.options;

                    Component KEY_INTERACT = ReadableString.textFrom(gameOptions.keyUse);
                    Component KEY_ATTACK = ReadableString.textFrom(gameOptions.keyAttack);
                    Component KEY_SPRINT = ReadableString.textFrom(gameOptions.keySprint);
                    Component KEY_SNEAK = ReadableString.textFrom(gameOptions.keyShift);

                    List<MutableComponent> LORE_MANUAL_SELECT = ReadableString.translatableLines("item.iron_quarry.blueprint.lore.manual.select", ReadableString.intFrom(PacketBlueprintSelect.MAX_DISTANCE), KEY_ATTACK, KEY_INTERACT);
                    List<MutableComponent> LORE_MANUAL_EXPAND = ReadableString.translatableLines("item.iron_quarry.blueprint.lore.manual.expand", KEY_SNEAK);
                    List<MutableComponent> LORE_MANUAL_EXPAND_FASTER = ReadableString.translatableLines("item.iron_quarry.blueprint.lore.manual.expand_faster", KEY_SPRINT);

                    LORE_MANUAL_SELECT.forEach(textConsumer);
                    textConsumer.accept(ReadableString.space());
                    LORE_MANUAL_EXPAND.forEach(textConsumer);
                    LORE_MANUAL_EXPAND_FASTER.forEach(textConsumer);
                } else {
                    MutableComponent LORE_INSTRUCTIONS = ReadableString.translatable("item.iron_quarry.lore.instructions");

                    textConsumer.accept(LORE_INSTRUCTIONS);
                }
            }
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        String translationKey = "item.iron_quarry.blueprint";
        if(this.isSealed(stack)) translationKey += ".sealed";

        MutableComponent itemName = Component.translatable(translationKey);

        Optional<BlockPos> firstPos = this.getFirstPos(stack);
        Optional<BlockPos> secondPos = this.getSecondPos(stack);

        if(firstPos.isEmpty() || secondPos.isEmpty()) {
            itemName.append(" ").append(Component.translatable("item.iron_quarry.blueprint.unbound"));
        } else if(this.allChunksMined(stack)) {
            itemName.append(" ").append(Component.translatable("item.iron_quarry.blueprint.completed"));
        } else {
            Component cuboidSize = ReadableString.textFrom(Cuboid.from(firstPos.get(), secondPos.get())).orElse(ReadableString.ERROR);

            itemName.append(" (").append(cuboidSize).append(")");
        }

        return itemName;
    }
    @Override
    public boolean renderItemName(ItemStack stack) {
        return !this.isSealed(stack) && this.getFirstPos(stack).isPresent() && this.getSecondPos(stack).isPresent() && !this.allChunksMined(stack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return this.isSealed(stack);
    }

    public void setSealed(ItemStack stack, boolean sealed){
        stack.set(ZComponents.BLUEPRINT_SEALED, sealed);
        stack.set(DataComponents.RARITY, sealed ? Rarity.EPIC : Rarity.UNCOMMON);
    }
    public boolean isSealed(ItemStack stack){
        return stack.getOrDefault(ZComponents.BLUEPRINT_SEALED, false);
    }

    @Override
    public InteractionResult handleUseOnBlock(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
        if(!hand.equals(InteractionHand.MAIN_HAND)) return InteractionResult.PASS;
        if(player.isCreative() && player.isSecondaryUseActive()) return InteractionResult.PASS;

        ItemStack stack = player.getItemInHand(hand);
        if(this.isSealed(stack)) return InteractionResult.PASS;

        if(world.isClientSide()) BlueprintSelection.use(this, player);

        return InteractionResult.FAIL;
    }
    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if(!hand.equals(InteractionHand.MAIN_HAND)) return InteractionResult.FAIL;

        ItemStack stack = user.getItemInHand(hand);

        if(user.isCreative() && user.isSecondaryUseActive()) {
            if(!world.isClientSide()) {
                this.setSealed(stack, !this.isSealed(stack));
                this.resetMinedChunks(stack);
            }

            return InteractionResult.CONSUME;
        }

        if(this.isSealed(stack)) return InteractionResult.FAIL;
        if(world.isClientSide()) BlueprintSelection.use(this, user);

        return InteractionResult.CONSUME;
    }
    @Override
    public InteractionResult handleAttackOnBlock(Player player, Level world, InteractionHand hand, BlockPos targetedPos, Direction direction) {
        if(!hand.equals(InteractionHand.MAIN_HAND)) return InteractionResult.PASS;

        return InteractionResult.FAIL;
    }
    @Override
    @Environment(EnvType.CLIENT)
    public boolean handleMainHandAttack(LocalPlayer player, ItemStack stack, int clickCount) {
        return BlueprintSelection.attack(this, player, stack, clickCount);
    }

    public void setWorld(ItemStack stack, Level world) {
        this.setWorld(stack, world.dimension());
    }
    public void setWorld(ItemStack stack, ResourceKey<Level> newWorldKey) {
        ResourceKey<Level> oldWorldKey = this.getWorldRegistryKey(stack).orElse(null);
        if(ZUtil.equals(oldWorldKey, newWorldKey)) return;

        this.resetPositions(stack);

        stack.set(ZComponents.BLUEPRINT_WORLD, newWorldKey);

    }
    public Optional<ResourceKey<Level>> getWorldRegistryKey(ItemStack stack) {
        return Optional.ofNullable(stack.get(ZComponents.BLUEPRINT_WORLD));
    }

    public void resetPositions(ItemStack stack) {
        stack.remove(ZComponents.BLUEPRINT_POSITION);
    }
    public void setFirstPos(ItemStack stack, @Nullable Level world, BlockPos blockPos) {
        this.setPosition(DualPosition.FIRST, stack, world, blockPos);
    }
    public void setSecondPos(ItemStack stack, @Nullable Level world, BlockPos blockPos) {
        this.setPosition(DualPosition.SECOND, stack, world, blockPos);
    }
    public void setPosition(DualPosition position, ItemStack stack, @Nullable Level world, BlockPos blockPos) {
        if(world != null) {
            this.setWorld(stack, world);
            blockPos = ZUtil.limitInsideWorldBounds(blockPos, world);
        } else if(this.getWorldRegistryKey(stack).isEmpty()) {
            throw new IllegalStateException("BlueprintItem#setWorld has to be called first if no world is supplied.");
        }

        BlueprintPositionComponent blueprintPosition = stack.getOrDefault(ZComponents.BLUEPRINT_POSITION, BlueprintPositionComponent.twin(blockPos));

        stack.set(ZComponents.BLUEPRINT_POSITION, blueprintPosition.withPosition(position, blockPos));
    }
    public Optional<BlockPos> getFirstPos(ItemStack stack) {
        return Optional.ofNullable(this.getPosition(DualPosition.FIRST, stack));
    }
    public Optional<BlockPos> getSecondPos(ItemStack stack) {
        return Optional.ofNullable(this.getPosition(DualPosition.SECOND, stack));
    }
    private @Nullable BlockPos getPosition(DualPosition position, ItemStack stack) {
        BlueprintPositionComponent blueprintPosition = stack.get(ZComponents.BLUEPRINT_POSITION);
        if(blueprintPosition == null) return null;

        return blueprintPosition.getPosition(position);
    }

    public long getMinedChunks(ItemStack stack) {
        return stack.getOrDefault(ZComponents.BLUEPRINT_MINED_CHUNKS, 0L);
    }
    public long getMineableChunks(ItemStack stack) {
        BlockPos firstPos = this.getFirstPos(stack).orElse(null);
        if(firstPos == null) return 0;
        BlockPos secondPos = this.getSecondPos(stack).orElse(null);
        if(secondPos == null) return 0;

        int firstChunkX = firstPos.getX() >> 4;
        int firstChunkZ = firstPos.getZ() >> 4;
        int secondChunkX = secondPos.getX() >> 4;
        int secondChunkZ = secondPos.getZ() >> 4;

        int chunksOnX = Math.abs(firstChunkX - secondChunkX) + 1;
        int chunksOnZ = Math.abs(firstChunkZ - secondChunkZ) + 1;
        return (long) chunksOnX * chunksOnZ;
    }
    public boolean allChunksMined(ItemStack stack) {
        long mineable = this.getMineableChunks(stack);
        if(mineable == 0) return false;
        long mined = this.getMinedChunks(stack);

        return mined == mineable;
    }
    public boolean hasNextChunk(ItemStack stack) {
        return this.isSealed(stack) && this.getMinedChunks(stack) + 1 < this.getMineableChunks(stack);
    }
    public boolean hasPreviousChunk(ItemStack stack) {
        return this.isSealed(stack) && this.getMinedChunks(stack) > 0;
    }
    public void resetMinedChunks(ItemStack stack) {
        stack.remove(ZComponents.BLUEPRINT_MINED_CHUNKS);
    }
    public void increaseMinedChunks(ItemStack stack) {
        this.increaseMinedChunks(stack, 1);
    }
    public void increaseMinedChunks(ItemStack stack, long amount) {
        stack.set(ZComponents.BLUEPRINT_MINED_CHUNKS, this.getMinedChunks(stack) + amount);
    }

    public List<@NotNull ChunkPos> getNextChunkPos(ItemStack stack, int maxPositions){
        List<@NotNull ChunkPos> chunkPosList = new ArrayList<>();

        for (int i = 0; i < maxPositions; i++) {
            ChunkPos chunkPos = this.getNextChunkPosWithOffset(stack, i);
            if(chunkPos == null) return chunkPosList;
            chunkPosList.add(chunkPos);
        }

        return chunkPosList;
    }
    public @Nullable ChunkPos getNextChunkPos(ItemStack stack) {
        return this.getNextChunkPosWithOffset(stack, 0);
    }
    public @Nullable ChunkPos getNextChunkPosWithOffset(ItemStack stack, long offset){
        BlockPos firstPos = this.getFirstPos(stack).orElse(null);
        if(firstPos == null) return null;
        BlockPos secondPos = this.getSecondPos(stack).orElse(null);
        if(secondPos == null) return null;

        int firstChunkX = firstPos.getX() >> 4;
        int firstChunkZ = firstPos.getZ() >> 4;
        int secondChunkX = secondPos.getX() >> 4;
        int secondChunkZ = secondPos.getZ() >> 4;

        int chunksOnX = Math.abs(firstChunkX - secondChunkX) + 1;
        int chunksOnZ = Math.abs(firstChunkZ - secondChunkZ) + 1;
        long chunksToMine = (long) chunksOnX * chunksOnZ;

        long currentChunkIndex = this.getMinedChunks(stack) + offset;
        if(currentChunkIndex >= chunksToMine) return null;

        int minChunkX = Math.min(firstChunkX, secondChunkX);
        int minChunkZ = Math.min(firstChunkZ, secondChunkZ);

        int offsetChunkX = (int) (currentChunkIndex % chunksOnX);
        int offsetChunkZ = (int) (currentChunkIndex / chunksOnX);
        int currentChunkX = minChunkX + offsetChunkX;
        int currentChunkZ = minChunkZ + offsetChunkZ;

        return new ChunkPos(currentChunkX, currentChunkZ);
    }
    public Optional<BoundingBox> getChunkBox(ItemStack stack, ChunkPos chunkPos) {
        BlockPos firstPos = this.getFirstPos(stack).orElse(null);
        if(firstPos == null) return Optional.empty();
        BlockPos secondPos = this.getSecondPos(stack).orElse(null);
        if(secondPos == null) return Optional.empty();

        //General Max/Min
        BoundingBox blueprintBox = BoundingBox.fromCorners(firstPos, secondPos);

        //Max/Min within the current Chunk
        BoundingBox chunkBox = new BoundingBox(chunkPos.x() << 4, blueprintBox.minY(), chunkPos.z() << 4, (chunkPos.x() << 4) + 15, blueprintBox.maxY(), (chunkPos.z() << 4) + 15);
        if(!blueprintBox.intersects(chunkBox)) return Optional.empty();

        return Optional.of(new BoundingBox(
                Math.max(blueprintBox.minX(), chunkBox.minX()), blueprintBox.minY(), Math.max(blueprintBox.minZ(), chunkBox.minZ()),
                Math.min(blueprintBox.maxX(), chunkBox.maxX()), blueprintBox.maxY(), Math.min(blueprintBox.maxZ(), chunkBox.maxZ())
        ));
    }

    @Override
    public void modifyCreativeTabEntries(CreativeModeTab.Output entries) {
        entries.accept(this);

        ItemStack stack = new ItemStack(this);

        this.setWorld(stack,  Level.OVERWORLD);
        this.setFirstPos(stack, null, new BlockPos(10000, 64, 10000));
        this.setSecondPos(stack, null, new BlockPos(-10000, -64, -10000));
        this.setSealed(stack, true);

        entries.accept(stack);
    }
}
