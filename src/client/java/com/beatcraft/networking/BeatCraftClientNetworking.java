package com.beatcraft.networking;

import com.beatcraft.BeatCraft;
import com.beatcraft.BeatCraftClient;
import com.beatcraft.BeatmapPlayer;
import com.beatcraft.audio.BeatmapAudioPlayer;
import com.beatcraft.data.menu.SongData;
import com.beatcraft.data.menu.SongDownloader;
import com.beatcraft.logic.GameLogicHandler;
import com.beatcraft.networking.s2c.*;
import com.beatcraft.render.HUDRenderer;
import com.beatcraft.render.effect.SaberRenderer;
import com.beatcraft.replay.PlayFrame;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

import java.io.IOException;
import java.util.UUID;

public class BeatCraftClientNetworking {


    public static void init() {

        // receivers
        ClientPlayNetworking.registerGlobalReceiver(SaberSyncS2CPayload.ID, BeatCraftClientNetworking::handleSaberSyncPayload);
        ClientPlayNetworking.registerGlobalReceiver(MapSyncS2CPayload.ID, BeatCraftClientNetworking::handleMapSyncPayload);
        ClientPlayNetworking.registerGlobalReceiver(BeatSyncS2CPayload.ID, BeatCraftClientNetworking::handleBeatSyncPayload);
        ClientPlayNetworking.registerGlobalReceiver(PlayerUntrackS2CPayload.ID, BeatCraftClientNetworking::handlePlayerUntrackPayload);
        ClientPlayNetworking.registerGlobalReceiver(SpeedSyncS2CPayload.ID, BeatCraftClientNetworking::handleSpeedSyncPayload);
        ClientPlayNetworking.registerGlobalReceiver(SongPauseS2CPayload.ID, BeatCraftClientNetworking::handlePausePayload);
        ClientPlayNetworking.registerGlobalReceiver(SceneSyncS2CPayload.ID, BeatCraftClientNetworking::handleSceneSync);
    }


    private static void handleSaberSyncPayload(SaberSyncS2CPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            UUID uuid = payload.player();

            ClientWorld world = context.client().world;

            if (world == null) return;

            SaberRenderer.otherPlayerSabers.put(uuid, new PlayFrame(0, payload.leftPos(), payload.leftRot(), payload.rightPos(), payload.rightRot(), payload.headPos(), payload.headRot()));

        });
    }

    private static void handleMapSyncPayload(MapSyncS2CPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            UUID uuid = payload.player();
            String song_id = payload.uid();
            String set = payload.set();
            String diff = payload.diff();

            ClientWorld world = MinecraftClient.getInstance().world;
            if (world == null) return;
            //PlayerEntity player = world.getPlayerByUuid(uuid);
            //if (player == null) return;

            GameLogicHandler.trackPlayer(uuid);

            if (BeatCraftClient.songs.getById(song_id) == null) {
                BeatmapAudioPlayer.beatmapAudio.closeBuffer();
                SongDownloader.downloadFromId(song_id, MinecraftClient.getInstance().runDirectory.getAbsolutePath(), () -> loadNewSong(song_id, set, diff));
            } else {
                BeatmapAudioPlayer.beatmapAudio.closeBuffer();
                loadNewSong(song_id, set, diff);
            }

        });
    }

    private static void loadNewSong(String id, String set, String diff) {
        try {
            BeatCraftClient.songs.loadSongs();

            SongData data = BeatCraftClient.songs.getById(id);

            SongData.BeatmapInfo info = data.getBeatMapInfo(set, diff);

            BeatmapPlayer.setupDifficultyFromFile(info.getBeatmapLocation().toString());
            BeatmapAudioPlayer.playAudioFromFile(BeatmapPlayer.currentInfo.getSongFilename());
            BeatmapPlayer.restart();
            GameLogicHandler.reset();
            HUDRenderer.scene = HUDRenderer.MenuScene.InGame;
        } catch (IOException e) {
            BeatCraft.LOGGER.error("Failed to play song", e);
        }
    }

    private static void handleBeatSyncPayload(BeatSyncS2CPayload payload, ClientPlayNetworking.Context context) {
        if (GameLogicHandler.isTrackingClient()) return; // give time for song to download if map sync only just happened
        float beat = payload.beat();
        if (Math.abs(BeatmapPlayer.getCurrentBeat() - beat) > 0.1) {
            BeatmapPlayer.play(beat, true);
        }
    }

    private static void handlePlayerUntrackPayload(PlayerUntrackS2CPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            GameLogicHandler.untrack(payload.uuid());
        });
    }

    private static void handleSpeedSyncPayload(SpeedSyncS2CPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            BeatmapPlayer.setPlaybackSpeed(payload.speed(), true);
        });
    }

    private static void handlePausePayload(SongPauseS2CPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            if (payload.paused()) {
                BeatmapPlayer.pause(true);
                HUDRenderer.scene = HUDRenderer.MenuScene.Paused;
            } else {
                BeatmapPlayer.play();
                HUDRenderer.scene = HUDRenderer.MenuScene.InGame;
                GameLogicHandler.unpauseMap(false);
            }
        });
    }

    private static void handleSceneSync(SceneSyncS2CPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            var scene = HUDRenderer.MenuScene.values()[payload.scene()];
            HUDRenderer.scene = scene;
            if (scene == HUDRenderer.MenuScene.SongSelect || scene == HUDRenderer.MenuScene.EndScreen) {
                BeatmapPlayer.reset();
            }
        });
    }

}
