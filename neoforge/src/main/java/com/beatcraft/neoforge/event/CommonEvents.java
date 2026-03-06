package com.beatcraft.neoforge.event;

import com.beatcraft.Beatcraft;
import com.beatcraft.networking.ServerNetworking;
import com.beatcraft.networking.SetSaberModelPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Beatcraft.MOD_ID)
public class CommonEvents {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("beatcraft").optional();

        registrar.playToServer(
            SetSaberModelPayload.TYPE,
            SetSaberModelPayload.CODEC,
            (payload, context) ->
                context.enqueueWork(() -> ServerNetworking.handleSetSaberModel(
                    payload, context.player(), context::reply))
        );


    }

}
