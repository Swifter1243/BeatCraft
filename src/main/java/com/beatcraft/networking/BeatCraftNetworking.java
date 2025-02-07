package com.beatcraft.networking;

import com.beatcraft.BeatCraft;
import com.beatcraft.networking.c2s.BeatSyncC2SPayload;
import com.beatcraft.networking.c2s.MapSyncC2SPayload;
import com.beatcraft.networking.c2s.SaberSyncC2SPayload;
import com.beatcraft.networking.s2c.BeatSyncS2CPayload;
import com.beatcraft.networking.s2c.MapSyncS2CPayload;
import com.beatcraft.networking.s2c.SaberSyncS2CPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class BeatCraftNetworking {

    // S2C
    public static final Identifier SABER_SYNC_S2C = Identifier.of(BeatCraft.MOD_ID, "saber_sync_s2c");
    public static final Identifier MAP_SYNC_S2C = Identifier.of(BeatCraft.MOD_ID, "map_sync_s2c");
    public static final Identifier BEAT_SYNC_S2C = Identifier.of(BeatCraft.MOD_ID, "beat_sync_s2c");

    // C2S
    public static final Identifier SABER_SYNC_C2S = Identifier.of(BeatCraft.MOD_ID, "saber_sync_c2s");
    public static final Identifier MAP_SYNC_C2S = Identifier.of(BeatCraft.MOD_ID, "map_sync_c2s");
    public static final Identifier BEAT_SYNC_C2S = Identifier.of(BeatCraft.MOD_ID, "beat_sync_c2s");

    public static void init() {

        // Server to Client
        PayloadTypeRegistry.playS2C().register(SaberSyncS2CPayload.ID, SaberSyncS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(MapSyncS2CPayload.ID, MapSyncS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(BeatSyncS2CPayload.ID, BeatSyncS2CPayload.CODEC);

        // Client to Server
        PayloadTypeRegistry.playC2S().register(SaberSyncC2SPayload.ID, SaberSyncC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(MapSyncC2SPayload.ID, MapSyncC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(BeatSyncC2SPayload.ID, BeatSyncC2SPayload.CODEC);

        // receivers
        ServerPlayNetworking.registerGlobalReceiver(SaberSyncC2SPayload.ID, BeatCraftNetworking::handleSaberSyncPayload);
        ServerPlayNetworking.registerGlobalReceiver(MapSyncC2SPayload.ID, BeatCraftNetworking::handleMapSyncPayload);
        ServerPlayNetworking.registerGlobalReceiver(BeatSyncC2SPayload.ID, BeatCraftNetworking::handleBeatSyncPayload);

    }

    private static void handleSaberSyncPayload(SaberSyncC2SPayload payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            PlayerEntity sending_player = context.player();
            UUID uuid = sending_player.getUuid();
            PlayerLookup.tracking(sending_player).forEach(player -> {
                ServerPlayNetworking.send(player, new SaberSyncS2CPayload(
                    uuid,
                    payload.leftPos(), payload.leftRot(),
                    payload.rightPos(), payload.rightRot()
                ));
            });
        });
    }

    private static void handleMapSyncPayload(MapSyncC2SPayload payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            PlayerEntity player = context.player();
            UUID uuid = player.getUuid();
            PlayerLookup.all(context.server()).forEach(pl -> {
                if (pl == player) return;
                ServerPlayNetworking.send(pl, new MapSyncS2CPayload(uuid, payload.uid()));
            });
        });
    }

    private static void handleBeatSyncPayload(BeatSyncC2SPayload payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            PlayerEntity player = context.player();
            PlayerLookup.tracking(player).forEach(pl -> {
                ServerPlayNetworking.send(pl, new BeatSyncS2CPayload(payload.beat()));
            });
        });
    }

}
