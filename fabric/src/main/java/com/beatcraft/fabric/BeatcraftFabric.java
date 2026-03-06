package com.beatcraft.fabric;

import com.beatcraft.Beatcraft;
import com.beatcraft.common.items.ModItems;
import com.beatcraft.fabric.common.items.FabricItems;
import com.beatcraft.fabric.common.items.FabricItemsGroup;
import com.beatcraft.networking.ServerNetworking;
import com.beatcraft.networking.SetSaberModelPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class BeatcraftFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        Beatcraft.init();

        FabricItems.init();
        FabricItemsGroup.init();
        ModItems.init();

        PayloadTypeRegistry.playC2S().register(SetSaberModelPayload.TYPE, SetSaberModelPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SetSaberModelPayload.TYPE,
            (payload, context) ->
                ServerNetworking.handleSetSaberModel(payload, context.player(),
                    context.responseSender()::sendPacket));

    }
}
