package com.beatcraft.networking;

import com.beatcraft.BeatCraft;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class BeatCraftNetworking {

    public static final Identifier SABER_SYNC_C2S_PAYLOAD = Identifier.of(BeatCraft.MOD_ID, "saber_sync_c2s");
    public static final Identifier SABER_SYNC_S2C_PAYLOAD = Identifier.of(BeatCraft.MOD_ID, "saber_sync_s2c");

    public static void init() {

        // Server to Client
        PayloadTypeRegistry.playS2C().register(SaberSyncS2CPayload.ID, SaberSyncS2CPayload.CODEC);

        // Client to Server
        PayloadTypeRegistry.playC2S().register(SaberSyncC2SPayload.ID, SaberSyncC2SPayload.CODEC);


        // receivers
        ServerPlayNetworking.registerGlobalReceiver(SaberSyncC2SPayload.ID, BeatCraftNetworking::handleSaberSyncPayload);

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


}
