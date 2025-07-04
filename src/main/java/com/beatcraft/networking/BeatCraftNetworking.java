package com.beatcraft.networking;

import com.beatcraft.BeatCraft;
import com.beatcraft.environment.StructurePlacer;
import com.beatcraft.networking.c2s.*;
import com.beatcraft.networking.s2c.*;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class BeatCraftNetworking {

    // S2C
    public static final Identifier SABER_SYNC_S2C = Identifier.of(BeatCraft.MOD_ID, "saber_sync_s2c");
    public static final Identifier MAP_SYNC_S2C = Identifier.of(BeatCraft.MOD_ID, "map_sync_s2c");
    public static final Identifier BEAT_SYNC_S2C = Identifier.of(BeatCraft.MOD_ID, "beat_sync_s2c");
    public static final Identifier PLAYER_DISCONNECT_S2C = Identifier.of(BeatCraft.MOD_ID, "player_disconnect_s2c");
    public static final Identifier SPEED_SYNC_S2C = Identifier.of(BeatCraft.MOD_ID, "speed_sync_s2c");
    public static final Identifier SONG_PAUSE_S2C = Identifier.of(BeatCraft.MOD_ID, "song_pause_s2c");
    public static final Identifier SCENE_SYNC_S2C = BeatCraft.id("scene_sync_s2c");

    // C2S
    public static final Identifier SABER_SYNC_C2S = Identifier.of(BeatCraft.MOD_ID, "saber_sync_c2s");
    public static final Identifier MAP_SYNC_C2S = Identifier.of(BeatCraft.MOD_ID, "map_sync_c2s");
    public static final Identifier BEAT_SYNC_C2S = Identifier.of(BeatCraft.MOD_ID, "beat_sync_c2s");
    public static final Identifier SPEED_SYNC_C2S = Identifier.of(BeatCraft.MOD_ID, "speed_sync_c2s");
    public static final Identifier SONG_PAUSE_C2S = Identifier.of(BeatCraft.MOD_ID, "song_pause_c2s");
    public static final Identifier PLACE_ENVIRONMENT_C2S = Identifier.of(BeatCraft.MOD_ID, "place_environment_c2s");
    public static final Identifier SCENE_SYNC_C2S = BeatCraft.id("scene_sync_c2s");

    public static void init() {

        // Server to Client
        PayloadTypeRegistry.playS2C().register(SaberSyncS2CPayload.ID, SaberSyncS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(MapSyncS2CPayload.ID, MapSyncS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(BeatSyncS2CPayload.ID, BeatSyncS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(PlayerUntrackS2CPayload.ID, PlayerUntrackS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SpeedSyncS2CPayload.ID, SpeedSyncS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SongPauseS2CPayload.ID, SongPauseS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SceneSyncS2CPayload.ID, SceneSyncS2CPayload.CODEC);

        // Client to Server
        PayloadTypeRegistry.playC2S().register(SaberSyncC2SPayload.ID, SaberSyncC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(MapSyncC2SPayload.ID, MapSyncC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(BeatSyncC2SPayload.ID, BeatSyncC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SpeedSyncC2SPayload.ID, SpeedSyncC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SongPauseC2SPayload.ID, SongPauseC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(PlaceEnvironmentStructureC2SPayload.ID, PlaceEnvironmentStructureC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SceneSyncC2SPayload.ID, SceneSyncC2SPayload.CODEC);

        // receivers
        ServerPlayNetworking.registerGlobalReceiver(SaberSyncC2SPayload.ID, BeatCraftNetworking::handleSaberSyncPayload);
        ServerPlayNetworking.registerGlobalReceiver(MapSyncC2SPayload.ID, BeatCraftNetworking::handleMapSyncPayload);
        ServerPlayNetworking.registerGlobalReceiver(BeatSyncC2SPayload.ID, BeatCraftNetworking::handleBeatSyncPayload);
        ServerPlayNetworking.registerGlobalReceiver(SpeedSyncC2SPayload.ID, BeatCraftNetworking::handleSpeedSyncPayload);
        ServerPlayNetworking.registerGlobalReceiver(SongPauseC2SPayload.ID, BeatCraftNetworking::handlePausePayload);
        ServerPlayNetworking.registerGlobalReceiver(PlaceEnvironmentStructureC2SPayload.ID, BeatCraftNetworking::placeStructure);
        ServerPlayNetworking.registerGlobalReceiver(SceneSyncC2SPayload.ID, BeatCraftNetworking::handleSceneSync);

    }

    private static void handleSaberSyncPayload(SaberSyncC2SPayload payload, ServerPlayNetworking.Context context) {
        PlayerEntity sending_player = context.player();
        UUID uuid = sending_player.getUuid();
        PlayerLookup.all(context.server()).forEach(player -> {
            ServerPlayNetworking.send(player, new SaberSyncS2CPayload(
                uuid,
                payload.leftPos(), payload.leftRot(),
                payload.rightPos(), payload.rightRot(),
                payload.headPos(), payload.headRot()
            ));
        });
    }

    private static void handleMapSyncPayload(MapSyncC2SPayload payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            PlayerEntity player = context.player();
            UUID uuid = player.getUuid();
            BeatCraft.currentTrackId = payload.uid();
            BeatCraft.currentTrackedPlayer = uuid;
            BeatCraft.currentSet = payload.set();
            BeatCraft.currentDiff = payload.diff();
            BeatCraft.currentModifiers = payload.modifiers();
            PlayerLookup.all(context.server()).forEach(pl -> {
                if (pl == player) return;
                ServerPlayNetworking.send(pl, new MapSyncS2CPayload(uuid, payload.uid(), payload.set(), payload.diff(), payload.modifiers()));
            });
        });
    }

    private static void handleBeatSyncPayload(BeatSyncC2SPayload payload, ServerPlayNetworking.Context context) {
        PlayerLookup.all(context.server()).forEach(pl -> {
            ServerPlayNetworking.send(pl, new BeatSyncS2CPayload(payload.beat()));
        });
    }

    private static void handleSpeedSyncPayload(SpeedSyncC2SPayload payload, ServerPlayNetworking.Context context) {

        PlayerLookup.all(context.server()).forEach(pl -> {
            ServerPlayNetworking.send(pl, new SpeedSyncS2CPayload(payload.speed()));
        });
    }

    private static void handleSceneSync(SceneSyncC2SPayload payload, ServerPlayNetworking.Context context) {
        PlayerEntity player = context.player();

        var scene = payload.scene();

        if (scene == 1) { // SongSelect
            if (BeatCraft.currentTrackedPlayer.equals(player.getUuid())) {
                BeatCraft.currentTrackedPlayer = null;
                BeatCraft.currentTrackId = null;
                BeatCraft.currentSet = null;
                BeatCraft.currentDiff = null;
            }
        }

        PlayerLookup.all(context.server()).forEach(pl -> {
            ServerPlayNetworking.send(pl, new SceneSyncS2CPayload(payload.scene()));
            if (scene == 1) {
                BeatCraft.LOGGER.info("send untrack packet");
                ServerPlayNetworking.send(pl, new PlayerUntrackS2CPayload(player.getUuid()));
            }
        });
    }

    private static void handlePausePayload(SongPauseC2SPayload payload, ServerPlayNetworking.Context context) {

        PlayerLookup.all(context.server()).forEach(pl -> {
            ServerPlayNetworking.send(pl, new SongPauseS2CPayload(payload.paused()));
        });

    }

    private static void placeStructure(PlaceEnvironmentStructureC2SPayload payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            PlayerEntity player = context.player();
            ServerWorld world = (ServerWorld) player.getWorld();
            String struct = payload.struct();

            StructurePlacer.placeStructure(struct, world);
        });
    }

}
