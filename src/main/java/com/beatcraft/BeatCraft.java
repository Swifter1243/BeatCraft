package com.beatcraft;

import com.beatcraft.blocks.CornerLightTileBlock;
import com.beatcraft.blocks.EdgeLightTileBlock;
import com.beatcraft.blocks.ModBlocks;
import com.beatcraft.blocks.entity.ColorNoteDisplayBlockEntity;
import com.beatcraft.data.components.ModComponents;
import com.beatcraft.environment.StructurePlacer;
import com.beatcraft.items.ModItems;
import com.beatcraft.items.group.ModItemGroup;
import com.beatcraft.networking.BeatCraftNetworking;
import com.beatcraft.networking.s2c.MapSyncS2CPayload;
import com.beatcraft.networking.s2c.PlayerUntrackS2CPayload;
import com.beatcraft.world.FirstJoinState;
import com.beatcraft.world.PlacedEnvironmentState;
import com.beatcraft.world.gen.BeatCraftWorldGeneration;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.PersistentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class BeatCraft implements ModInitializer {
	public static final String MOD_ID = "beatcraft";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static UUID currentTrackedPlayer = null;
	public static String currentTrackId = null;
	public static String currentSet = null;
	public static String currentDiff = null;
	public static boolean isFlatWorld = false;

	private static final PersistentState.Type<FirstJoinState> joinStateType = new PersistentState.Type<>(
		FirstJoinState::new,
		FirstJoinState::fromNbt,
		null
	);

	private static final PersistentState.Type<PlacedEnvironmentState> placedEnvironmentType = new PersistentState.Type<>(
		PlacedEnvironmentState::new,
		PlacedEnvironmentState::fromNbt,
		null
	);

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}


	@Override
	public void onInitialize() {
		LOGGER.info("hi :3");

		ModComponents.init();
		ModItems.init();
		ModBlocks.init();
		ModItemGroup.init();

		BeatCraftWorldGeneration.generateWorldGen();

		BeatCraftNetworking.init();

		StructurePlacer.init();

		ColorNoteDisplayBlockEntity.init();

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
					ServerPlayNetworking.send(p, new PlayerUntrackS2CPayload(handler.player.getUuid()));
				}
			});
		});

		ServerPlayConnectionEvents.JOIN.register((handler, packetSender, server) -> {
			BeatCraft.LOGGER.info("player connect!");


			ServerPlayerEntity player = handler.player;
			ServerWorld world = player.getServerWorld();
			var stateManager = world.getPersistentStateManager();

			FirstJoinState state = stateManager.getOrCreate(joinStateType, "beatcraft_join_state");

			StructurePlacer.setState(stateManager.getOrCreate(placedEnvironmentType, "beatcraft_placed_environment"));

			isFlatWorld = server.getSaveProperties().isFlatWorld();

			if (!isFlatWorld && !state.hasJoined()) {
				state.markJoin();
			}

			if (!state.hasJoined()) {
				state.markJoin();

				placePlayArea(world);
				BeatCraft.LOGGER.info("Auto-placed playarea");

				player.teleport(world, 0.0, 0.0, 0.0, 0, 0);

				initSabers(player);

				initGameRules(server, world);

				player.sendMessage(Text.translatable("event.beatcraft.first_join_feedback"));

			}

			if (currentTrackedPlayer != null) {
				packetSender.sendPacket(new MapSyncS2CPayload(currentTrackedPlayer, currentTrackId, currentSet, currentDiff));
			}
		});

	}

	private void initSabers(PlayerEntity player) {
		ItemStack lSaber = new ItemStack(ModItems.SABER_ITEM);
		ItemStack rSaber = new ItemStack(ModItems.SABER_ITEM);
		lSaber.set(ModComponents.AUTO_SYNC_COLOR, 0);
		lSaber.set(ModComponents.SABER_COLOR_COMPONENT, 0xc03030);
		rSaber.set(ModComponents.AUTO_SYNC_COLOR, 1);
		rSaber.set(ModComponents.SABER_COLOR_COMPONENT, 0x2064a8);

		player.getInventory().offHand.set(0, lSaber);
		player.getInventory().setStack(0, rSaber);
	}

	private void initGameRules(MinecraftServer server, ServerWorld world) {
		server.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, server);
		server.getGameRules().get(GameRules.DO_WEATHER_CYCLE).set(false, server);
		server.getGameRules().get(GameRules.DO_TRADER_SPAWNING).set(false, server);
		server.getGameRules().get(GameRules.DO_MOB_SPAWNING).set(false, server);
		world.setTimeOfDay(18_000);
		server.setDifficulty(Difficulty.PEACEFUL, true);
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

	private static void fillBlocks(ServerWorld world, BlockPos minPos, BlockPos maxPos, BlockState blockState) {

		for (int x = minPos.getX(); x <= maxPos.getX(); x++) {
			for (int y = minPos.getY(); y <= maxPos.getY(); y++) {
				for (int z = minPos.getZ(); z <= maxPos.getZ(); z++) {
					world.setBlockState(new BlockPos(x, y, z), blockState);
				}
			}
		}
	}

	private static int generatePlayArea(CommandContext<ServerCommandSource> context) {

		ServerWorld world = context.getSource().getWorld();

		placePlayArea(world);

		context.getSource().sendFeedback(() -> Text.translatable("command.beatcraft.feedback.generated_playarea"), true);

		return 1;
	}

	private static void placePlayArea(ServerWorld world) {
		// play area tower
		fillBlocks(world, new BlockPos(-2, -64, -2), new BlockPos(1, -2, 1), ModBlocks.BLACK_MIRROR_BLOCK.getDefaultState());
		fillBlocks(world, new BlockPos(-2, -1, -2), new BlockPos(1, -1, 1), ModBlocks.REFLECTIVE_MIRROR_BLOCK.getDefaultState());

		// light rim on tower
		world.setBlockState(new BlockPos(1, 0, -2), ModBlocks.CORNER_LIGHT_TILE_BLOCK.getDefaultState().with(CornerLightTileBlock.FACE, Direction.DOWN).with(CornerLightTileBlock.ROTATION, Direction.EAST));
		world.setBlockState(new BlockPos(1, 0, -1), ModBlocks.EDGE_LIGHT_TILE_BLOCK.getDefaultState().with(EdgeLightTileBlock.FACE, Direction.DOWN).with(EdgeLightTileBlock.ROTATION, Direction.EAST));
		world.setBlockState(new BlockPos(1, 0, 0), ModBlocks.EDGE_LIGHT_TILE_BLOCK.getDefaultState().with(EdgeLightTileBlock.FACE, Direction.DOWN).with(EdgeLightTileBlock.ROTATION, Direction.EAST));

		world.setBlockState(new BlockPos(1, 0, 1), ModBlocks.CORNER_LIGHT_TILE_BLOCK.getDefaultState().with(CornerLightTileBlock.FACE, Direction.DOWN).with(CornerLightTileBlock.ROTATION, Direction.SOUTH));
		world.setBlockState(new BlockPos(0, 0, 1), ModBlocks.EDGE_LIGHT_TILE_BLOCK.getDefaultState().with(EdgeLightTileBlock.FACE, Direction.DOWN).with(EdgeLightTileBlock.ROTATION, Direction.SOUTH));
		world.setBlockState(new BlockPos(-1, 0, 1), ModBlocks.EDGE_LIGHT_TILE_BLOCK.getDefaultState().with(EdgeLightTileBlock.FACE, Direction.DOWN).with(EdgeLightTileBlock.ROTATION, Direction.SOUTH));

		world.setBlockState(new BlockPos(-2, 0, 1), ModBlocks.CORNER_LIGHT_TILE_BLOCK.getDefaultState().with(CornerLightTileBlock.FACE, Direction.DOWN).with(CornerLightTileBlock.ROTATION, Direction.WEST));
		world.setBlockState(new BlockPos(-2, 0, 0), ModBlocks.EDGE_LIGHT_TILE_BLOCK.getDefaultState().with(EdgeLightTileBlock.FACE, Direction.DOWN).with(EdgeLightTileBlock.ROTATION, Direction.WEST));
		world.setBlockState(new BlockPos(-2, 0, -1), ModBlocks.EDGE_LIGHT_TILE_BLOCK.getDefaultState().with(EdgeLightTileBlock.FACE, Direction.DOWN).with(EdgeLightTileBlock.ROTATION, Direction.WEST));

		world.setBlockState(new BlockPos(-2, 0, -2), ModBlocks.CORNER_LIGHT_TILE_BLOCK.getDefaultState().with(CornerLightTileBlock.FACE, Direction.DOWN).with(CornerLightTileBlock.ROTATION, Direction.NORTH));
		world.setBlockState(new BlockPos(-1, 0, -2), ModBlocks.EDGE_LIGHT_TILE_BLOCK.getDefaultState().with(EdgeLightTileBlock.FACE, Direction.DOWN).with(EdgeLightTileBlock.ROTATION, Direction.NORTH));
		world.setBlockState(new BlockPos(0, 0, -2), ModBlocks.EDGE_LIGHT_TILE_BLOCK.getDefaultState().with(EdgeLightTileBlock.FACE, Direction.DOWN).with(EdgeLightTileBlock.ROTATION, Direction.NORTH));

		// runway
		fillBlocks(world, new BlockPos(-2, -1, 8), new BlockPos(1, -1, 300), ModBlocks.BLACK_MIRROR_BLOCK.getDefaultState());

		// runway lights
		fillBlocks(world, new BlockPos(-2, 0, 8), new BlockPos(-2, 0, 290), ModBlocks.EDGE_LIGHT_TILE_BLOCK.getDefaultState().with(EdgeLightTileBlock.FACE, Direction.DOWN).with(EdgeLightTileBlock.ROTATION, Direction.WEST));
		fillBlocks(world, new BlockPos(1, 0, 8), new BlockPos(1, 0, 290), ModBlocks.EDGE_LIGHT_TILE_BLOCK.getDefaultState().with(EdgeLightTileBlock.FACE, Direction.DOWN).with(EdgeLightTileBlock.ROTATION, Direction.EAST));

		// runway front lights
		world.setBlockState(new BlockPos(1, -1, 7), ModBlocks.CORNER_LIGHT_TILE_BLOCK.getDefaultState().with(CornerLightTileBlock.FACE, Direction.SOUTH).with(CornerLightTileBlock.ROTATION, Direction.EAST));
		world.setBlockState(new BlockPos(0, -1, 7), ModBlocks.EDGE_LIGHT_TILE_BLOCK.getDefaultState().with(EdgeLightTileBlock.FACE, Direction.SOUTH).with(EdgeLightTileBlock.ROTATION, Direction.DOWN));
		world.setBlockState(new BlockPos(-1, -1, 7), ModBlocks.EDGE_LIGHT_TILE_BLOCK.getDefaultState().with(EdgeLightTileBlock.FACE, Direction.SOUTH).with(EdgeLightTileBlock.ROTATION, Direction.DOWN));
		world.setBlockState(new BlockPos(-2, -1, 7), ModBlocks.CORNER_LIGHT_TILE_BLOCK.getDefaultState().with(CornerLightTileBlock.FACE, Direction.SOUTH).with(CornerLightTileBlock.ROTATION, Direction.DOWN));

	}

	private static int outputCurrentEnvironment(CommandContext<ServerCommandSource> context) {
		if (StructurePlacer.currentStructure.isEmpty()) {
			context.getSource().sendFeedback(() -> Text.translatable("command.beatcraft.feedback.no_environment_placed"), false);
		} else {
			context.getSource().sendFeedback(() -> Text.translatable("command.beatcraft.feedback.current_environment", StructurePlacer.currentStructure), false);
		}
		return 1;
	}

	private static int removeEnvironment(CommandContext<ServerCommandSource> context) {

		if (StructurePlacer.removeStructure(context.getSource().getWorld())) {
			context.getSource().sendFeedback(() -> Text.translatable("command.beatcraft.feedback.removed_environment"), false);
		} else {
			context.getSource().sendFeedback(() -> Text.translatable("command.beatcraft.feedback.no_environment_to_remove"), false);
		}

		return 1;
	}

	private static int placeEnvironment(CommandContext<ServerCommandSource> context) {

		String environment = StringArgumentType.getString(context, "environment");

		if (StructurePlacer.placeStructureForced(environment, context.getSource().getWorld())) {
			context.getSource().sendFeedback(() -> Text.translatable("command.beatcraft.feedback.environment_placed", environment), true);
		} else {
			context.getSource().sendFeedback(() -> Text.translatable("command.beatcraft.feedback.invalid_environment", environment), true);
		}

		return 1;
	}

	private CompletableFuture<Suggestions> environmentSuggester(CommandContext<ServerCommandSource> context, SuggestionsBuilder suggestionsBuilder) {
		String partial = suggestionsBuilder.getRemaining();

		StructurePlacer.matchEnvironments(partial).forEach(suggestionsBuilder::suggest);

		return suggestionsBuilder.buildFuture();
	}

	private int centerPlayer(CommandContext<ServerCommandSource> context) {

		var player = context.getSource().getPlayer();

		if (player != null) {
			player.teleport(context.getSource().getWorld(), 0.0, 0.0, 0.0, 0, 0);
			return 1;
		}
		return -1;
	}


	private void registerCommands() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(literal("sabers").requires(source -> source.hasPermissionLevel(2))
					.executes(BeatCraft::giveSabers)
			);
			dispatcher.register(literal("playarea").requires(source -> source.hasPermissionLevel(2))
					.executes(BeatCraft::generatePlayArea)
			);
			dispatcher.register(literal("environment").requires(source -> source.hasPermissionLevel(2))
					.executes(BeatCraft::outputCurrentEnvironment)
					.then(literal("remove")
							.executes(BeatCraft::removeEnvironment)
					)
					.then(literal("place")
							.then(argument("environment", StringArgumentType.string()).suggests(this::environmentSuggester)
									.executes(BeatCraft::placeEnvironment)
							)
					)
			);
			dispatcher.register(literal("center").requires(source -> source.hasPermissionLevel(2))
					.executes(this::centerPlayer)
			);
		});
	}

}