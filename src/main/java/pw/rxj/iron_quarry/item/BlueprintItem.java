package pw.rxj.iron_quarry.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipType;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
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

public class BlueprintItem extends Item implements IBlockAttackable, IHandledSmithing, IHandledGrinding, IHandledItemEntity, IHandledMainHandScrolling, IAlwaysRenderItemName, IHandledKeyedAction, IModifyItemGroupEntries {
    public BlueprintItem(Settings settings) {
        super(settings);
    }

    public static boolean isOf(ItemStack stack){
        return ZUtil.getBlockOrItem(stack) instanceof BlueprintItem;
    }
    public static boolean isNotOf(ItemStack stack){
        return !isOf(stack);
    }

    @Override
    public ItemStack getSmithingOutput(HandledSmithingRecipe handler, Inventory inventory, RegistryWrapper.WrapperLookup registryLookup) {
        ItemStack output = handler.getResult(registryLookup).copy();
        if(BlueprintItem.isNotOf(output)) return ItemStack.EMPTY;
        ItemStack base = inventory.getStack(1).copy();

        if(this.isSealed(base)) return ItemStack.EMPTY;
        if(this.getFirstPos(base).isEmpty()) return ItemStack.EMPTY;
        if(this.getSecondPos(base).isEmpty()) return ItemStack.EMPTY;

        this.setSealed(base, true);

        return base.copyComponentsToNewStack(output.getItem(), output.getCount());
    }
    @Override
    public ItemStack getSmithingOutputPreview(Ingredient base, Ingredient addition, ItemStack output) {
        ItemStack preview = output.copy();

        if(addition.test(Items.AMETHYST_SHARD.getDefaultStack())) {
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
        if(this.isSealed(itemEntity.getStack())) itemEntity.setInvulnerable(true);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean handleMainHandScrolling(ClientPlayerEntity player, ItemStack stack, ScrollDirection scrollDirection) {
        if(!MinecraftClient.getInstance().options.sneakKey.isPressed() || this.isSealed(stack)) return false;

        Direction direction = ZUtil.getFacingDirection(player.getYaw(), player.getPitch());
        int multiplier = MinecraftClient.getInstance().options.sprintKey.isPressed() ? 16 : 1;

        int amount = switch(scrollDirection) {
            case FORWARDS -> multiplier;
            case BACKWARDS -> -multiplier;
            default -> 0;
        };

        if(this.expandInDirection(stack, player, direction, amount)) {
            ZNetwork.sendToServer(PacketBlueprintExpand.bake(direction, (byte) amount));

            return true;
        }

        return false;
    }
    public boolean expandInDirection(ItemStack stack, PlayerEntity player, Direction direction, int amount) {
        World playerWorld = player.getWorld();

        RegistryKey<World> world = this.getWorldRegistryKey(stack).orElseGet(playerWorld::getRegistryKey);
        if(!world.equals(player.getWorld().getRegistryKey())) return false;

        BlockPos firstPos = this.getFirstPos(stack).orElseGet(player::getBlockPos);
        BlockPos secondPos = this.getSecondPos(stack).orElseGet(player::getBlockPos);

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
            firstPos = firstPos.add(vector);
        } else {
            secondPos = secondPos.add(vector);
        }

        this.setFirstPos(stack, playerWorld, firstPos);
        this.setSecondPos(stack, playerWorld, secondPos);

        return true;
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return this.isSealed(stack) & this.getMinedChunks(stack) > 0;
    }
    @Override
    public int getItemBarColor(ItemStack stack) {
        return 0x5980DF;
    }
    @Override
    public int getItemBarStep(ItemStack stack) {
        return (int) Math.ceil(((double) this.getMinedChunks(stack) / this.getMineableChunks(stack)) * 13.0);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType options) {
        RegistryKey<World> worldKey = this.getWorldRegistryKey(stack).orElse(null);
        Identifier worldId = worldKey != null ? worldKey.getValue() : null;

        BlockPos firstPos = this.getFirstPos(stack).orElse(null);
        BlockPos secondPos = this.getSecondPos(stack).orElse(null);
        boolean isSealed = this.isSealed(stack);

        MutableText LORE_EMPTY = ReadableString.translatable("item.iron_quarry.blueprint.lore.empty");

        MutableText LORE_WORLD = ReadableString.translatable("item.iron_quarry.blueprint.lore.world", ReadableString.textFrom(worldId).orElse(LORE_EMPTY));
        MutableText LORE_FIRST_POS = ReadableString.translatable("item.iron_quarry.blueprint.lore.first_pos", ReadableString.textFrom(firstPos).orElse(LORE_EMPTY));
        MutableText LORE_SECOND_POS = ReadableString.translatable("item.iron_quarry.blueprint.lore.second_pos", ReadableString.textFrom(secondPos).orElse(LORE_EMPTY));

        tooltip.add(LORE_WORLD);
        tooltip.add(LORE_FIRST_POS);
        tooltip.add(LORE_SECOND_POS);

        if(isSealed) {
            long mined = this.getMinedChunks(stack);
            long mineable = this.getMineableChunks(stack);
            float percentage = mineable > 0 ? (float) mined / mineable : 0;

            MutableText LORE_MINED = ReadableString.translatable("item.iron_quarry.blueprint.lore.mined", mined, mineable, ZUtil.expandableFixedFloat(percentage * 100));

            tooltip.add(ReadableString.space());
            tooltip.add(LORE_MINED);
            if(this.allChunksMined(stack)) {
                MutableText LORE_COMPLETED = ReadableString.translatable("item.iron_quarry.blueprint.lore.completed");
                tooltip.add(LORE_COMPLETED);
            }
        } else {
            MinecraftClient minecraftClient = MinecraftClient.getInstance();

            if(minecraftClient.currentScreen instanceof QuarryBlockScreen) {
                MutableText LORE_SEAL_FIRST = ReadableString.translatable("item.iron_quarry.blueprint.lore.seal_first");

                tooltip.add(ReadableString.space());
                tooltip.add(LORE_SEAL_FIRST);
            } else {
                tooltip.add(ReadableString.space());

                if(Screen.hasShiftDown()) {
                    GameOptions gameOptions = minecraftClient.options;

                    Text KEY_INTERACT = ReadableString.textFrom(gameOptions.useKey);
                    Text KEY_ATTACK = ReadableString.textFrom(gameOptions.attackKey);
                    Text KEY_SPRINT = ReadableString.textFrom(gameOptions.sprintKey);
                    Text KEY_SNEAK = ReadableString.textFrom(gameOptions.sneakKey);

                    MutableText LORE_MANUAL_0 = ReadableString.translatable("item.iron_quarry.blueprint.lore.manual.0", KEY_ATTACK, KEY_INTERACT);
                    MutableText LORE_MANUAL_1 = ReadableString.translatable("item.iron_quarry.blueprint.lore.manual.1", KEY_SNEAK);
                    MutableText LORE_MANUAL_2 = ReadableString.translatable("item.iron_quarry.blueprint.lore.manual.2");
                    MutableText LORE_MANUAL_3 = ReadableString.translatable("item.iron_quarry.blueprint.lore.manual.3", KEY_SPRINT);
                    MutableText LORE_MANUAL_4 = ReadableString.translatable("item.iron_quarry.blueprint.lore.manual.4");

                    tooltip.add(LORE_MANUAL_0);
                    tooltip.add(ReadableString.space());
                    tooltip.add(LORE_MANUAL_1);
                    tooltip.add(LORE_MANUAL_2);
                    tooltip.add(LORE_MANUAL_3);
                    tooltip.add(LORE_MANUAL_4);
                } else {
                    MutableText LORE_INSTRUCTIONS = ReadableString.translatable("item.iron_quarry.lore.instructions");

                    tooltip.add(LORE_INSTRUCTIONS);
                }
            }
        }
    }

    @Override
    public Text getName(ItemStack stack) {
        String translationKey = "item.iron_quarry.blueprint";
        if(this.isSealed(stack)) translationKey += ".sealed";

        MutableText itemName = Text.translatable(translationKey);

        Optional<BlockPos> firstPos = this.getFirstPos(stack);
        Optional<BlockPos> secondPos = this.getSecondPos(stack);

        if(firstPos.isEmpty() || secondPos.isEmpty()) {
            itemName.append(" ").append(Text.translatable("item.iron_quarry.blueprint.unbound"));
        } else if(this.allChunksMined(stack)) {
            itemName.append(" ").append(Text.translatable("item.iron_quarry.blueprint.completed"));
        } else {
            Text cuboidSize = ReadableString.textFrom(Cuboid.from(firstPos.get(), secondPos.get())).orElse(ReadableString.ERROR);

            itemName.append(" (").append(cuboidSize).append(")");
        }

        return itemName;
    }
    @Override
    public boolean renderItemName(ItemStack stack) {
        return !this.isSealed(stack) && this.getFirstPos(stack).isPresent() && this.getSecondPos(stack).isPresent() && !this.allChunksMined(stack);
    }

    public boolean hasGlint(ItemStack stack) {
        return this.isSealed(stack);
    }

    public void setSealed(ItemStack stack, boolean sealed){
        stack.set(ZComponents.BLUEPRINT_SEALED, sealed);
        stack.set(DataComponentTypes.RARITY, sealed ? Rarity.EPIC : Rarity.UNCOMMON);
    }
    public boolean isSealed(ItemStack stack){
        return stack.getOrDefault(ZComponents.BLUEPRINT_SEALED, false);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        if(player == null) return ActionResult.FAIL;

        if(!context.getHand().equals(Hand.MAIN_HAND)) return ActionResult.PASS;
        if(player.isCreative() && MinecraftClient.getInstance().options.sneakKey.isPressed()) return ActionResult.PASS;

        ItemStack stack = context.getStack();
        if(this.isSealed(stack)) return ActionResult.FAIL;

        BlockPos targetedPos = context.getBlockPos();
        this.setSecondPos(stack, context.getWorld(), targetedPos);
        Text secondPosText = ReadableString.textFrom(targetedPos).orElse(ReadableString.ERROR);

        player.sendMessage(ReadableString.translatable("item.iron_quarry.blueprint.overlay.second_pos_set", secondPosText), true);

        return ActionResult.SUCCESS;
    }
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
//        Main.LOGGER.info(user.shouldCancelInteraction());
        if(!world.isClient()) return TypedActionResult.fail(stack);
        if(!hand.equals(Hand.MAIN_HAND) || !user.isCreative()) return TypedActionResult.fail(stack);

        if(MinecraftClient.getInstance().options.sneakKey.isPressed()) {
            ZNetwork.sendToServer(PacketKeyedAction.bake("key.sneak", hand, ActionGoal.USE, BlockHitResult.createMissed(Vec3d.ZERO, Direction.UP, BlockPos.ORIGIN)));
            return TypedActionResult.success(stack, false);
        }

        return TypedActionResult.pass(stack);
    }
    @Override
    public void keyedUse(String keyName, ItemUsageContext context) {
        if(!keyName.equals("key.sneak")) return;

        ItemStack stack = context.getStack();
        PlayerEntity player = context.getPlayer();

        if(player != null && player.isCreative()) {
            this.setSealed(stack, !this.isSealed(stack));
            this.resetMinedChunks(stack);
        }
    }
    @Override
    public ActionResult attackOnBlock(PlayerEntity player, World world, Hand hand, BlockPos targetedPos, Direction direction) {
        if(!hand.equals(Hand.MAIN_HAND)) return ActionResult.PASS;

        ItemStack stack = player.getStackInHand(hand);
        if(this.isSealed(stack)) return ActionResult.FAIL;

        this.setFirstPos(stack, world, targetedPos);

        Text firstPosText = ReadableString.textFrom(targetedPos).orElse(ReadableString.ERROR);

        player.sendMessage(ReadableString.translatable("item.iron_quarry.blueprint.overlay.first_pos_set", firstPosText), true);

        return ActionResult.SUCCESS;
    }

