package pw.rxj.iron_quarry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.item.v1.EnchantmentEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.util.TriState;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
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
import pw.rxj.iron_quarry.factory.ZTradeOffers;
import pw.rxj.iron_quarry.interfaces.IBlockAttackable;
import pw.rxj.iron_quarry.interfaces.ICustomEnchantabilityProvider;
import pw.rxj.iron_quarry.interfaces.IHandledItemEntity;
import pw.rxj.iron_quarry.interfaces.IHandledUseBlock;
import pw.rxj.iron_quarry.interfaces.ITickingInventoryItem;
import pw.rxj.iron_quarry.item.ZItems;
import pw.rxj.iron_quarry.network.ZNetwork;
import pw.rxj.iron_quarry.recipe.HandledSmithingRecipe;
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

	public static final ScreenHandlerType<QuarryBlockScreenHandler> QUARRY_BLOCK_SCREEN_HANDLER = new ExtendedScreenHandlerType<>(QuarryBlockScreenHandler::new, QuarryBlockScreenData.PACKET_CODEC);

	public static final RegistryKey<ItemGroup> ITEM_GROUP = RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier.of(MOD_ID, "universal"));


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

		GameLifecycleCallback.IMMINENT_FIRST_RELOAD.register(ZTradeOffers::register);
		GameLifecycleCallback.IMMINENT_FIRST_RELOAD.register(ZLootTables::register);

		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
			if(ZUtil.getBlockOrItem(player.getStackInHand(hand)) instanceof IBlockAttackable itemStack) {
				return itemStack.attackOnBlock(player, world, hand, pos, direction);
			} else {
				return ActionResult.PASS;
			}
		});

		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if(world.getBlockState(hitResult.getBlockPos()).getBlock() instanceof IHandledUseBlock block) {
				return block.useOnBlock(player, world, hand, hitResult);
			} else {
				return ActionResult.PASS;
			}
		});

		EnchantmentEvents.ALLOW_ENCHANTING.register((enchantment, target, enchantingContext) -> {
			if(ZUtil.getBlockOrItem(target) instanceof ICustomEnchantabilityProvider enchantabilityProvider) {
				return enchantabilityProvider.getViableEnchantments(target).stream().anyMatch(enchantment::matchesKey) ? TriState.TRUE : TriState.FALSE;
			}

			return TriState.DEFAULT;
		});

		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if(entity instanceof ItemEntity itemEntity) {
				if(ZUtil.getBlockOrItem(itemEntity.getStack()) instanceof IHandledItemEntity handledItemEntity) {
					handledItemEntity.handleItemEntity(itemEntity);
				}
			}
		});

		ServerTickEvents.START_WORLD_TICK.register(world -> {
			for (ServerPlayerEntity player : world.getPlayers()) {
				PlayerInventory inventory = player.getInventory();

				for (int slot = 0; slot < inventory.size(); slot++) {
					ItemStack stack = inventory.getStack(slot);

					if(ZUtil.getBlockOrItem(stack) instanceof ITickingInventoryItem tickingInventoryItem) {
						tickingInventoryItem.tick(player, world, stack);
					}
				}
			}
		});

		EnergyStorage.SIDED.registerForBlockEntity(QuarryBlockEntity::getEnergySideConfiguration, ZBlockEntities.QUARRY_BLOCK_ENTITY);
		ItemStorage.SIDED.registerForBlockEntity(QuarryBlockEntity::getItemSideConfiguration, ZBlockEntities.QUARRY_BLOCK_ENTITY);

		Registry.register(Registries.SCREEN_HANDLER, Identifier.of(MOD_ID, "quarry_block_screen_handler"), QUARRY_BLOCK_SCREEN_HANDLER);

		Registry.register(Registries.RECIPE_SERIALIZER, Identifier.of(MOD_ID, "handled_smithing_recipe"), HandledSmithingRecipe.SERIALIZER);

		Registry.register(Registries.ITEM_GROUP, ITEM_GROUP, FabricItemGroup.builder()
				.displayName(Text.translatable("itemGroup.iron_quarry.universal"))
				.icon(() -> ZBlocks.NETHERITE_QUARRY.getBlockItem().getDefaultStack())
				.build());

		ZNetwork.registerServer();

		LOGGER.info("Henlo from Iron Quarry :)");
	}
}
