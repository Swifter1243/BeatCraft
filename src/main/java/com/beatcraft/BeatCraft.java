package com.beatcraft;

import com.beatcraft.blocks.ModBlocks;
import com.beatcraft.data.components.ModComponents;
import com.beatcraft.items.ModItems;
import com.beatcraft.items.group.ModItemGroup;
import com.beatcraft.networking.BeatCraftNetworking;
import com.beatcraft.networking.s2c.MapSyncS2CPayload;
import com.beatcraft.networking.s2c.PlayerDisconnectS2CPayload;
import com.beatcraft.world.gen.BeatCraftWorldGeneration;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static net.minecraft.server.command.CommandManager.literal;

public class BeatCraft implements ModInitializer {
	public static final String MOD_ID = "beatcraft";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static UUID currentTrackedPlayer = null;
	public static String currentTrackId = null;
	public static String currentSet = null;
	public static String currentDiff = null;

	@Override
	public void onInitialize() {
		LOGGER.info("hi :3");

		ModComponents.init();
		ModItems.init();
		ModBlocks.init();
		ModItemGroup.init();

		BeatCraftWorldGeneration.generateWorldGen();

		BeatCraftNetworking.init();

		registerCommands();

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			if (currentTrackedPlayer == handler.player.getUuid()) {
				currentTrackedPlayer = null;
				currentTrackId = null;
				currentSet = null;
				currentDiff = null;
			}
			PlayerLookup.all(server).forEach(p -> {
				if (p != handler.player) {
					ServerPlayNetworking.send(p, new PlayerDisconnectS2CPayload(handler.player.getUuid()));
				}
			});
		});

		ServerPlayConnectionEvents.JOIN.register((handler, packetSender, server) -> {
			BeatCraft.LOGGER.info("player connect!");
			if (currentTrackedPlayer != null) {
				packetSender.sendPacket(new MapSyncS2CPayload(currentTrackedPlayer, currentTrackId, currentSet, currentDiff));
			}
		});

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