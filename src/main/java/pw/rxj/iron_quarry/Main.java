package pw.rxj.iron_quarry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.item.v1.EnchantmentEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.util.TriState;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.FormattedMessageFactory;
import org.apache.logging.log4j.message.Message;
import pw.rxj.iron_quarry.block.ZBlocks;
import pw.rxj.iron_quarry.blockentity.QuarryBlockEntity;
import pw.rxj.iron_quarry.blockentity.ZBlockEntities;
import pw.rxj.iron_quarry.data.QuarryBlockScreenData;
import pw.rxj.iron_quarry.data.ZComponents;
import pw.rxj.iron_quarry.event.GameLifecycleCallback;
import pw.rxj.iron_quarry.factory.ZLootTables;
import pw.rxj.iron_quarry.interfaces.IBlockAttackable;
import pw.rxj.iron_quarry.interfaces.IBlockUsable;
import pw.rxj.iron_quarry.interfaces.ICustomEnchantabilityProvider;
import pw.rxj.iron_quarry.interfaces.IHandledItemEntity;
import pw.rxj.iron_quarry.interfaces.IHandledUseBlock;
import pw.rxj.iron_quarry.interfaces.ITickingInventoryItem;
import pw.rxj.iron_quarry.item.ZItems;
import pw.rxj.iron_quarry.network.ZNetwork;
import pw.rxj.iron_quarry.recipe.HandledCraftingRecipe;
import pw.rxj.iron_quarry.recipe.HandledSmithingRecipe;
import pw.rxj.iron_quarry.resource.SilkTouchTradingCondition;
import pw.rxj.iron_quarry.resource.config.ConfigHandler;
import pw.rxj.iron_quarry.screen.QuarryBlockScreenHandler;
import pw.rxj.iron_quarry.util.ChunkLoadingManager;
import pw.rxj.iron_quarry.util.ZUtil;
import team.reborn.energy.api.EnergyStorage;

public class Main implements ModInitializer {
	public static final String MOD_ID = "iron_quarry";
	public static final Logger LOGGER = LogManager.getLogger("Iron Quarry",
		new FormattedMessageFactory() {
			@Override
			public Message newMessage(String message) {
				return super.newMessage("[Iron Quarry] " + message);
			}
			@Override
			public Message newMessage(String message, Object p0) {
				return super.newMessage("[Iron Quarry] " + message, p0);
			}
			@Override
			public Message newMessage(String message, Object p0, Object p1) {
				return super.newMessage("[Iron Quarry] " + message, p0, p1);
			}
		}
	);
	public static final ConfigHandler CONFIG = ConfigHandler.bake(FabricLoader.getInstance().getConfigDir().resolve(MOD_ID));

	public static final MenuType<QuarryBlockScreenHandler> QUARRY_BLOCK_SCREEN_HANDLER = new ExtendedMenuType<>(QuarryBlockScreenHandler::new, QuarryBlockScreenData.PACKET_CODEC);

	public static final ResourceKey<CreativeModeTab> ITEM_GROUP = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(MOD_ID, "universal"));


	@Override
	public void onInitialize() {
		CONFIG.read(EnvType.SERVER);
		CONFIG.applyServerChanges();
		CONFIG.registerServer();

		ChunkLoadingManager.register();
		ZBlockEntities.register();
		ZComponents.register();
		ZBlocks.register();
		ZItems.register();

		GameLifecycleCallback.IMMINENT_FIRST_RELOAD.register(ZLootTables::register);
		ResourceConditions.register(SilkTouchTradingCondition.TYPE);

		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
			if(ZUtil.getBlockOrItem(player.getItemInHand(hand)) instanceof IBlockAttackable itemStack) {
				return itemStack.attackOnBlock(player, world, hand, pos, direction);
			} else {
				return InteractionResult.PASS;
			}
		});

		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if(world.getBlockState(hitResult.getBlockPos()).getBlock() instanceof IHandledUseBlock block) {
				InteractionResult result = block.useOnBlock(player, world, hand, hitResult);
				if(!result.equals(InteractionResult.PASS)) return result;
			}

			if(ZUtil.getBlockOrItem(player.getItemInHand(hand)) instanceof IBlockUsable item) {
				return item.useOnBlock(player, world, hand, hitResult);
			}

			return InteractionResult.PASS;
		});

		EnchantmentEvents.ALLOW_ENCHANTING.register((enchantment, target, enchantingContext) -> {
			if(ZUtil.getBlockOrItem(target) instanceof ICustomEnchantabilityProvider enchantabilityProvider) {
				return enchantabilityProvider.getViableEnchantments(target).stream().anyMatch(enchantment::is) ? TriState.TRUE : TriState.FALSE;
			}

			return TriState.DEFAULT;
		});

		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if(entity instanceof ItemEntity itemEntity) {
				if(ZUtil.getBlockOrItem(itemEntity.getItem()) instanceof IHandledItemEntity handledItemEntity) {
					handledItemEntity.handleItemEntity(itemEntity);
				}
			}
		});

		ServerTickEvents.START_LEVEL_TICK.register(world -> {
			for (ServerPlayer player : world.players()) {
				Inventory inventory = player.getInventory();

				for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
					ItemStack stack = inventory.getItem(slot);

					if(ZUtil.getBlockOrItem(stack) instanceof ITickingInventoryItem tickingInventoryItem) {
						tickingInventoryItem.tick(player, world, stack);
					}
				}
			}
		});

		EnergyStorage.SIDED.registerForBlockEntity(QuarryBlockEntity::getEnergySideConfiguration, ZBlockEntities.QUARRY_BLOCK_ENTITY);
		ItemStorage.SIDED.registerForBlockEntity(QuarryBlockEntity::getItemSideConfiguration, ZBlockEntities.QUARRY_BLOCK_ENTITY);

		Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath(MOD_ID, "quarry_block_screen_handler"), QUARRY_BLOCK_SCREEN_HANDLER);

		Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath(MOD_ID, "handled_crafting_recipe"), HandledCraftingRecipe.SERIALIZER);
		Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath(MOD_ID, "handled_smithing_recipe"), HandledSmithingRecipe.SERIALIZER);
		RecipeSynchronization.synchronizeRecipeSerializer(HandledCraftingRecipe.SERIALIZER);
		RecipeSynchronization.synchronizeRecipeSerializer(HandledSmithingRecipe.SERIALIZER);

		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ITEM_GROUP, FabricCreativeModeTab.builder()
				.title(Component.translatable("itemGroup.iron_quarry.universal"))
				.icon(() -> ZBlocks.NETHERITE_QUARRY.getBlockItem().getDefaultInstance())
				.build());

		ZNetwork.registerServer();

		LOGGER.info("Henlo from Iron Quarry :)");
	}
}
