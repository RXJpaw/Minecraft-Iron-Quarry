package pw.rxj.iron_quarry.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.interfaces.*;
import pw.rxj.iron_quarry.network.PacketBlueprintExpand;
import pw.rxj.iron_quarry.network.ZNetwork;
import pw.rxj.iron_quarry.recipe.HandledSmithingRecipe;
import pw.rxj.iron_quarry.render.Cuboid;
import pw.rxj.iron_quarry.screen.QuarryBlockScreen;
import pw.rxj.iron_quarry.types.ScrollDirection;
import pw.rxj.iron_quarry.util.ReadableString;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BlueprintItem extends Item implements BlockAttackable, IHandledSmithing, IHandledGrinding, IHandledItemEntity, IHandledMainHandScrolling, IAlwaysRenderItemName {
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
    public ItemStack getSmithingOutput(HandledSmithingRecipe handler, Inventory inventory) {
        ItemStack output = handler.getOutput().copy();
        if(BlueprintItem.isNotOf(output)) return ItemStack.EMPTY;
        ItemStack base = inventory.getStack(0).copy();

        if(this.isSealed(base)) return ItemStack.EMPTY;
        if(this.getFirstPos(base).isEmpty()) return ItemStack.EMPTY;
        if(this.getSecondPos(base).isEmpty()) return ItemStack.EMPTY;

        this.setSealed(base, true);
        output.setNbt(base.getNbt());

        return output;
    }
    @Override
    public ItemStack getSmithingOutputPreview(Ingredient base, Ingredient addition, ItemStack output) {
        ItemStack preview = output.copy();

        if(addition.test(ZItems.CONDUCTIVE_AMETHYST.getItem().getDefaultStack())) {
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
    public ItemStack getGrindingOutput(ItemStack stack, ItemStack follower) {
        if(!follower.isEmpty()) return null;
        ItemStack output = stack.copy();

        NbtCompound nbtCompound = output.getNbt();
        if(nbtCompound == null) return null;

        nbtCompound.remove("Sealed");
        nbtCompound.remove("MinedChunks");

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

    private NbtCompound getBlockPosNbt(BlockPos blockPos) {
        int x = blockPos.getX();
        int y = blockPos.getY();
        int z = blockPos.getZ();

        NbtCompound blockPosNbt = new NbtCompound();
        blockPosNbt.putInt("x", x);
        blockPosNbt.putInt("y", y);
        blockPosNbt.putInt("z", z);

        return blockPosNbt;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
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

            tooltip.add(Text.empty());
            tooltip.add(LORE_MINED);

            if(this.allChunksMined(stack)) {
                MutableText LORE_COMPLETED = ReadableString.translatable("item.iron_quarry.blueprint.lore.completed");
                tooltip.add(LORE_COMPLETED);
            }
        } else {
            MinecraftClient minecraftClient = MinecraftClient.getInstance();

            if(minecraftClient.currentScreen instanceof QuarryBlockScreen) {
                MutableText LORE_SEAL_FIRST = ReadableString.translatable("item.iron_quarry.blueprint.lore.seal_first");

                tooltip.add(Text.empty());
                tooltip.add(LORE_SEAL_FIRST);
            } else {
                tooltip.add(Text.empty());

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
                    tooltip.add(Text.empty());
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
    @Override
    public Rarity getRarity(ItemStack stack) {
        return this.isSealed(stack) ? Rarity.EPIC : Rarity.UNCOMMON;
    }
    public boolean hasGlint(ItemStack stack) {
        return isSealed(stack);
    }

    public void setSealed(ItemStack stack, boolean sealed){
        if(sealed) {
            stack.getOrCreateNbt().putBoolean("Sealed", true);
        } else if(stack.getNbt() != null) {
            stack.getNbt().remove("Sealed");
        }
    }
    public boolean isSealed(ItemStack stack){
        NbtCompound nbt = stack.getNbt();
        if(nbt == null) return false;

        return nbt.getBoolean("Sealed");
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if(!context.getHand().equals(Hand.MAIN_HAND)) return ActionResult.PASS;

        PlayerEntity player = context.getPlayer();
        if(player == null) return ActionResult.FAIL;
        ItemStack stack = context.getStack();
        if(this.isSealed(stack)) return ActionResult.FAIL;

        BlockPos targetedPos = context.getBlockPos();

        this.setSecondPos(stack, context.getWorld(), targetedPos);

        Text secondPosText = ReadableString.textFrom(targetedPos).orElse(ReadableString.ERROR);

        player.sendMessage(ReadableString.translatable("item.iron_quarry.blueprint.overlay.second_pos_set", secondPosText), true);

        return ActionResult.SUCCESS;
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

        String worldKey = ZUtil.toString(newWorldKey);
        stack.getOrCreateNbt().putString("World", worldKey);

    }
    public Optional<RegistryKey<World>> getWorldRegistryKey(ItemStack stack) {
        NbtCompound itemNbt = stack.getNbt();
        if(itemNbt == null) return Optional.empty();

        String worldKey = itemNbt.getString("World");
        return Optional.ofNullable(ZUtil.toRegistryKey(worldKey));
    }

    public void resetPositions(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if(nbt == null) return;

        nbt.remove(Position.FIRST.getNbtLiteral());
        nbt.remove(Position.SECOND.getNbtLiteral());
    }
    public void setFirstPos(ItemStack stack, @Nullable World world, BlockPos blockPos) {
        this.setPosition(Position.FIRST, stack, world, blockPos);
    }
    public void setSecondPos(ItemStack stack, @Nullable World world, BlockPos blockPos) {
        this.setPosition(Position.SECOND, stack, world, blockPos);
    }
    private void setPosition(Position position, ItemStack stack, @Nullable World world, BlockPos blockPos) {
        if(world != null) {
            this.setWorld(stack, world);
            blockPos = ZUtil.limitInsideWorldBounds(blockPos, world);
        } else if(this.getWorldRegistryKey(stack).isEmpty()) {
            throw new IllegalStateException("BlueprintItem#setWorld has to be called first if no world is supplied.");
        }

        stack.getOrCreateNbt().put(position.getNbtLiteral(), this.getBlockPosNbt(blockPos));
    }
    public Optional<BlockPos> getFirstPos(ItemStack stack) {
        return Optional.ofNullable(this.getPosition(Position.FIRST, stack));
    }
    public Optional<BlockPos> getSecondPos(ItemStack stack) {
        return Optional.ofNullable(this.getPosition(Position.SECOND, stack));
    }
    private @Nullable BlockPos getPosition(Position position, ItemStack stack) {
        NbtCompound itemNbt = stack.getNbt();
        if(itemNbt == null) return null;

        NbtCompound positionNbt = itemNbt.getCompound(position.getNbtLiteral());
        if(positionNbt.isEmpty()) return null;

        return new BlockPos(positionNbt.getInt("x"), positionNbt.getInt("y"), positionNbt.getInt("z"));
    }

    public long getMinedChunks(ItemStack stack) {
        NbtCompound itemNbt = stack.getNbt();
        if(itemNbt == null) return 0;

        return itemNbt.getInt("MinedChunks");
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
    public void increaseMinedChunks(ItemStack stack) {
        this.increaseMinedChunks(stack, 1);
    }
    public void increaseMinedChunks(ItemStack stack, long amount) {
        long minedChunks = this.getMinedChunks(stack);

        stack.getOrCreateNbt().putLong("MinedChunks", minedChunks + amount);
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
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        super.appendStacks(group, stacks);

        if(this.isIn(group)) {
            ItemStack stack = new ItemStack(this);

            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            ClientWorld clientWorld = minecraftClient.world;

            this.setWorld(stack, clientWorld == null ? World.OVERWORLD : clientWorld.getRegistryKey());
            this.setFirstPos(stack, null, new BlockPos(10000, 64, 10000));
            this.setSecondPos(stack, null, new BlockPos(-10000, -64, -10000));
            this.setSealed(stack, true);

            stacks.add(stack);
        }
    }

    private enum Position {
        FIRST(0, "FirstPosition"),
        SECOND(1, "SecondPosition");

        private final int id;
        private final String nbtLiteral;

        private Position(int id, String nbtLiteral) {
            this.id = id;
            this.nbtLiteral = nbtLiteral;
        }

        public int getId() {
            return id;
        }
        public String getNbtLiteral() {
            return nbtLiteral;
        }
    }
}
