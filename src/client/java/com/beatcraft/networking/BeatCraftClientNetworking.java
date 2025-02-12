package com.beatcraft.networking;

import com.beatcraft.BeatCraft;
import com.beatcraft.BeatCraftClient;
import com.beatcraft.BeatmapPlayer;
import com.beatcraft.data.menu.SongData;
import com.beatcraft.data.menu.SongDownloader;
import com.beatcraft.logic.GameLogicHandler;
import com.beatcraft.networking.s2c.BeatSyncS2CPayload;
import com.beatcraft.networking.s2c.MapSyncS2CPayload;
import com.beatcraft.networking.s2c.PlayerDisconnectS2CPayload;
import com.beatcraft.networking.s2c.SaberSyncS2CPayload;
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
        ClientPlayNetworking.registerGlobalReceiver(PlayerDisconnectS2CPayload.ID, BeatCraftClientNetworking::handlePlayerDisconnectPayload);
    }


    private static void handleSaberSyncPayload(SaberSyncS2CPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            UUID uuid = payload.player();

            ClientWorld world = context.client().world;

            if (world == null) return;

            SaberRenderer.otherPlayerSabers.put(uuid, new PlayFrame(0, payload.leftPos(), payload.leftRot(), payload.rightPos(), payload.rightRot()));

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
            SongDownloader.downloadFromId(song_id, MinecraftClient.getInstance().runDirectory.getAbsolutePath(), () -> loadNewSong(song_id, set, diff));

        });
    }

    private static void loadNewSong(String id, String set, String diff) {
        try {
            BeatCraftClient.songs.loadSongs();

            SongData data = BeatCraftClient.songs.getById(id);

            SongData.BeatmapInfo info = data.getBeatMapInfo(set, diff);

            BeatmapPlayer.setupDifficultyFromFile(info.getBeatmapLocation().toString());
            BeatmapPlayer.restart();
            GameLogicHandler.reset();
        } catch (IOException e) {
            BeatCraft.LOGGER.error("Failed to play song", e);
        }
    }

    private static void handleBeatSyncPayload(BeatSyncS2CPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            float beat = payload.beat();
            if (Math.abs(BeatmapPlayer.getCurrentBeat() - beat) > 0.01) {
                BeatmapPlayer.currentBeatmap.seek(beat);
            }
        });
    }

    private static void handlePlayerDisconnectPayload(PlayerDisconnectS2CPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            GameLogicHandler.untrack(payload.uuid());
        });
    }

}
