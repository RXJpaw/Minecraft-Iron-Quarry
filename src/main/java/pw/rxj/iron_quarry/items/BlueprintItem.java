package pw.rxj.iron_quarry.items;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import pw.rxj.iron_quarry.interfaces.BlockAttackable;
import pw.rxj.iron_quarry.interfaces.IHandledItemEntity;
import pw.rxj.iron_quarry.interfaces.IHandledSmithing;
import pw.rxj.iron_quarry.recipes.HandledSmithingRecipe;
import pw.rxj.iron_quarry.util.ReadableString;
import pw.rxj.iron_quarry.util.ZUtil;

import java.util.ArrayList;
import java.util.List;

public class BlueprintItem extends Item implements BlockAttackable, IHandledSmithing, IHandledItemEntity {
    public BlueprintItem(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack getSmithingOutput(HandledSmithingRecipe handler, Inventory inventory) {
        ItemStack output = handler.getOutput().copy();
        ItemStack base = inventory.getStack(0).copy();

        if(isSealed(base)) return ItemStack.EMPTY;
        if(getFirstPos(base) == null) return ItemStack.EMPTY;
        if(getSecondPos(base) == null) return ItemStack.EMPTY;

        NbtCompound nbtCompound = base.getOrCreateNbt();
        nbtCompound.putBoolean("Sealed", true);

        output.setNbt(nbtCompound.copy());

        return output;
    }

    @Override
    public ItemStack getSmithingOutputPreview(Ingredient base, Ingredient addition, ItemStack output) {
        ItemStack preview = output.copy();

        if(addition.test(ZItems.CONDUCTIVE_AMETHYST.getItem().getDefaultStack())) {
            NbtCompound nbtCompound = preview.getOrCreateNbt();
            nbtCompound.putBoolean("Sealed", true);

            return preview;
        }

        return null;
    }

    @Override
    public void handleItemEntity(ItemEntity itemEntity) {
        if(isSealed(itemEntity.getStack())) itemEntity.setInvulnerable(true);
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
        RegistryKey<World> worldKey = getWorldRegistryKey(stack);
        Identifier worldId = worldKey != null ? worldKey.getValue() : null;

        BlockPos secondPos = getSecondPos(stack);
        BlockPos firstPos = getFirstPos(stack);

        if(firstPos == null || secondPos == null) {
            MutableText LORE_UNUSABLE = Text.translatable("item.iron_quarry.blueprint.lore.unbound");

            tooltip.add(LORE_UNUSABLE);
            tooltip.add(Text.empty());
        }

        MutableText LORE_POS_EMPTY = Text.translatable("item.iron_quarry.blueprint.lore.empty");

        MutableText LORE_WORLD = Text.translatable("item.iron_quarry.blueprint.lore.world", ReadableString.textFrom(worldId).orElse(LORE_POS_EMPTY));
        MutableText LORE_FIRST_POS = Text.translatable("item.iron_quarry.blueprint.lore.first_pos", ReadableString.textFrom(firstPos).orElse(LORE_POS_EMPTY));
        MutableText LORE_SECOND_POS = Text.translatable("item.iron_quarry.blueprint.lore.second_pos", ReadableString.textFrom(secondPos).orElse(LORE_POS_EMPTY));

        tooltip.add(LORE_WORLD);
        tooltip.add(LORE_FIRST_POS);
        tooltip.add(LORE_SECOND_POS);
    }

    @Override
    public Text getName(ItemStack stack) {
        String translationKey = "item.iron_quarry.blueprint";
        if(isSealed(stack)) translationKey += ".sealed";

        MutableText itemName = Text.translatable(translationKey);

        if(getFirstPos(stack) == null || getSecondPos(stack) == null) {
            itemName.append(" ").append(Text.translatable("item.iron_quarry.blueprint.unbound"));
        }

        return itemName;
    }
    @Override
    public Rarity getRarity(ItemStack stack) {
        return BlueprintItem.isSealed(stack) ? Rarity.EPIC : Rarity.UNCOMMON;
    }
    public boolean hasGlint(ItemStack stack) {
        return isSealed(stack);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if(!context.getHand().equals(Hand.MAIN_HAND)) return ActionResult.PASS;

        PlayerEntity player = context.getPlayer();
        if(player == null) return ActionResult.FAIL;
        ItemStack stack = context.getStack();
        if(isSealed(stack)) return ActionResult.FAIL;

        setWorld(stack, context.getWorld());

        BlockPos targetedPos = context.getBlockPos();
        NbtCompound blockPos = getBlockPosNbt(targetedPos);
        stack.getOrCreateNbt().put("SecondPosition", blockPos);

        player.sendMessage(Text.of(String.format("Second position set to: §n%s", ReadableString.from(targetedPos).orElse("<error>"))), true);

        return ActionResult.SUCCESS;
    }
    @Override
    public ActionResult attackOnBlock(PlayerEntity player, World world, Hand hand, BlockPos targetedPos, Direction direction) {
        if(!hand.equals(Hand.MAIN_HAND)) return ActionResult.PASS;

        ItemStack stack = player.getStackInHand(hand);
        if(isSealed(stack)) return ActionResult.FAIL;

        setWorld(stack, world);

        NbtCompound blockPos = getBlockPosNbt(targetedPos);
        stack.getOrCreateNbt().put("FirstPosition", blockPos);

        player.sendMessage(Text.of(String.format("First position set to: §n%s", ReadableString.from(targetedPos).orElse("<error>"))), true);

        return ActionResult.SUCCESS;
    }

    public static void setWorld(ItemStack stack, World world) {
        RegistryKey<World> oldWorldKey = getWorldRegistryKey(stack);
        RegistryKey<World> newWorldKey = world.getRegistryKey();

        if(!ZUtil.equals(oldWorldKey, newWorldKey)) {
            resetPositions(stack);
        }

        String stringifiedKey = ZUtil.toString(newWorldKey);
        stack.getOrCreateNbt().putString("World", stringifiedKey);
    }
    public static void resetPositions(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if(nbt == null) return;

        nbt.remove("SecondPosition");
        nbt.remove("FirstPosition");
    }

    public static BlockPos getFirstPos(ItemStack stack) {
        NbtCompound itemNbt = stack.getNbt();
        if(itemNbt == null) return null;
        NbtCompound firstPosNbt = itemNbt.getCompound("FirstPosition");
        if(firstPosNbt.isEmpty()) return null;

        return new BlockPos(firstPosNbt.getInt("x"), firstPosNbt.getInt("y"), firstPosNbt.getInt("z"));
    }
    public static BlockPos getSecondPos(ItemStack stack) {
        NbtCompound itemNbt = stack.getNbt();
        if(itemNbt == null) return null;
        NbtCompound secondPosNbt = itemNbt.getCompound("SecondPosition");
        if(secondPosNbt.isEmpty()) return null;

        return new BlockPos(secondPosNbt.getInt("x"), secondPosNbt.getInt("y"), secondPosNbt.getInt("z"));
    }
    public static int getMinedChunks(ItemStack stack) {
        NbtCompound itemNbt = stack.getNbt();
        if(itemNbt == null) return 0;

        return itemNbt.getInt("MinedChunks");
    }
    public static @Nullable RegistryKey<World> getWorldRegistryKey(ItemStack stack) {
        NbtCompound itemNbt = stack.getNbt();
        if(itemNbt == null) return null;

        String worldKey = itemNbt.getString("World");
        return ZUtil.toRegistryKey(worldKey);
    }
    public static boolean isSealed(ItemStack stack){
        if(!(stack.getItem() instanceof BlueprintItem)) return false;

        NbtCompound nbt = stack.getNbt();
        if(nbt == null) return false;

        return nbt.getBoolean("Sealed");
    }

    public static void increaseMinedChunks(ItemStack stack) {
        increaseMinedChunks(stack, 1);
    }
    public static void increaseMinedChunks(ItemStack stack, int amount) {
        int minedChunks = getMinedChunks(stack);

        stack.getOrCreateNbt().putInt("MinedChunks", minedChunks + amount);
    }

    public static List<ChunkPos> getNextChunkPos(ItemStack stack, int maxPositions){
        List<ChunkPos> chunkPosList = new ArrayList<>();

        for (int i = 0; i < maxPositions; i++) {
            ChunkPos chunkPos = getNextChunkPosWithOffset(stack, i);
            if(chunkPos == null) return chunkPosList;
            chunkPosList.add(chunkPos);
        }

        return chunkPosList;
    }
    public static ChunkPos getNextChunkPos(ItemStack stack) {
        return getNextChunkPosWithOffset(stack, 0);
    }
    public static ChunkPos getNextChunkPosWithOffset(ItemStack stack, int offset){
        BlockPos firstPos = getFirstPos(stack);
        if(firstPos == null) return null;
        BlockPos secondPos = getSecondPos(stack);
        if(secondPos == null) return null;

        int firstChunkX = firstPos.getX() >> 4;
        int firstChunkZ = firstPos.getZ() >> 4;
        int secondChunkX = secondPos.getX() >> 4;
        int secondChunkZ = secondPos.getZ() >> 4;

        int chunksOnX = Math.abs(firstChunkX - secondChunkX) + 1;
        int chunksOnZ = Math.abs(firstChunkZ - secondChunkZ) + 1;
        int chunksToMine = chunksOnX * chunksOnZ;

        int currentChunkIndex = getMinedChunks(stack) + offset;
        if(currentChunkIndex >= chunksToMine) return null;

        int minChunkX = Math.min(firstChunkX, secondChunkX);
        int minChunkZ = Math.min(firstChunkZ, secondChunkZ);

        int offsetChunkX = currentChunkIndex % chunksOnX;
        int offsetChunkZ = currentChunkIndex / chunksOnX;
        int currentChunkX = minChunkX + offsetChunkX;
        int currentChunkZ = minChunkZ + offsetChunkZ;

        return new ChunkPos(currentChunkX, currentChunkZ);
    }
}
