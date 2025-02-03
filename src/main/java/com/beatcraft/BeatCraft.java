package com.beatcraft;

import com.beatcraft.blocks.ModBlocks;
import com.beatcraft.data.components.ModComponents;
import com.beatcraft.items.ModItems;
import com.beatcraft.items.group.ModItemGroup;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraft.server.command.CommandManager.literal;

public class BeatCraft implements ModInitializer {
	public static final String MOD_ID = "beatcraft";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("hi :3");

		ModComponents.init();
		ModItems.init();
		ModBlocks.init();
		ModItemGroup.init();

		registerCommands();

	}


	private static int giveSabers(CommandContext<ServerCommandSource> context) {

		PlayerEntity player = context.getSource().getPlayer();

		if (player != null) {
			ItemStack lSaber = new ItemStack(ModItems.SABER_ITEM);
			ItemStack rSaber = new ItemStack(ModItems.SABER_ITEM);

			lSaber.set(ModComponents.AUTO_SYNC_COLOR, 0);
			lSaber.set(ModComponents.SABER_COLOR_COMPONENT, 0xc03030);

			rSaber.set(ModComponents.AUTO_SYNC_COLOR, 1);
			rSaber.set(ModComponents.SABER_COLOR_COMPONENT, 0x2064a8);

			player.giveItemStack(lSaber);
			player.giveItemStack(rSaber);

		}

		return 1;
	}


	private void registerCommands() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(literal("sabers")
					.executes(BeatCraft::giveSabers)
			);
		});
	}

}