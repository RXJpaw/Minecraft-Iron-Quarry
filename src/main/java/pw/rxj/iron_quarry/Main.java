package pw.rxj.iron_quarry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.FormattedMessageFactory;
import org.apache.logging.log4j.message.Message;
import pw.rxj.iron_quarry.block.ZBlocks;
import pw.rxj.iron_quarry.blockentity.QuarryBlockEntity;
import pw.rxj.iron_quarry.blockentity.ZBlockEntities;
import pw.rxj.iron_quarry.event.GameLifecycleCallback;
import pw.rxj.iron_quarry.factory.ZTradeOffers;
import pw.rxj.iron_quarry.interfaces.BlockAttackable;
import pw.rxj.iron_quarry.interfaces.IHandledItemEntity;
import pw.rxj.iron_quarry.item.ZItems;
import pw.rxj.iron_quarry.network.ZNetwork;
import pw.rxj.iron_quarry.recipe.HandledCraftingRecipe;
import pw.rxj.iron_quarry.recipe.HandledSmithingRecipe;
import pw.rxj.iron_quarry.resource.ConfigHandler;
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


	public static final ScreenHandlerType<QuarryBlockScreenHandler> QUARRY_BLOCK_SCREEN_HANDLER = new ExtendedScreenHandlerType<>(QuarryBlockScreenHandler::new);

	public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.create(new Identifier(MOD_ID, "universal")).icon(() -> ZBlocks.NETHERITE_QUARRY.getBlockItem().getDefaultStack()).build();


	@Override
	public void onInitialize() {
		CONFIG.read(EnvType.SERVER);
		CONFIG.registerServer();

		ChunkLoadingManager.register();
		ZBlockEntities.register();
		ZBlocks.register();
		ZItems.register();

		GameLifecycleCallback.IMMINENT_FIRST_RELOAD.register(ZTradeOffers::register);

		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
			if(ZUtil.getBlockOrItem(player.getStackInHand(hand)) instanceof BlockAttackable itemStack) {
				return itemStack.attackOnBlock(player, world, hand, pos, direction);
			} else {
				return ActionResult.PASS;
			}
		});

		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if(entity instanceof ItemEntity itemEntity) {
				if(ZUtil.getBlockOrItem(itemEntity.getStack()) instanceof IHandledItemEntity handledItemEntity) {
					handledItemEntity.handleItemEntity(itemEntity);
				}
			}
		});

		EnergyStorage.SIDED.registerForBlockEntity(QuarryBlockEntity::getEnergySideConfiguration, ZBlockEntities.QUARRY_BLOCK_ENTITY);
		FluidStorage.SIDED.registerForBlockEntity(QuarryBlockEntity::getFluidSideConfiguration, ZBlockEntities.QUARRY_BLOCK_ENTITY);
		ItemStorage.SIDED.registerForBlockEntity(QuarryBlockEntity::getItemSideConfiguration, ZBlockEntities.QUARRY_BLOCK_ENTITY);

		Registry.register(Registry.SCREEN_HANDLER, new Identifier(MOD_ID, "quarry_block_screen_handler"), QUARRY_BLOCK_SCREEN_HANDLER);

		Registry.register(Registry.RECIPE_SERIALIZER, new Identifier(MOD_ID, "handled_crafting_recipe"), HandledCraftingRecipe.SERIALIZER);
		Registry.register(Registry.RECIPE_SERIALIZER, new Identifier(MOD_ID, "handled_smithing_recipe"), HandledSmithingRecipe.SERIALIZER);

		ZNetwork.registerServer();

		LOGGER.info("Henlo from Iron Quarry :)");
	}
}