    public void setWorld(ItemStack stack, World world) {
        this.setWorld(stack, world.getRegistryKey());
    }
    public void setWorld(ItemStack stack, RegistryKey<World> newWorldKey) {
        RegistryKey<World> oldWorldKey = this.getWorldRegistryKey(stack).orElse(null);
        if(ZUtil.equals(oldWorldKey, newWorldKey)) return;

        this.resetPositions(stack);

        stack.set(ZComponents.BLUEPRINT_WORLD, newWorldKey);

    }
    public Optional<RegistryKey<World>> getWorldRegistryKey(ItemStack stack) {
        return Optional.ofNullable(stack.get(ZComponents.BLUEPRINT_WORLD));
    }

    public void resetPositions(ItemStack stack) {
        stack.remove(ZComponents.BLUEPRINT_POSITION);
    }
    public void setFirstPos(ItemStack stack, @Nullable World world, BlockPos blockPos) {
        this.setPosition(DualPosition.FIRST, stack, world, blockPos);
    }
    public void setSecondPos(ItemStack stack, @Nullable World world, BlockPos blockPos) {
        this.setPosition(DualPosition.SECOND, stack, world, blockPos);
    }
    private void setPosition(DualPosition position, ItemStack stack, @Nullable World world, BlockPos blockPos) {
        if(world != null) {
            this.setWorld(stack, world);
            blockPos = ZUtil.limitInsideWorldBounds(blockPos, world);
        } else if(this.getWorldRegistryKey(stack).isEmpty()) {
            throw new IllegalStateException("BlueprintItem#setWorld has to be called first if no world is supplied.");
        }

        BlueprintPositionComponent blueprintPosition = stack.getOrDefault(ZComponents.BLUEPRINT_POSITION, BlueprintPositionComponent.twin(blockPos));

        blueprintPosition.setPosition(position, blockPos);

        stack.set(ZComponents.BLUEPRINT_POSITION, blueprintPosition);
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
    public void onModifyItemGroupEntry(FabricItemGroupEntries entries) {
        entries.add(this);

        ItemStack stack = new ItemStack(this);

        this.setWorld(stack,  World.OVERWORLD);
        this.setFirstPos(stack, null, new BlockPos(10000, 64, 10000));
        this.setSecondPos(stack, null, new BlockPos(-10000, -64, -10000));
        this.setSealed(stack, true);

        entries.add(stack);
    }
}
