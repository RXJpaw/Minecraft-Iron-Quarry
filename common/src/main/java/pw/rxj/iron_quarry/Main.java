package pw.rxj.iron_quarry;

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
import net.minecraft.world.item.crafting.RecipeType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.FormattedMessageFactory;
import org.apache.logging.log4j.message.Message;
import pw.rxj.iron_quarry.block.ZBlocks;
import pw.rxj.iron_quarry.blockentity.ZBlockEntities;
import pw.rxj.iron_quarry.compat.Compat;
import pw.rxj.iron_quarry.data.QuarryBlockScreenData;
import pw.rxj.iron_quarry.data.ZComponents;
import pw.rxj.iron_quarry.event.EnchantmentCallback;
import pw.rxj.iron_quarry.event.GameLifecycleCallback;
import pw.rxj.iron_quarry.event.PlayerInteractionCallback;
import pw.rxj.iron_quarry.event.ServerEntityCallback;
import pw.rxj.iron_quarry.event.ServerLevelCallback;
import pw.rxj.iron_quarry.factory.ZLootTables;
import pw.rxj.iron_quarry.interfaces.ICustomEnchantabilityProvider;
import pw.rxj.iron_quarry.interfaces.IHandledAttackOnBlock;
import pw.rxj.iron_quarry.interfaces.IHandledBlockUse;
import pw.rxj.iron_quarry.interfaces.IHandledItemEntity;
import pw.rxj.iron_quarry.interfaces.IHandledUseOnBlock;
import pw.rxj.iron_quarry.interfaces.ITickingInventoryItem;
import pw.rxj.iron_quarry.item.ZItems;
import pw.rxj.iron_quarry.network.ZNetwork;
import pw.rxj.iron_quarry.platform.Platform;
import pw.rxj.iron_quarry.recipe.HandledCraftingRecipe;
import pw.rxj.iron_quarry.recipe.HandledSmithingRecipe;
import pw.rxj.iron_quarry.resource.SilkTouchTradingCondition;
import pw.rxj.iron_quarry.resource.config.ConfigHandler;
import pw.rxj.iron_quarry.resource.config.ConfigSide;
import pw.rxj.iron_quarry.screen.QuarryBlockScreenHandler;
import pw.rxj.iron_quarry.util.ChunkLoadingManager;
import pw.rxj.iron_quarry.util.ZUtil;

public class Main {
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
	public static final ConfigHandler CONFIG = ConfigHandler.bake(Platform.LOADER.getConfigDir().resolve(MOD_ID));

	public static final MenuType<QuarryBlockScreenHandler> QUARRY_BLOCK_SCREEN_HANDLER = Platform.MENU.createMenuType(QuarryBlockScreenHandler::new, QuarryBlockScreenData.PACKET_CODEC);

	public static final ResourceKey<CreativeModeTab> ITEM_GROUP = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(MOD_ID, "universal"));


	public static void init() {
		Compat.init();

		CONFIG.read(ConfigSide.SERVER);
		GameLifecycleCallback.IMMINENT_REGISTRY_FREEZE.register(CONFIG::applyServerChanges);
		CONFIG.registerServer();

		ChunkLoadingManager.register();
		ZComponents.register();
		ZBlocks.register();
		ZBlockEntities.register();
		ZItems.register();

		GameLifecycleCallback.IMMINENT_FIRST_RELOAD.register(ZLootTables::register);

		PlayerInteractionCallback.ATTACK_BLOCK.register((player, world, hand, pos, direction) -> {
			if(ZUtil.getBlockOrItem(player.getItemInHand(hand)) instanceof IHandledAttackOnBlock item) {
				return item.handleAttackOnBlock(player, world, hand, pos, direction);
			} else {
				return InteractionResult.PASS;
			}
		});

		PlayerInteractionCallback.USE_BLOCK.register((player, world, hand, hitResult) -> {
			if(world.getBlockState(hitResult.getBlockPos()).getBlock() instanceof IHandledBlockUse block) {
				InteractionResult result = block.handleBlockUse(player, world, hand, hitResult);
				if(!result.equals(InteractionResult.PASS)) return result;
			}

			if(ZUtil.getBlockOrItem(player.getItemInHand(hand)) instanceof IHandledUseOnBlock item) {
				return item.handleUseOnBlock(player, world, hand, hitResult);
			}

			return InteractionResult.PASS;
		});

		EnchantmentCallback.ALLOW_ENCHANTING.register((enchantment, target) -> {
			if(ZUtil.getBlockOrItem(target) instanceof ICustomEnchantabilityProvider enchantabilityProvider) {
				return enchantabilityProvider.getViableEnchantments(target).stream().anyMatch(enchantment::is);
			}

			return null;
		});

		ServerEntityCallback.LOAD.register((entity, world) -> {
			if(entity instanceof ItemEntity itemEntity) {
				if(ZUtil.getBlockOrItem(itemEntity.getItem()) instanceof IHandledItemEntity handledItemEntity) {
					handledItemEntity.handleItemEntity(itemEntity);
				}
			}
		});

		ServerLevelCallback.START_TICK.register(world -> {
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

		Platform.REGISTRY.register(Registries.MENU, Identifier.fromNamespaceAndPath(MOD_ID, "quarry_block_screen_handler"), () -> QUARRY_BLOCK_SCREEN_HANDLER);

		Platform.REGISTRY.register(Registries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath(MOD_ID, "handled_crafting_recipe"), () -> HandledCraftingRecipe.SERIALIZER);
		Platform.REGISTRY.register(Registries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath(MOD_ID, "handled_smithing_recipe"), () -> HandledSmithingRecipe.SERIALIZER);
		Platform.RECIPE.synchronizeRecipes(HandledCraftingRecipe.SERIALIZER, RecipeType.CRAFTING);
		Platform.RECIPE.synchronizeRecipes(HandledSmithingRecipe.SERIALIZER, RecipeType.SMITHING);

		Platform.REGISTRY.register(Registries.LOOT_CONDITION_TYPE, SilkTouchTradingCondition.ID, () -> SilkTouchTradingCondition.CODEC);

		Platform.REGISTRY.register(Registries.CREATIVE_MODE_TAB, ITEM_GROUP.identifier(), () -> Platform.REGISTRY.createCreativeTabBuilder()
				.title(Component.translatable("itemGroup.iron_quarry.universal"))
				.icon(() -> ZBlocks.NETHERITE_QUARRY.getBlockItem().getDefaultInstance())
				.displayItems((displayParameters, entries) -> {
					ZBlocks.fillCreativeTab(entries);
					ZItems.fillCreativeTab(entries);
				})
				.build());

		ZNetwork.registerServer();

		LOGGER.info("Henlo from Iron Quarry :)");
	}
}
