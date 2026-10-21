package pw.rxj.iron_quarry.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTabOutput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.HashedStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.data.BlueprintPositionComponent;
import pw.rxj.iron_quarry.data.ZComponents;
import pw.rxj.iron_quarry.interfaces.*;
import pw.rxj.iron_quarry.network.PacketBlueprintExpand;
import pw.rxj.iron_quarry.network.PacketKeyedAction;
import pw.rxj.iron_quarry.network.ZNetwork;
import pw.rxj.iron_quarry.recipe.HandledSmithingRecipe;
import pw.rxj.iron_quarry.render.Cuboid;
import pw.rxj.iron_quarry.screen.QuarryBlockScreen;
import pw.rxj.iron_quarry.types.ActionGoal;
import pw.rxj.iron_quarry.types.DualPosition;
import pw.rxj.iron_quarry.types.ScrollDirection;
import pw.rxj.iron_quarry.util.ReadableString;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class BlueprintItem extends Item implements IBlockAttackable, IHandledSmithing, IHandledGrinding, IHandledItemEntity, IHandledMainHandScrolling, IAlwaysRenderItemName, IHandledKeyedAction, IModifyItemGroupEntries {
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
    public ItemStack getSmithingOutput(HandledSmithingRecipe handler, SmithingRecipeInput input) {
        ItemStack output = handler.getResultStack();
        if(BlueprintItem.isNotOf(output)) return ItemStack.EMPTY;
        ItemStack base = input.getItem(1).copy();

        if(this.isSealed(base)) return ItemStack.EMPTY;
        if(this.getFirstPos(base).isEmpty()) return ItemStack.EMPTY;
        if(this.getSecondPos(base).isEmpty()) return ItemStack.EMPTY;

        this.setSealed(base, true);

        return base.transmuteCopy(output.getItem(), output.getCount());
    }
    @Override
    public ItemStack getSmithingOutputPreview(Optional<Ingredient> base, Optional<Ingredient> addition, @Nullable TagKey<Item> additionTag, ItemStack output) {
        ItemStack preview = output.copy();

        if(Ingredient.testOptionalIngredient(addition, Items.AMETHYST_SHARD.getDefaultInstance())) {
            this.setSealed(preview, true);

            return preview;
        }

        return null;
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
        if(this.isSealed(itemEntity.getItem())) itemEntity.setPermanentlyInvulnerable(true);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean handleMainHandScrolling(LocalPlayer player, ItemStack stack, ScrollDirection scrollDirection) {
        if(!Minecraft.getInstance().options.keyShift.isDown() || this.isSealed(stack)) return false;

        Direction direction = ZUtil.getFacingDirection(player.getYRot(), player.getXRot());
        int multiplier = Minecraft.getInstance().options.keySprint.isDown() ? 16 : 1;

        int amount = switch(scrollDirection) {
            case FORWARDS -> multiplier;
            case BACKWARDS -> -multiplier;
            default -> 0;
        };

        if(this.expandInDirection(stack, player, direction, amount)) {
            ZNetwork.sendToServer(PacketBlueprintExpand.bake(direction, (byte) amount, HashedStack.create(stack, player.connection.decoratedHashOpsGenenerator())));

            return true;
        }

        return false;
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

            if(minecraftClient.gui.screen() instanceof QuarryBlockScreen) {
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

                    MutableComponent LORE_MANUAL_0 = ReadableString.translatable("item.iron_quarry.blueprint.lore.manual.0", KEY_ATTACK, KEY_INTERACT);
                    MutableComponent LORE_MANUAL_1 = ReadableString.translatable("item.iron_quarry.blueprint.lore.manual.1", KEY_SNEAK);
                    MutableComponent LORE_MANUAL_2 = ReadableString.translatable("item.iron_quarry.blueprint.lore.manual.2");
                    MutableComponent LORE_MANUAL_3 = ReadableString.translatable("item.iron_quarry.blueprint.lore.manual.3", KEY_SPRINT);
                    MutableComponent LORE_MANUAL_4 = ReadableString.translatable("item.iron_quarry.blueprint.lore.manual.4");

                    textConsumer.accept(LORE_MANUAL_0);
                    textConsumer.accept(ReadableString.space());
                    textConsumer.accept(LORE_MANUAL_1);
                    textConsumer.accept(LORE_MANUAL_2);
                    textConsumer.accept(LORE_MANUAL_3);
                    textConsumer.accept(LORE_MANUAL_4);
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
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if(player == null) return InteractionResult.FAIL;

        if(!context.getHand().equals(InteractionHand.MAIN_HAND)) return InteractionResult.PASS;
        if(player.isCreative() && player.isSecondaryUseActive()) return InteractionResult.PASS;

        ItemStack stack = context.getItemInHand();
        if(this.isSealed(stack)) return InteractionResult.FAIL;

        BlockPos targetedPos = context.getClickedPos();
        this.setSecondPos(stack, context.getLevel(), targetedPos);
        Component secondPosText = ReadableString.textFrom(targetedPos).orElse(ReadableString.ERROR);

        player.sendOverlayMessage(ReadableString.translatable("item.iron_quarry.blueprint.overlay.second_pos_set", secondPosText));

        return InteractionResult.SUCCESS;
    }
    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
//        Main.LOGGER.info(user.shouldCancelInteraction());
        if(!world.isClientSide()) return InteractionResult.FAIL;
        if(!hand.equals(InteractionHand.MAIN_HAND) || !user.isCreative()) return InteractionResult.FAIL;

        if(Minecraft.getInstance().options.keyShift.isDown()) {
            ZNetwork.sendToServer(PacketKeyedAction.bake("key.sneak", hand, ActionGoal.USE, BlockHitResult.miss(Vec3.ZERO, Direction.UP, BlockPos.ZERO)));
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }
    @Override
    public void keyedUse(String keyName, UseOnContext context) {
        if(!keyName.equals("key.sneak")) return;

        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();

        if(player != null && player.isCreative()) {
            this.setSealed(stack, !this.isSealed(stack));
            this.resetMinedChunks(stack);
        }
    }
    @Override
    public InteractionResult attackOnBlock(Player player, Level world, InteractionHand hand, BlockPos targetedPos, Direction direction) {
        if(!hand.equals(InteractionHand.MAIN_HAND)) return InteractionResult.PASS;

        ItemStack stack = player.getItemInHand(hand);
        if(this.isSealed(stack)) return InteractionResult.FAIL;

        this.setFirstPos(stack, world, targetedPos);

        Component firstPosText = ReadableString.textFrom(targetedPos).orElse(ReadableString.ERROR);

        player.sendOverlayMessage(ReadableString.translatable("item.iron_quarry.blueprint.overlay.first_pos_set", firstPosText));

        return InteractionResult.SUCCESS;
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
    private void setPosition(DualPosition position, ItemStack stack, @Nullable Level world, BlockPos blockPos) {
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

    @Override
    public void onModifyItemGroupEntry(FabricCreativeModeTabOutput entries) {
        entries.accept(this);

        ItemStack stack = new ItemStack(this);

        this.setWorld(stack,  Level.OVERWORLD);
        this.setFirstPos(stack, null, new BlockPos(10000, 64, 10000));
        this.setSecondPos(stack, null, new BlockPos(-10000, -64, -10000));
        this.setSealed(stack, true);

        entries.accept(stack);
    }
}
